/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.countdown;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure arithmetic of the respawn countdown: which warning thresholds are still ahead of us and when
 * they fire. Thresholds whose moment has already passed (for example while the server was down) are
 * never returned, so they are not announced late after a restart.
 */
public final class CountdownSchedule {

    /** One warning: the threshold in seconds and the absolute time it fires at. */
    public record Warning(int secondsLeft, long fireAt) {
    }

    private CountdownSchedule() {
    }

    /**
     * @param warnAt      thresholds in seconds before the respawn (any order, duplicates ignored)
     * @param respawnAt   respawn time in epoch milliseconds
     * @param now         current time in epoch milliseconds
     * @return warnings that are still in the future, ordered by firing time (largest threshold first)
     */
    public static List<Warning> remaining(List<Integer> warnAt, long respawnAt, long now) {
        List<Warning> warnings = new ArrayList<>();
        for (Integer seconds : warnAt) {
            if (seconds == null || seconds <= 0) continue;
            long fireAt = respawnAt - seconds * 1000L;
            if (fireAt <= now) continue;
            if (warnings.stream().anyMatch(w -> w.secondsLeft() == seconds)) continue;
            warnings.add(new Warning(seconds, fireAt));
        }
        warnings.sort((a, b) -> Long.compare(a.fireAt(), b.fireAt()));
        return warnings;
    }

    /** Ticks (20 per second) from {@code now} until {@code fireAt}, at least 1. */
    public static long ticksUntil(long fireAt, long now) {
        return Math.max(1L, (long) Math.ceil((fireAt - now) / 50d));
    }

}
