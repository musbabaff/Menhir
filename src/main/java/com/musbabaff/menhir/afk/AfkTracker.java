/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.afk;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers when each player last <em>moved</em> (changed position, not just looked around).
 * Pure bookkeeping with no Bukkit dependency, so the threshold logic is unit-testable.
 */
public final class AfkTracker {

    /** Minimum interval between two AFK warnings sent to the same player. */
    public static final long WARNING_COOLDOWN_MILLIS = 2000L;

    private final Map<UUID, Long> lastMovement = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastWarning = new ConcurrentHashMap<>();

    /** Records that the player moved at {@code now}. */
    public void recordMovement(UUID player, long now) {
        lastMovement.put(player, now);
    }

    /** Forgets everything about the player (call on quit). */
    public void remove(UUID player) {
        lastMovement.remove(player);
        lastWarning.remove(player);
    }

    /**
     * Milliseconds since the player last moved. Unknown players are treated as active ({@code 0}).
     */
    public long getIdleMillis(UUID player, long now) {
        Long last = lastMovement.get(player);
        if (last == null) return 0L;
        return Math.max(0L, now - last);
    }

    /**
     * @return {@code true} if the player has not moved for longer than {@code thresholdMillis}
     */
    public boolean isAfk(UUID player, long thresholdMillis, long now) {
        return getIdleMillis(player, now) > thresholdMillis;
    }

    /**
     * Decides whether a warning may be sent now and, if so, records it.
     *
     * @return {@code true} if at least {@link #WARNING_COOLDOWN_MILLIS} passed since the last warning
     */
    public boolean shouldWarn(UUID player, long now) {
        Long last = lastWarning.get(player);
        if (last != null && now - last < WARNING_COOLDOWN_MILLIS) return false;
        lastWarning.put(player, now);
        return true;
    }

    public boolean isTracked(UUID player) {
        return lastMovement.containsKey(player);
    }

    public void clear() {
        lastMovement.clear();
        lastWarning.clear();
    }

}
