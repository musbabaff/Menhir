/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.storage;

import com.musbabaff.menhir.api.TopEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behaviour every {@link StorageProvider} must have. Subclasses create the provider under test and
 * a fresh provider over the same backend to verify persistence.
 */
public abstract class StorageProviderContractTest {

    protected static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    protected static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");
    protected static final UUID CAROL = UUID.fromString("00000000-0000-0000-0000-000000000003");

    protected StorageProvider storage;

    /** A loaded, empty provider. */
    protected abstract StorageProvider createProvider();

    /** A new provider instance over the same backend, loaded. */
    protected abstract StorageProvider reopen();

    @BeforeEach
    void setUp() {
        storage = createProvider();
    }

    @AfterEach
    void tearDown() {
        if (storage != null) storage.close();
    }

    @Test
    @DisplayName("unknown players and stones read as zero / empty")
    void emptyReads() {
        assertEquals(0, storage.getBreaks("north", ALICE));
        assertTrue(storage.getTop("north", 10).isEmpty());
        assertTrue(storage.getGlobalTop(10).isEmpty());
        assertTrue(storage.getState("north").isEmpty());
        assertTrue(storage.getBlockIds().isEmpty());
    }

    @Test
    @DisplayName("hits accumulate and the top list is sorted with the limit applied")
    void addBreaksAndTop() {
        storage.addBreaks("north", ALICE, "Alice", 3);
        storage.addBreaks("north", ALICE, "Alice", 2);
        storage.addBreaks("north", BOB, "Bob", 7);
        storage.addBreaks("north", CAROL, "Carol", 1);
        assertEquals(5, storage.getBreaks("north", ALICE));
        List<TopEntry> top = storage.getTop("north", 2);
        assertEquals(List.of(new TopEntry(BOB, "Bob", 7), new TopEntry(ALICE, "Alice", 5)), top);
        assertEquals(3, storage.getPlayers("north").size());
    }

    @Test
    @DisplayName("global top sums hits over all stones")
    void globalTop() {
        storage.addBreaks("north", ALICE, "Alice", 3);
        storage.addBreaks("south", ALICE, "Alice", 4);
        storage.addBreaks("south", BOB, "Bob", 6);
        assertEquals(List.of(new TopEntry(ALICE, "Alice", 7), new TopEntry(BOB, "Bob", 6)), storage.getGlobalTop(10));
    }

    @Test
    @DisplayName("resetBlock clears one stone, resetPlayer one player, resetAll everything")
    void resets() {
        storage.addBreaks("north", ALICE, "Alice", 3);
        storage.addBreaks("south", ALICE, "Alice", 4);
        storage.addBreaks("south", BOB, "Bob", 6);
        storage.resetBlock("north");
        assertEquals(0, storage.getBreaks("north", ALICE));
        assertEquals(4, storage.getBreaks("south", ALICE));
        storage.resetPlayer(ALICE);
        assertEquals(0, storage.getBreaks("south", ALICE));
        assertEquals(6, storage.getBreaks("south", BOB));
        storage.resetAll();
        assertTrue(storage.getGlobalTop(10).isEmpty());
    }

    @Test
    @DisplayName("state is stored per stone; respawnAt is cleared when not broken")
    void state() {
        storage.setState("north", 42, true, 123_456L);
        assertEquals(new StoneState(42, true, 123_456L), storage.getState("north").orElseThrow());
        storage.setState("north", 250, false, 999L);
        assertEquals(new StoneState(250, false, 0L), storage.getState("north").orElseThrow());
    }

    @Test
    @DisplayName("data survives save + reopen")
    void persistence() {
        storage.addBreaks("north", ALICE, "Alice", 3);
        storage.addBreaks("north", BOB, "Bob", 1);
        storage.setState("north", 97, true, 5_000L);
        storage.setState("south", 10, false, 0L);
        storage.close();
        storage = reopen();
        assertEquals(3, storage.getBreaks("north", ALICE));
        assertEquals(new StoneState(97, true, 5_000L), storage.getState("north").orElseThrow());
        assertEquals(new StoneState(10, false, 0L), storage.getState("south").orElseThrow());
        assertEquals(List.of(new TopEntry(ALICE, "Alice", 3), new TopEntry(BOB, "Bob", 1)), storage.getTop("north", 10));
    }

    @Test
    @DisplayName("deleteBlock removes state and hits, also after reopen")
    void delete() {
        storage.addBreaks("north", ALICE, "Alice", 3);
        storage.setState("north", 1, false, 0L);
        storage.deleteBlock("north");
        assertTrue(storage.getState("north").isEmpty());
        assertEquals(0, storage.getBreaks("north", ALICE));
        storage.close();
        storage = reopen();
        assertFalse(storage.getBlockIds().contains("north"));
    }

    @Test
    @DisplayName("renameBlock moves everything to the new id")
    void rename() {
        storage.addBreaks("north", ALICE, "Alice", 3);
        storage.setState("north", 5, false, 0L);
        storage.renameBlock("north", "west");
        assertEquals(3, storage.getBreaks("west", ALICE));
        assertEquals(0, storage.getBreaks("north", ALICE));
        storage.close();
        storage = reopen();
        assertEquals(3, storage.getBreaks("west", ALICE));
        assertEquals(new StoneState(5, false, 0L), storage.getState("west").orElseThrow());
        assertFalse(storage.getBlockIds().contains("north"));
    }

}
