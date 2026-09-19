/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Locale;

/**
 * The root {@code storage} section.
 */
public final class StorageSettings {

    /** Storage backend. */
    public enum Type { YAML, MYSQL }

    /** MySQL connection settings. */
    public record MySql(String host, int port, String database, String username, String password,
                        String tablePrefix, boolean useSsl, int poolSize, int connectionTimeout, int reconnectInterval) {
    }

    private final Type type;
    private final String serverId;
    private final boolean crossServerLeaderboard;
    private final int flushInterval;
    private final MySql mysql;

    public StorageSettings(ConfigurationSection section) {
        ConfigurationSection config = section != null ? section : new MemoryConfiguration();
        String rawType = config.getString("type", "YAML").trim().toUpperCase(Locale.ROOT);
        this.type = switch (rawType) {
            case "MYSQL", "MARIADB" -> Type.MYSQL;
            case "YAML", "YML", "FILE", "FLATFILE" -> Type.YAML;
            default -> throw new IllegalArgumentException("Invalid storage.type '" + rawType + "' (use YAML or MYSQL)");
        };
        this.serverId = config.getString("server-id", "survival");
        this.crossServerLeaderboard = config.getBoolean("cross-server-leaderboard", false);
        this.flushInterval = Math.max(1, config.getInt("flush-interval", 30));
        ConfigurationSection sql = config.getConfigurationSection("mysql");
        if (sql == null) sql = new MemoryConfiguration();
        this.mysql = new MySql(
                sql.getString("host", "127.0.0.1"),
                sql.getInt("port", 3306),
                sql.getString("database", "menhir"),
                sql.getString("username", ""),
                sql.getString("password", ""),
                sql.getString("table-prefix", "menhir_"),
                sql.getBoolean("use-ssl", false),
                Math.max(1, sql.getInt("pool-size", 6)),
                Math.max(1000, sql.getInt("connection-timeout", 5000)),
                Math.max(5, sql.getInt("reconnect-interval", 60)));
        if (serverId.length() > 32) throw new IllegalArgumentException("storage.server-id must be at most 32 characters");
    }

    public Type getType() { return type; }
    public String getServerId() { return serverId; }
    public boolean isCrossServerLeaderboard() { return crossServerLeaderboard; }
    /** Seconds between two background flushes. */
    public int getFlushInterval() { return flushInterval; }
    public MySql getMysql() { return mysql; }

}
