/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage.mysql;

import java.util.List;

/**
 * SQL text used by {@link MySqlStorage}, built once from the table prefix. Kept free of JDBC so
 * the statements and their parameter order can be unit-tested.
 */
public final class MySqlQueries {

    /** Version written to the meta table; bump when the schema changes. */
    public static final int SCHEMA_VERSION = 1;

    private final String breaks;
    private final String state;
    private final String meta;

    public MySqlQueries(String prefix) {
        String p = prefix == null ? "" : prefix;
        if (!p.matches("[A-Za-z0-9_]*")) throw new IllegalArgumentException("Invalid table prefix '" + p + "'");
        this.breaks = p + "breaks";
        this.state = p + "state";
        this.meta = p + "meta";
    }

    public String breaksTable() { return breaks; }
    public String stateTable() { return state; }
    public String metaTable() { return meta; }

    /** DDL statements, safe to run on every start. */
    public List<String> createTables() {
        return List.of(
                "CREATE TABLE IF NOT EXISTS " + breaks + " ("
                        + "server VARCHAR(32) NOT NULL, "
                        + "block_id VARCHAR(64) NOT NULL, "
                        + "player_uuid CHAR(36) NOT NULL, "
                        + "player_name VARCHAR(16) NOT NULL, "
                        + "breaks INT NOT NULL DEFAULT 0, "
                        + "updated_at BIGINT NOT NULL, "
                        + "PRIMARY KEY (server, block_id, player_uuid), "
                        + "INDEX idx_top (server, block_id, breaks DESC))",
                "CREATE TABLE IF NOT EXISTS " + state + " ("
                        + "server VARCHAR(32) NOT NULL, "
                        + "block_id VARCHAR(64) NOT NULL, "
                        + "health INT NOT NULL DEFAULT -1, "
                        + "broken TINYINT(1) NOT NULL DEFAULT 0, "
                        + "respawn_at BIGINT NOT NULL DEFAULT 0, "
                        + "PRIMARY KEY (server, block_id))",
                "CREATE TABLE IF NOT EXISTS " + meta + " ("
                        + "k VARCHAR(32) NOT NULL PRIMARY KEY, "
                        + "v VARCHAR(64) NOT NULL)"
        );
    }

    /** Params: k, v, v. */
    public String upsertMeta() {
        return "INSERT INTO " + meta + " (k, v) VALUES (?, ?) ON DUPLICATE KEY UPDATE v = ?";
    }

    /** Params: k. */
    public String selectMeta() {
        return "SELECT v FROM " + meta + " WHERE k = ?";
    }

    /** Params: server. Columns: block_id, player_uuid, player_name, breaks. */
    public String selectBreaksOfServer() {
        return "SELECT block_id, player_uuid, player_name, breaks FROM " + breaks + " WHERE server = ?";
    }

    /** Params: server. Columns: block_id, health, broken, respawn_at. */
    public String selectStatesOfServer() {
        return "SELECT block_id, health, broken, respawn_at FROM " + state + " WHERE server = ?";
    }

    /** Params: server, block_id, player_uuid, player_name, breaks, updated_at, breaks (delta), player_name, updated_at. */
    public String upsertBreaks() {
        return "INSERT INTO " + breaks + " (server, block_id, player_uuid, player_name, breaks, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE breaks = breaks + ?, player_name = ?, updated_at = ?";
    }

    /** Params: server, block_id, health, broken, respawn_at, health, broken, respawn_at. */
    public String upsertState() {
        return "INSERT INTO " + state + " (server, block_id, health, broken, respawn_at) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE health = ?, broken = ?, respawn_at = ?";
    }

    /** Params: server, block_id. */
    public String deleteBreaksOfBlock() {
        return "DELETE FROM " + breaks + " WHERE server = ? AND block_id = ?";
    }

    /** Params: server, block_id. */
    public String deleteStateOfBlock() {
        return "DELETE FROM " + state + " WHERE server = ? AND block_id = ?";
    }

    /** Params: server, player_uuid. */
    public String deleteBreaksOfPlayer() {
        return "DELETE FROM " + breaks + " WHERE server = ? AND player_uuid = ?";
    }

    /** Params: server. */
    public String deleteAllBreaks() {
        return "DELETE FROM " + breaks + " WHERE server = ?";
    }

    /**
     * Top players of one block. Params: block_id, limit (own server) or server, block_id, limit.
     * Columns: player_uuid, player_name, breaks.
     */
    public String selectTop(boolean crossServer) {
        if (crossServer) {
            return "SELECT player_uuid, MAX(player_name) AS player_name, SUM(breaks) AS breaks FROM " + breaks
                    + " WHERE block_id = ? GROUP BY player_uuid ORDER BY breaks DESC LIMIT ?";
        }
        return "SELECT player_uuid, player_name, breaks FROM " + breaks
                + " WHERE server = ? AND block_id = ? ORDER BY breaks DESC LIMIT ?";
    }

    /**
     * Global top over all blocks. Params: limit (cross-server) or server, limit.
     * Columns: player_uuid, player_name, breaks.
     */
    public String selectGlobalTop(boolean crossServer) {
        if (crossServer) {
            return "SELECT player_uuid, MAX(player_name) AS player_name, SUM(breaks) AS breaks FROM " + breaks
                    + " GROUP BY player_uuid ORDER BY breaks DESC LIMIT ?";
        }
        return "SELECT player_uuid, MAX(player_name) AS player_name, SUM(breaks) AS breaks FROM " + breaks
                + " WHERE server = ? GROUP BY player_uuid ORDER BY breaks DESC LIMIT ?";
    }

}
