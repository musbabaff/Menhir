/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage.yaml;

import com.musbabaff.menhir.api.TopEntry;
import com.musbabaff.menhir.storage.AbstractStorage;
import com.musbabaff.menhir.storage.MemoryStore;
import com.musbabaff.menhir.storage.MemoryStore.BlockSnapshot;
import com.musbabaff.menhir.storage.StoneState;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * File storage: one {@code storage/<id>.yml} per stone. Legacy binary {@code <id>.mb} files are
 * imported when no {@code .yml} exists for the stone (the {@code .mb} file is left untouched).
 *
 * <p>Only stones that changed are rewritten. Writes happen on a single background thread from an
 * immutable snapshot taken on the caller thread; {@link #close()} writes synchronously.</p>
 */
public final class YamlStorage extends AbstractStorage {

    public static final String EXTENSION = ".yml";

    private final File folder;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Menhir-Storage-YAML");
        thread.setDaemon(true);
        return thread;
    });

    public YamlStorage(File storageFolder, java.util.logging.Logger logger) {
        super(logger);
        this.folder = storageFolder;
    }

    @Override
    public String getType() {
        return "YAML";
    }

    public File getFolder() {
        return folder;
    }

    @Override
    public CompletableFuture<Void> load() {
        memory.clear();
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        File[] files = folder.listFiles();
        if (files == null) return CompletableFuture.completedFuture(null);
        for (File file : files) {
            String name = file.getName();
            if (name.endsWith(EXTENSION)) {
                String id = name.substring(0, name.length() - EXTENSION.length());
                try {
                    BlockSnapshot snapshot = readYaml(id, file);
                    memory.putLoaded(id, snapshot.state(), snapshot.players().values());
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Could not read " + file.getName(), e);
                }
            }
        }
        // Legacy .mb files: import those without a .yml counterpart.
        for (File file : files) {
            String name = file.getName();
            if (!name.endsWith(LegacyBlockFile.EXTENSION)) continue;
            String id = name.substring(0, name.length() - LegacyBlockFile.EXTENSION.length());
            if (new File(folder, id + EXTENSION).exists()) continue;
            try {
                BlockSnapshot snapshot = LegacyBlockFile.read(id, file);
                memory.putLoaded(id, snapshot.state(), snapshot.players().values());
                writeYaml(snapshot);
                logger.info("Imported legacy data file " + name + " into " + id + EXTENSION);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not import legacy data file " + name, e);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void save() {
        if (!memory.hasChanges()) return;
        MemoryStore.ChangeSet changes = memory.drain();
        executor.execute(() -> write(changes));
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (memory.hasChanges()) write(memory.drain());
    }

    private void write(MemoryStore.ChangeSet changes) {
        for (String deleted : changes.deletedBlocks()) {
            try {
                Files.deleteIfExists(new File(folder, deleted + EXTENSION).toPath());
                Files.deleteIfExists(new File(folder, deleted + LegacyBlockFile.EXTENSION).toPath());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not delete data of block " + deleted, e);
            }
        }
        for (BlockSnapshot snapshot : changes.dirtySnapshots().values()) {
            try {
                writeYaml(snapshot);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not save data of block " + snapshot.blockId(), e);
            }
        }
    }

    private void writeYaml(BlockSnapshot snapshot) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        StoneState state = snapshot.state();
        if (state != null) {
            yaml.set("health", state.health());
            yaml.set("broken", state.broken());
            yaml.set("respawn-at", state.respawnAt());
        }
        List<Map<String, Object>> players = new ArrayList<>();
        for (TopEntry entry : snapshot.players().values()) {
            players.add(Map.of("uuid", entry.uuid().toString(), "name", entry.name(), "breaks", entry.breaks()));
        }
        yaml.set("players", players);
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        File file = new File(folder, snapshot.blockId() + EXTENSION);
        File temp = new File(folder, snapshot.blockId() + EXTENSION + ".tmp");
        yaml.save(temp);
        Files.move(temp.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    static BlockSnapshot readYaml(String blockId, File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        StoneState state = yaml.contains("health")
                ? new StoneState(yaml.getInt("health"), yaml.getBoolean("broken"), yaml.getLong("respawn-at"))
                : null;
        Map<UUID, TopEntry> players = new java.util.LinkedHashMap<>();
        for (Map<?, ?> entry : yaml.getMapList("players")) {
            Object uuid = entry.get("uuid");
            if (uuid == null) continue;
            try {
                UUID id = UUID.fromString(String.valueOf(uuid));
                Object rawName = entry.get("name");
                Object rawBreaks = entry.get("breaks");
                String name = rawName == null ? "?" : String.valueOf(rawName);
                int breaks = rawBreaks == null ? 0 : Integer.parseInt(String.valueOf(rawBreaks));
                players.put(id, new TopEntry(id, name, breaks));
            } catch (IllegalArgumentException ignored) {
                // skip malformed rows instead of losing the whole file
            }
        }
        return new BlockSnapshot(blockId, state, players);
    }

}
