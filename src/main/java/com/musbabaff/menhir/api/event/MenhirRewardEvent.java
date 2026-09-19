/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api.event;

import com.musbabaff.menhir.api.MenhirStone;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A reward is about to be given to a player. Fired once per reward and player, with the commands
 * that will be run by the console.
 *
 * <p>The command list is mutable: listeners may add, remove or rewrite commands. Cancelling the
 * event gives nothing to this player for this reward.</p>
 */
public class MenhirRewardEvent extends MenhirStoneEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final OfflinePlayer player;
    private final int place;
    private final String rewardId;
    private final List<String> commands;
    private boolean cancelled;

    /**
     * Creates a new {@code MenhirRewardEvent}.
     *
     * @param stone    the stone
     * @param player   the rewarded player (may be offline for end-of-round rewards)
     * @param place    the player's place in the round (1 = best), or {@code -1} for per-hit rewards
     * @param rewardId the reward's key under {@code rewards} in the config
     * @param commands the commands to run, placeholders already replaced (mutable)
     */
    public MenhirRewardEvent(@NotNull MenhirStone stone, @NotNull OfflinePlayer player, int place, @NotNull String rewardId, @NotNull List<String> commands) {
        super(stone);
        this.player = player;
        this.place = place;
        this.rewardId = rewardId;
        this.commands = commands;
    }

    /**
     * Returns the rewarded player.
     *
     * @return the rewarded player
     */
    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Returns the player's place in the round (1 = best), or {@code -1} for per-hit rewards.
     *
     * @return the player's place in the round (1 = best), or {@code -1} for per-hit rewards
     */
    public int getPlace() {
        return place;
    }

    /**
     * Returns the reward's config key.
     *
     * @return the reward's config key
     */
    public @NotNull String getRewardId() {
        return rewardId;
    }

    /**
     * Returns the commands that will be run; modify the list to change them.
     *
     * @return the commands that will be run; modify the list to change them
     */
    public @NotNull List<String> getCommands() {
        return commands;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
