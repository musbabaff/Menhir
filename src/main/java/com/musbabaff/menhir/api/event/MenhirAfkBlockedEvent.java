/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api.event;

import com.musbabaff.menhir.api.MenhirStone;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A hit is about to be ignored because the player counts as AFK.
 *
 * <p>Cancelling the event lets the hit through as if the player were active.</p>
 */
public class MenhirAfkBlockedEvent extends MenhirStoneEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final long idleMillis;
    private boolean cancelled;

    /**
     * Creates a new {@code MenhirAfkBlockedEvent}.
     *
     * @param stone      the stone
     * @param player     the player
     * @param idleMillis milliseconds since the player last moved
     */
    public MenhirAfkBlockedEvent(@NotNull MenhirStone stone, @NotNull Player player, long idleMillis) {
        super(stone);
        this.player = player;
        this.idleMillis = idleMillis;
    }

    /**
     * Returns the player.
     *
     * @return the player
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Returns milliseconds since the player last changed position.
     *
     * @return milliseconds since the player last changed position
     */
    public long getIdleMillis() {
        return idleMillis;
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
