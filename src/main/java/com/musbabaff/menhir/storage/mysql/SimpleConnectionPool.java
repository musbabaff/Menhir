/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage.mysql;

import com.musbabaff.menhir.config.storage.StorageSettings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A small JDBC connection pool on top of the MySQL / MariaDB driver that Paper ships. It exists so
 * Menhir does not have to bundle a pooling library; the plugin only ever needs a handful of
 * connections for its background flushes.
 *
 * <p>Connections are validated before reuse and replaced when broken.</p>
 */
public final class SimpleConnectionPool implements AutoCloseable {

    private static final String[] DRIVERS = {"com.mysql.cj.jdbc.Driver", "org.mariadb.jdbc.Driver"};

    private final String url;
    private final Properties properties;
    private final int maxSize;
    private final int timeoutMillis;
    private final BlockingQueue<Connection> idle;
    private final AtomicInteger total = new AtomicInteger();
    private volatile boolean closed;

    public SimpleConnectionPool(StorageSettings.MySql settings) {
        String driver = loadDriver();
        String scheme = driver.startsWith("org.mariadb") ? "jdbc:mariadb" : "jdbc:mysql";
        this.url = scheme + "://" + settings.host() + ":" + settings.port() + "/" + settings.database();
        this.properties = new Properties();
        properties.setProperty("user", settings.username());
        properties.setProperty("password", settings.password());
        properties.setProperty("useSSL", String.valueOf(settings.useSsl()));
        properties.setProperty("sslMode", settings.useSsl() ? "REQUIRED" : "DISABLED");
        properties.setProperty("connectTimeout", String.valueOf(settings.connectionTimeout()));
        properties.setProperty("socketTimeout", String.valueOf(Math.max(settings.connectionTimeout(), 15000)));
        properties.setProperty("characterEncoding", "utf8");
        properties.setProperty("useUnicode", "true");
        properties.setProperty("allowPublicKeyRetrieval", "true");
        properties.setProperty("autoReconnect", "false");
        this.maxSize = settings.poolSize();
        this.timeoutMillis = settings.connectionTimeout();
        this.idle = new ArrayBlockingQueue<>(maxSize);
    }

    private static String loadDriver() {
        for (String driver : DRIVERS) {
            try {
                Class.forName(driver);
                return driver;
            } catch (ClassNotFoundException ignored) {
                // try the next one
            }
        }
        throw new IllegalStateException("No MySQL/MariaDB JDBC driver found on the server classpath");
    }

    public String getUrl() {
        return url;
    }

    /**
     * Borrows a connection; call {@link #release(Connection)} when done.
     *
     * @throws SQLException if no connection could be obtained within the connection timeout
     */
    public Connection borrow() throws SQLException {
        if (closed) throw new SQLException("Connection pool is closed");
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (true) {
            Connection connection = idle.poll();
            if (connection != null) {
                if (isUsable(connection)) return connection;
                discard(connection);
                continue;
            }
            if (total.get() < maxSize) {
                total.incrementAndGet();
                try {
                    return DriverManager.getConnection(url, properties);
                } catch (SQLException | RuntimeException e) {
                    total.decrementAndGet();
                    throw e;
                }
            }
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) throw new SQLException("Timed out waiting for a database connection");
            try {
                connection = idle.poll(remaining, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for a database connection", e);
            }
            if (connection == null) throw new SQLException("Timed out waiting for a database connection");
            if (isUsable(connection)) return connection;
            discard(connection);
        }
    }

    /** Returns a connection to the pool (or closes it if the pool is closed / the connection broke). */
    public void release(Connection connection) {
        if (connection == null) return;
        if (closed || !isUsable(connection) || !idle.offer(connection)) discard(connection);
    }

    private boolean isUsable(Connection connection) {
        try {
            return !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private void discard(Connection connection) {
        total.decrementAndGet();
        try {
            connection.close();
        } catch (SQLException ignored) {
            // already broken
        }
    }

    @Override
    public void close() {
        closed = true;
        Connection connection;
        while ((connection = idle.poll()) != null) discard(connection);
    }

}
