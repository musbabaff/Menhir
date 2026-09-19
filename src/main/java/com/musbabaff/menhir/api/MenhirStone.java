/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * Read-only view of a Menhir stone. Instances are live: every getter reflects the current state.
 * Obtain them from {@link MenhirAPI#getStones()} or {@link MenhirAPI#getStone(String)}.
 */
public interface MenhirStone {

    /**
     * Returns the config id of the stone ({@code blocks.<id>}).
     *
     * @return the config id of the stone ({@code blocks.<id>})
     */
    @NotNull String getId();

    /**
     * Returns the display name ({@code display-name} in the config, raw text with colour codes),.
     *
     * @return the display name ({@code display-name} in the config, raw text with colour codes),
     *         or the id when none is configured
     */
    @NotNull String getDisplayName();

    /**
     * Returns a copy of the block location.
     *
     * @return a copy of the block location
     */
    @NotNull Location getLocation();

    /**
     * Returns the current health (hits left until the stone breaks).
     *
     * @return the current health (hits left until the stone breaks)
     */
    int getHealth();

    /**
     * Returns the configured maximum health.
     *
     * @return the configured maximum health
     */
    int getMaxHealth();

    /**
     * Returns {@code true} while the stone is in its respawn cooldown.
     *
     * @return {@code true} while the stone is in its respawn cooldown
     */
    boolean isBroken();

    /**
     * Returns the respawn time in epoch milliseconds, empty while the stone is not broken.
     *
     * @return the respawn time in epoch milliseconds, empty while the stone is not broken
     */
    @NotNull OptionalLong getRespawnAt();

    /**
     * Returns the permission a player needs to hit the stone, empty when none is required.
     *
     * @return the permission a player needs to hit the stone, empty when none is required
     */
    @NotNull Optional<String> getPermission();

}
