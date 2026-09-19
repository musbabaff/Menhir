/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api.event;

import com.musbabaff.menhir.api.MenhirStone;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Base class of every Menhir event: something happened to a stone.
 */
public abstract class MenhirStoneEvent extends Event {

    private final MenhirStone stone;

    /**
     * Sets {@code stone}.
     *
     * @param stone the stone the event is about
     */
    protected MenhirStoneEvent(@NotNull MenhirStone stone) {
        this.stone = stone;
    }

    /**
     * Returns the stone the event is about.
     *
     * @return the stone the event is about
     */
    public @NotNull MenhirStone getStone() {
        return stone;
    }

}
