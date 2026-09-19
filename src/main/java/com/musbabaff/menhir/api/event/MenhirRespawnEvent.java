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
 * A broken stone came back.
 */
public class MenhirRespawnEvent extends MenhirStoneEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final boolean scheduled;

    /**
     * Creates a new {@code MenhirRespawnEvent}.
     *
     * @param stone     the stone
     * @param scheduled {@code true} when the cooldown timer ran out, {@code false} for a command or API call
     */
    public MenhirRespawnEvent(@NotNull MenhirStone stone, boolean scheduled) {
        super(stone);
        this.scheduled = scheduled;
    }

    /**
     * Returns {@code true} when the cooldown timer ran out, {@code false} for a command or API call.
     *
     * @return {@code true} when the cooldown timer ran out, {@code false} for a command or API call
     */
    public boolean isScheduled() {
        return scheduled;
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
