/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage.mysql;

import com.musbabaff.menhir.api.TopEntry;
import com.musbabaff.menhir.config.storage.StorageSettings;
import com.musbabaff.menhir.storage.AbstractStorage;
import com.musbabaff.menhir.storage.MemoryStore;
import com.musbabaff.menhir.storage.MemoryStore.BreakDelta;
import com.musbabaff.menhir.storage.MemoryStore.ChangeSet;
import com.musbabaff.menhir.storage.StoneState;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MySQL / MariaDB storage.
 *
 * <p>All JDBC work runs on one background thread. Hits are accumulated in memory and written as a
 * batch by {@link #save()} (called every {@code flush-interval} seconds); {@link #close()} flushes
 * synchronously. When the database is unreachable the plugin keeps running from memory, logs the
 * problem once and retries every {@code reconnect-interval} seconds. Changes that could not be
 * written at shutdown are kept in {@code storage/mysql-pending.yml} and replayed on the next start.</p>
 *
 * <p>The {@code server} column is filled with {@code storage.server-id}. With
 * {@code cross-server-leaderboard: true} the top lists are read from the database (all servers)
 * after every flush; otherwise they come from memory like the file storage.</p>
 */
public final class MySqlStorage extends AbstractStorage {

    private static final int TOP_CACHE_SIZE = 10;

    private final StorageSettings settings;
    private final MySqlQueries queries;
    private final String server;
    private final boolean crossServer;
    private final PendingFile pendingFile;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Menhir-Storage-MySQL");
        thread.setDaemon(true);
        return thread;
    });

    private SimpleConnectionPool pool;
    private volatile boolean available = true;
    private volatile long unavailableSince;
    private volatile boolean loaded;
    private volatile Map<String, List<TopEntry>> topCache = Map.of();
    private volatile List<TopEntry> globalTopCache = List.of();

    public MySqlStorage(StorageSettings settings, File storageFolder, Logger logger) {
        super(logger);
        this.settings = settings;
        this.queries = new MySqlQueries(settings.getMysql().tablePrefix());
        this.server = settings.getServerId();
        this.crossServer = settings.isCrossServerLeaderboard();
        this.pendingFile = new PendingFile(new File(storageFolder, "mysql-pending.yml"));
    }

    @Override
    public String getType() {
        return "MYSQL";
    }

    @Override
    public boolean isCrossServer() {
        return crossServer;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    // --- loading ---

    @Override
    public CompletableFuture<Void> load() {
        return CompletableFuture.runAsync(() -> {
            try {
                openPool();
                Connection connection = pool.borrow();
                try {
                    ensureSchema(connection);
                    readAll(connection);
                    if (crossServer) refreshCaches(connection);
                } finally {
                    pool.release(connection);
                }
                loaded = true;
                markAvailable();
                ChangeSet pending = pendingFile.readAndDelete(logger);
                if (pending != null) {
                    memory.requeue(pending);
                    logger.info("Replaying " + pending.breaks().size() + " pending hit record(s) from the previous session");
                }
            } catch (SQLException | RuntimeException e) {
                markUnavailable(e);
                throw new IllegalStateException("MySQL storage could not be loaded: " + e.getMessage(), e);
            }
        }, executor);
    }

    private synchronized void openPool() {
        if (pool == null) pool = new SimpleConnectionPool(settings.getMysql());
    }

    private void ensureSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String ddl : queries.createTables()) statement.executeUpdate(ddl);
        }
        try (PreparedStatement statement = connection.prepareStatement(queries.upsertMeta())) {
            statement.setString(1, "schema_version");
            statement.setString(2, String.valueOf(MySqlQueries.SCHEMA_VERSION));
            statement.setString(3, String.valueOf(MySqlQueries.SCHEMA_VERSION));
            statement.executeUpdate();
        }
    }

    private void readAll(Connection connection) throws SQLException {
        Map<String, List<TopEntry>> players = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(queries.selectBreaksOfServer())) {
            statement.setString(1, server);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    players.computeIfAbsent(rs.getString("block_id"), k -> new ArrayList<>())
                            .add(new TopEntry(UUID.fromString(rs.getString("player_uuid")), rs.getString("player_name"), rs.getInt("breaks")));
                }
            }
        }
        Map<String, StoneState> states = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(queries.selectStatesOfServer())) {
            statement.setString(1, server);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    states.put(rs.getString("block_id"), new StoneState(rs.getInt("health"), rs.getBoolean("broken"), rs.getLong("respawn_at")));
                }
            }
        }
        memory.clear();
        for (Map.Entry<String, List<TopEntry>> entry : players.entrySet()) {
            memory.putLoaded(entry.getKey(), states.remove(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<String, StoneState> entry : states.entrySet()) {
            memory.putLoaded(entry.getKey(), entry.getValue(), List.of());
        }
    }

    // --- reads that may come from the cross-server cache ---

    @Override
    public List<TopEntry> getTop(String blockId, int limit) {
        if (crossServer && loaded) {
            List<TopEntry> cached = topCache.get(blockId);
            if (cached != null) return cached.size() > limit ? cached.subList(0, limit) : cached;
        }
        return super.getTop(blockId, limit);
    }

    @Override
    public List<TopEntry> getGlobalTop(int limit) {
        if (crossServer && loaded) {
            List<TopEntry> cached = globalTopCache;
            return cached.size() > limit ? cached.subList(0, limit) : cached;
        }
        return super.getGlobalTop(limit);
    }

    // --- flushing ---

    @Override
    public void save() {
        if (!memory.hasChanges() && !crossServer) return;
        if (!available && System.currentTimeMillis() - unavailableSince < settings.getMysql().reconnectInterval() * 1000L) return;
        ChangeSet changes = memory.drain();
        executor.execute(() -> flush(changes));
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (memory.hasChanges()) {
            ChangeSet changes = memory.drain();
            if (!flush(changes)) {
                pendingFile.write(changes, logger);
                logger.warning("Database unreachable at shutdown; " + changes.breaks().size()
                        + " pending hit record(s) were saved to " + pendingFile.getFile().getName());
            }
        }
        synchronized (this) {
            if (pool != null) pool.close();
        }
    }

    /** Writes a change set; returns {@code false} (and re-queues it) when the database failed. */
    private boolean flush(ChangeSet changes) {
        Connection connection = null;
        try {
            openPool();
            connection = pool.borrow();
            if (!loaded) {
                ensureSchema(connection);
                readAll(connection);
                loaded = true;
            }
            connection.setAutoCommit(false);
            try {
                apply(connection, changes);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            if (crossServer) refreshCaches(connection);
            markAvailable();
            return true;
        } catch (SQLException | RuntimeException e) {
            memory.requeue(changes);
            markUnavailable(e);
            return false;
        } finally {
            if (connection != null) pool.release(connection);
        }
    }

    private void apply(Connection connection, ChangeSet changes) throws SQLException {
        long now = System.currentTimeMillis();
        if (changes.resetAll()) {
            try (PreparedStatement statement = connection.prepareStatement(queries.deleteAllBreaks())) {
                statement.setString(1, server);
                statement.executeUpdate();
            }
        }
        for (String blockId : changes.deletedBlocks()) {
            for (String sql : List.of(queries.deleteBreaksOfBlock(), queries.deleteStateOfBlock())) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, server);
                    statement.setString(2, blockId);
                    statement.executeUpdate();
                }
            }
        }
        if (!changes.resetBlocks().isEmpty()) {
            try (PreparedStatement statement = connection.prepareStatement(queries.deleteBreaksOfBlock())) {
                for (String blockId : changes.resetBlocks()) {
                    statement.setString(1, server);
                    statement.setString(2, blockId);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
        if (!changes.resetPlayers().isEmpty()) {
            try (PreparedStatement statement = connection.prepareStatement(queries.deleteBreaksOfPlayer())) {
                for (UUID player : changes.resetPlayers()) {
                    statement.setString(1, server);
                    statement.setString(2, player.toString());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
        if (!changes.breaks().isEmpty()) {
            try (PreparedStatement statement = connection.prepareStatement(queries.upsertBreaks())) {
                for (BreakDelta delta : changes.breaks()) {
                    if (delta.amount() == 0) continue;
                    statement.setString(1, server);
                    statement.setString(2, delta.blockId());
                    statement.setString(3, delta.player().toString());
                    statement.setString(4, truncate(delta.name(), 16));
                    statement.setInt(5, Math.max(0, delta.amount()));
                    statement.setLong(6, now);
                    statement.setInt(7, delta.amount());
                    statement.setString(8, truncate(delta.name(), 16));
                    statement.setLong(9, now);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
        if (!changes.states().isEmpty()) {
            try (PreparedStatement statement = connection.prepareStatement(queries.upsertState())) {
                for (Map.Entry<String, StoneState> entry : changes.states().entrySet()) {
                    StoneState state = entry.getValue();
                    statement.setString(1, server);
                    statement.setString(2, entry.getKey());
                    statement.setInt(3, state.health());
                    statement.setBoolean(4, state.broken());
                    statement.setLong(5, state.respawnAt());
                    statement.setInt(6, state.health());
                    statement.setBoolean(7, state.broken());
                    statement.setLong(8, state.respawnAt());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
    }

    private void refreshCaches(Connection connection) throws SQLException {
        Map<String, List<TopEntry>> tops = new HashMap<>();
        for (String blockId : memory.getBlockIds()) {
            try (PreparedStatement statement = connection.prepareStatement(queries.selectTop(true))) {
                statement.setString(1, blockId);
                statement.setInt(2, TOP_CACHE_SIZE);
                tops.put(blockId, readTop(statement));
            }
        }
        List<TopEntry> global;
        try (PreparedStatement statement = connection.prepareStatement(queries.selectGlobalTop(true))) {
            statement.setInt(1, TOP_CACHE_SIZE);
            global = readTop(statement);
        }
        topCache = Map.copyOf(tops);
        globalTopCache = global;
    }

    private static List<TopEntry> readTop(PreparedStatement statement) throws SQLException {
        List<TopEntry> entries = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                entries.add(new TopEntry(UUID.fromString(rs.getString("player_uuid")), rs.getString("player_name"), rs.getInt("breaks")));
            }
        }
        return List.copyOf(entries);
    }

    private static String truncate(String value, int max) {
        if (value == null) return "?";
        return value.length() <= max ? value : value.substring(0, max);
    }

    private void markAvailable() {
        if (!available) logger.info("Database connection restored");
        available = true;
    }

    private void markUnavailable(Exception e) {
        if (available) {
            logger.log(Level.SEVERE, "Database unavailable, running from memory and retrying every "
                    + settings.getMysql().reconnectInterval() + " s: " + e.getMessage());
        }
        available = false;
        unavailableSince = System.currentTimeMillis();
    }

}
