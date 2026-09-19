/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api.event;

import com.musbabaff.menhir.api.MenhirStone;
import com.musbabaff.menhir.api.TopEntry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * A stone reached zero health. Fired before rewards are given and before the round is reset, so the
 * leaderboard of the finished round is still available.
 */
public class MenhirBreakEvent extends MenhirStoneEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player breaker;
    private final List<TopEntry> topPlayers;
    private final int totalBreaks;

    /**
     * Creates a new {@code MenhirBreakEvent}.
     *
     * @param stone       the stone
     * @param breaker     the player who dealt the last hit, {@code null} when broken through the API
     * @param topPlayers  the leaderboard of the round, most hits first (unmodifiable)
     * @param totalBreaks the sum of all hits of the round
     */
    public MenhirBreakEvent(@NotNull MenhirStone stone, @Nullable Player breaker, @NotNull List<TopEntry> topPlayers, int totalBreaks) {
        super(stone);
        this.breaker = breaker;
        this.topPlayers = List.copyOf(topPlayers);
        this.totalBreaks = totalBreaks;
    }

    /**
     * Returns the player who dealt the last hit, or {@code null} when the stone was broken by a command or the API.
     *
     * @return the player who dealt the last hit, or {@code null} when the stone was broken by a command or the API
     */
    public @Nullable Player getBreaker() {
        return breaker;
    }

    /**
     * Returns the leaderboard of the round, most hits first.
     *
     * @return the leaderboard of the round, most hits first
     */
    public @NotNull List<TopEntry> getTopPlayers() {
        return topPlayers;
    }

    /**
     * Returns the sum of all hits of the round.
     *
     * @return the sum of all hits of the round
     */
    public int getTotalBreaks() {
        return totalBreaks;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the handler list of this event type.
     *
     * @return the handler list of this event type
     */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

}
