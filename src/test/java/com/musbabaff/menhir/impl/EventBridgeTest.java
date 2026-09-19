/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.impl;

import com.musbabaff.menhir.api.event.MenhirAfkBlockedEvent;
import com.musbabaff.menhir.api.event.MenhirDamageEvent;
import com.musbabaff.menhir.api.event.MenhirRewardEvent;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.health.BlockHealth;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that listener decisions (cancel / modify) really stop or change the internal flow.
 * The Bukkit event bus is replaced by a lambda, so no server is needed.
 */
class EventBridgeTest {

    private static MenhirBlock stone(int health) {
        MenhirBlock block = new MenhirBlock(null);
        block.setId("north");
        block.setHealth(new BlockHealth(block, 250));
        block.getHealth().setHealth(health);
        return block;
    }

    private static EventBridge bridge(java.util.function.Consumer<Event> listener) {
        return new EventBridge(listener);
    }

    @Test
    @DisplayName("an uncancelled damage event keeps the damage")
    void damagePassesThrough() {
        List<Event> seen = new ArrayList<>();
        OptionalInt damage = bridge(seen::add).damage(stone(100), null, 1);
        assertEquals(OptionalInt.of(1), damage);
        MenhirDamageEvent event = (MenhirDamageEvent) seen.get(0);
        assertEquals(100, event.getCurrentHealth());
        assertEquals(99, event.getNewHealth());
        assertEquals("north", event.getStone().getId());
    }

    @Test
    @DisplayName("cancelling the damage event discards the hit")
    void damageCancelled() {
        OptionalInt damage = bridge(e -> ((Cancellable) e).setCancelled(true)).damage(stone(100), null, 1);
        assertTrue(damage.isEmpty());
    }

    @Test
    @DisplayName("listeners can change the damage; it never drops below one")
    void damageModified() {
        assertEquals(OptionalInt.of(5), bridge(e -> ((MenhirDamageEvent) e).setDamage(5)).damage(stone(100), null, 1));
        assertEquals(OptionalInt.of(1), bridge(e -> ((MenhirDamageEvent) e).setDamage(-3)).damage(stone(100), null, 1));
        MenhirDamageEvent event = new MenhirDamageEvent(new StoneView(stone(3)), null, 10, 3);
        assertEquals(0, event.getNewHealth(), "new health never goes negative");
    }

    @Test
    @DisplayName("the AFK block applies unless a listener cancels it")
    void afkBlocked() {
        assertTrue(bridge(e -> { }).afkBlocked(stone(100), null, 20_000L));
        assertFalse(bridge(e -> ((Cancellable) e).setCancelled(true)).afkBlocked(stone(100), null, 20_000L));
        List<Event> seen = new ArrayList<>();
        bridge(seen::add).afkBlocked(stone(100), null, 20_000L);
        assertEquals(20_000L, ((MenhirAfkBlockedEvent) seen.get(0)).getIdleMillis());
    }

    @Test
    @DisplayName("reward commands pass through, can be rewritten, and vanish when cancelled")
    void reward() {
        List<String> commands = List.of("give %player% diamond");
        assertEquals(Optional.of(commands), bridge(e -> { }).reward(stone(0), null, 1, "first", commands));

        Optional<List<String>> rewritten = bridge(e -> {
            MenhirRewardEvent event = (MenhirRewardEvent) e;
            assertEquals(1, event.getPlace());
            assertEquals("first", event.getRewardId());
            event.getCommands().clear();
            event.getCommands().add("eco give Steve 100");
        }).reward(stone(0), null, 1, "first", commands);
        assertEquals(Optional.of(List.of("eco give Steve 100")), rewritten);

        assertTrue(bridge(e -> ((Cancellable) e).setCancelled(true)).reward(stone(0), null, -1, "hit", commands).isEmpty());
        assertEquals(List.of("give %player% diamond"), commands, "the original list is never modified");
    }

}
