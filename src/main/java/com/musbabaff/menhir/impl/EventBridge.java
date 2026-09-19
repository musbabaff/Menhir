/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.impl;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.api.MenhirStone;
import com.musbabaff.menhir.api.TopEntry;
import com.musbabaff.menhir.api.event.MenhirAfkBlockedEvent;
import com.musbabaff.menhir.api.event.MenhirBreakEvent;
import com.musbabaff.menhir.api.event.MenhirDamageEvent;
import com.musbabaff.menhir.api.event.MenhirRespawnCountdownEvent;
import com.musbabaff.menhir.api.event.MenhirRespawnEvent;
import com.musbabaff.menhir.api.event.MenhirRewardEvent;
import com.musbabaff.menhir.block.MenhirBlock;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * Fires the public API events from the internal flow and reports the listeners' decisions back.
 * The event dispatcher is injectable so the gate logic can be unit-tested without a server.
 */
@ApiStatus.Internal
public class EventBridge {

    private final Consumer<Event> caller;

    public EventBridge(MenhirPlugin plugin) {
        this(event -> plugin.getServer().getPluginManager().callEvent(event));
    }

    public EventBridge(Consumer<Event> caller) {
        this.caller = caller;
    }

    public MenhirStone view(MenhirBlock block) {
        return new StoneView(block);
    }

    /**
     * Fires {@link MenhirDamageEvent}.
     *
     * @return the (possibly modified) damage, or empty when a listener cancelled the hit
     */
    public OptionalInt damage(MenhirBlock block, Player player, int damage) {
        MenhirDamageEvent event = new MenhirDamageEvent(view(block), player, damage, block.getHealth().getHealth());
        caller.accept(event);
        return event.isCancelled() ? OptionalInt.empty() : OptionalInt.of(event.getDamage());
    }

    /**
     * Fires {@link MenhirAfkBlockedEvent}.
     *
     * @return {@code true} when the hit must be ignored, {@code false} when a listener cancelled the block
     */
    public boolean afkBlocked(MenhirBlock block, Player player, long idleMillis) {
        MenhirAfkBlockedEvent event = new MenhirAfkBlockedEvent(view(block), player, idleMillis);
        caller.accept(event);
        return !event.isCancelled();
    }

    /** Fires {@link MenhirBreakEvent}. */
    public void broken(MenhirBlock block, Player breaker, List<TopEntry> top, int totalBreaks) {
        caller.accept(new MenhirBreakEvent(view(block), breaker, top, totalBreaks));
    }

    /**
     * Fires {@link MenhirRewardEvent}.
     *
     * @return the commands to run (listeners may have changed them), or empty when cancelled
     */
    public Optional<List<String>> reward(MenhirBlock block, OfflinePlayer player, int place, String rewardId, List<String> commands) {
        MenhirRewardEvent event = new MenhirRewardEvent(view(block), player, place, rewardId, new ArrayList<>(commands));
        caller.accept(event);
        return event.isCancelled() ? Optional.empty() : Optional.of(event.getCommands());
    }

    /** Fires {@link MenhirRespawnEvent}. */
    public void respawn(MenhirBlock block, boolean scheduled) {
        caller.accept(new MenhirRespawnEvent(view(block), scheduled));
    }

    /** Fires {@link MenhirRespawnCountdownEvent}. */
    public void respawnCountdown(MenhirBlock block, int secondsLeft) {
        caller.accept(new MenhirRespawnCountdownEvent(view(block), secondsLeft));
    }

}
