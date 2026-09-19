/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api.event;

import com.musbabaff.menhir.api.MenhirStone;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A respawn countdown warning is being announced ({@code respawn-countdown.warn-at}).
 */
public class MenhirRespawnCountdownEvent extends MenhirStoneEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int secondsLeft;

    /**
     * Creates a new {@code MenhirRespawnCountdownEvent}.
     *
     * @param stone       the stone
     * @param secondsLeft seconds until the respawn
     */
    public MenhirRespawnCountdownEvent(@NotNull MenhirStone stone, int secondsLeft) {
        super(stone);
        this.secondsLeft = secondsLeft;
    }

    /**
     * Returns seconds until the respawn.
     *
     * @return seconds until the respawn
     */
    public int getSecondsLeft() {
        return secondsLeft;
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
