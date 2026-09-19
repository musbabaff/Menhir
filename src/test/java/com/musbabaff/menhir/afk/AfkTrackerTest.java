/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.afk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AfkTrackerTest {

    private static final long THRESHOLD = 15_000L;

    @Test
    @DisplayName("a player who moved recently is not AFK")
    void recentlyMoved() {
        AfkTracker tracker = new AfkTracker();
        UUID id = UUID.randomUUID();
        tracker.recordMovement(id, 1_000L);
        assertFalse(tracker.isAfk(id, THRESHOLD, 1_000L + THRESHOLD));
    }

    @Test
    @DisplayName("a player becomes AFK once the idle time exceeds the threshold")
    void becomesAfk() {
        AfkTracker tracker = new AfkTracker();
        UUID id = UUID.randomUUID();
        tracker.recordMovement(id, 1_000L);
        assertTrue(tracker.isAfk(id, THRESHOLD, 1_000L + THRESHOLD + 1));
        assertEquals(THRESHOLD + 1, tracker.getIdleMillis(id, 1_000L + THRESHOLD + 1));
    }

    @Test
    @DisplayName("moving again resets the idle time")
    void movementResets() {
        AfkTracker tracker = new AfkTracker();
        UUID id = UUID.randomUUID();
        tracker.recordMovement(id, 0L);
        tracker.recordMovement(id, 20_000L);
        assertFalse(tracker.isAfk(id, THRESHOLD, 30_000L));
    }

    @Test
    @DisplayName("unknown players are treated as active")
    void unknownPlayer() {
        AfkTracker tracker = new AfkTracker();
        assertFalse(tracker.isAfk(UUID.randomUUID(), THRESHOLD, 999_999L));
    }

    @Test
    @DisplayName("removed players are forgotten")
    void remove() {
        AfkTracker tracker = new AfkTracker();
        UUID id = UUID.randomUUID();
        tracker.recordMovement(id, 0L);
        tracker.remove(id);
        assertFalse(tracker.isTracked(id));
    }

    @Test
    @DisplayName("warnings are limited to one per two seconds")
    void warningCooldown() {
        AfkTracker tracker = new AfkTracker();
        UUID id = UUID.randomUUID();
        assertTrue(tracker.shouldWarn(id, 10_000L));
        assertFalse(tracker.shouldWarn(id, 11_000L));
        assertFalse(tracker.shouldWarn(id, 11_999L));
        assertTrue(tracker.shouldWarn(id, 12_000L));
    }

}
