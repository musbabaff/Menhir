/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.storage.mysql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySqlQueriesTest {

    private final MySqlQueries q = new MySqlQueries("menhir_");

    private static int params(String sql) {
        return (int) sql.chars().filter(c -> c == '?').count();
    }

    @Test
    @DisplayName("tables carry the prefix and the DDL matches the documented schema")
    void schema() {
        assertEquals("menhir_breaks", q.breaksTable());
        assertEquals("menhir_state", q.stateTable());
        assertEquals("menhir_meta", q.metaTable());
        List<String> ddl = q.createTables();
        assertEquals(3, ddl.size());
        assertTrue(ddl.get(0).startsWith("CREATE TABLE IF NOT EXISTS menhir_breaks ("));
        assertTrue(ddl.get(0).contains("PRIMARY KEY (server, block_id, player_uuid)"));
        assertTrue(ddl.get(0).contains("INDEX idx_top (server, block_id, breaks DESC)"));
        assertTrue(ddl.get(1).contains("PRIMARY KEY (server, block_id)"));
        assertTrue(ddl.get(1).contains("health INT"));
        assertTrue(ddl.get(2).contains("k VARCHAR(32) NOT NULL PRIMARY KEY"));
    }

    @Test
    @DisplayName("an empty prefix is allowed, unsafe prefixes are rejected")
    void prefixes() {
        assertEquals("breaks", new MySqlQueries("").breaksTable());
        assertThrows(IllegalArgumentException.class, () -> new MySqlQueries("bad prefix;"));
    }

    @Test
    @DisplayName("upserts add the delta on conflict and bind parameters in the documented order")
    void upserts() {
        String breaks = q.upsertBreaks();
        assertEquals("INSERT INTO menhir_breaks (server, block_id, player_uuid, player_name, breaks, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE breaks = breaks + ?, player_name = ?, updated_at = ?", breaks);
        assertEquals(9, params(breaks));

        String state = q.upsertState();
        assertEquals("INSERT INTO menhir_state (server, block_id, health, broken, respawn_at) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE health = ?, broken = ?, respawn_at = ?", state);
        assertEquals(8, params(state));

        assertEquals(3, params(q.upsertMeta()));
    }

    @Test
    @DisplayName("reads and deletes are scoped to the server column")
    void serverScoped() {
        assertEquals("SELECT block_id, player_uuid, player_name, breaks FROM menhir_breaks WHERE server = ?", q.selectBreaksOfServer());
        assertEquals("SELECT block_id, health, broken, respawn_at FROM menhir_state WHERE server = ?", q.selectStatesOfServer());
        assertEquals("DELETE FROM menhir_breaks WHERE server = ? AND block_id = ?", q.deleteBreaksOfBlock());
        assertEquals("DELETE FROM menhir_state WHERE server = ? AND block_id = ?", q.deleteStateOfBlock());
        assertEquals("DELETE FROM menhir_breaks WHERE server = ? AND player_uuid = ?", q.deleteBreaksOfPlayer());
        assertEquals("DELETE FROM menhir_breaks WHERE server = ?", q.deleteAllBreaks());
    }

    @Test
    @DisplayName("cross-server leaderboards drop the server filter and sum per player")
    void leaderboards() {
        assertEquals("SELECT player_uuid, player_name, breaks FROM menhir_breaks WHERE server = ? AND block_id = ? ORDER BY breaks DESC LIMIT ?",
                q.selectTop(false));
        String cross = q.selectTop(true);
        assertTrue(cross.contains("SUM(breaks)"));
        assertTrue(cross.contains("GROUP BY player_uuid"));
        assertTrue(!cross.contains("server"));
        assertEquals(2, params(cross));
        assertEquals(2, params(q.selectGlobalTop(false)));
        assertEquals(1, params(q.selectGlobalTop(true)));
    }

}
