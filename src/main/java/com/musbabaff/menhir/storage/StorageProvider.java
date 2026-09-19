/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage;

import com.musbabaff.menhir.api.TopEntry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Persistence of stone states and per-round hit counts.
 *
 * <p>Every read and write except {@link #load()}, {@link #save()} and {@link #close()} works on an
 * in-memory copy and is safe to call from the main thread; implementations persist the changes in
 * the background. Hit counts are per round: {@link #resetBlock(String)} is called whenever a stone
 * breaks or is reset, exactly like the in-memory model of the plugin.</p>
 */
public interface StorageProvider {

    /** Short name for logs and commands ({@code YAML}, {@code MYSQL}). */
    String getType();

    /**
     * Loads everything into memory. The future completes on an arbitrary thread; callers must switch
     * back to the main thread before touching Bukkit objects.
     */
    CompletableFuture<Void> load();

    /** Persists pending changes asynchronously. */
    void save();

    /** Persists pending changes synchronously and releases resources. */
    void close();

    /** Hits of a player on a stone in the current round ({@code 0} if unknown). */
    int getBreaks(String blockId, UUID player);

    /** Adds hits for a player on a stone; {@code name} refreshes the stored player name. */
    void addBreaks(String blockId, UUID player, String name, int amount);

    /** Best players of a stone, most hits first. */
    List<TopEntry> getTop(String blockId, int limit);

    /** Best players over all stones (hits summed per player), most hits first. */
    List<TopEntry> getGlobalTop(int limit);

    /** All players of a stone with their hits (unordered). */
    Map<UUID, TopEntry> getPlayers(String blockId);

    /** Removes all hits of a stone (new round). */
    void resetBlock(String blockId);

    /** Removes a player's hits from every stone. */
    void resetPlayer(UUID player);

    /** Removes all hits of all stones. */
    void resetAll();

    /** Stored state of a stone, empty when it was never saved. */
    Optional<StoneState> getState(String blockId);

    /** Stores the state of a stone. */
    void setState(String blockId, int health, boolean broken, long respawnAt);

    /** Forgets a stone completely (state and hits). */
    void deleteBlock(String blockId);

    /** Moves the data of a stone to a new id. */
    void renameBlock(String oldId, String newId);

    /** Ids of the stones that have stored data. */
    Collection<String> getBlockIds();

    /**
     * Whether leaderboards combine several servers. When {@code true}, {@link #getTop} and
     * {@link #getGlobalTop} may return players of other servers.
     */
    default boolean isCrossServer() {
        return false;
    }

    /** Whether the backend is reachable right now (always {@code true} for file storage). */
    default boolean isAvailable() {
        return true;
    }

}
