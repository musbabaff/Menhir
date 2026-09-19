/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * One row of a leaderboard: a player and the number of hits ("breaks") credited to them.
 *
 * @param uuid   the player's unique id
 * @param name   the player's last known name
 * @param breaks the number of hits
 */
public record TopEntry(@NotNull UUID uuid, @NotNull String name, int breaks) {

    /**
     * Creates an entry.
     *
     * @param uuid   the player's unique id, not {@code null}
     * @param name   the player's last known name, not {@code null}
     * @param breaks the number of hits, never negative
     * @throws IllegalArgumentException if {@code breaks} is negative
     */
    public TopEntry {
        if (uuid == null || name == null) throw new IllegalArgumentException("uuid and name must not be null");
        if (breaks < 0) throw new IllegalArgumentException("breaks must not be negative");
    }

}
