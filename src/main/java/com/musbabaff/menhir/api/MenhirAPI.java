/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * Entry point of the Menhir API.
 *
 * <p>Obtain an instance with {@link MenhirProvider#get()} or through the Bukkit services manager:
 * {@code Bukkit.getServicesManager().load(MenhirAPI.class)}. Add {@code softdepend: [Menhir]} to your
 * {@code plugin.yml} so Menhir is enabled before your plugin.</p>
 *
 * <p>All methods must be called from the main server thread unless stated otherwise. Methods that
 * change a stone fire the corresponding {@code com.musbabaff.menhir.api.event} events.</p>
 */
public interface MenhirAPI {

    /**
     * Returns every loaded stone (unmodifiable).
     *
     * @return every loaded stone (unmodifiable)
     */
    @NotNull Collection<MenhirStone> getStones();

    /**
     * Returns the stone, or empty when no stone has this id.
     *
     * @param id the config id of the stone
     * @return the stone, or empty when no stone has this id
     */
    @NotNull Optional<MenhirStone> getStone(@NotNull String id);

    /**
     * Returns {@code true} if the stone exists and is in its respawn cooldown.
     *
     * @param id the config id of the stone
     * @return {@code true} if the stone exists and is in its respawn cooldown
     */
    boolean isBroken(@NotNull String id);

    /**
     * Returns the respawn time in epoch milliseconds, empty when the stone is unknown or not broken.
     *
     * @param id the config id of the stone
     * @return the respawn time in epoch milliseconds, empty when the stone is unknown or not broken
     */
    @NotNull OptionalLong getRespawnAt(@NotNull String id);

    /**
     * Breaks the stone now: rewards are given, the break message is sent and the cooldown starts,
     * exactly as if the last hit had just happened. Does nothing when the stone is unknown or
     * already broken.
     *
     * @param id the config id of the stone
     */
    void breakStone(@NotNull String id);

    /**
     * Ends the cooldown of a broken stone now. Does nothing when the stone is unknown or not broken.
     *
     * @param id the config id of the stone
     */
    void respawnStone(@NotNull String id);

    /**
     * Returns hits of the player on the stone in the current round ({@code 0} if none).
     *
     * @param stoneId the config id of the stone
     * @param player  the player
     * @return hits of the player on the stone in the current round ({@code 0} if none)
     */
    int getBreaks(@NotNull String stoneId, @NotNull UUID player);

    /**
     * Returns the leaderboard of the stone, most hits first (cross-server when so configured).
     *
     * @param stoneId the config id of the stone
     * @param limit   maximum number of rows
     * @return the leaderboard of the stone, most hits first (cross-server when so configured)
     */
    @NotNull List<TopEntry> getTop(@NotNull String stoneId, int limit);

    /**
     * Returns the leaderboard over all stones (hits summed per player), most hits first.
     *
     * @param limit maximum number of rows
     * @return the leaderboard over all stones (hits summed per player), most hits first
     */
    @NotNull List<TopEntry> getGlobalTop(int limit);

    /**
     * Resets a stone: full health, cooldown ended, all hits of the round removed.
     *
     * @param id the config id of the stone
     */
    void resetStone(@NotNull String id);

    /**
     * Removes all hits of a player from every stone.
     *
     * @param player the player
     */
    void resetPlayer(@NotNull UUID player);

}
