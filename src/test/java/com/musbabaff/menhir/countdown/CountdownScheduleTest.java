/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.countdown;

import com.musbabaff.menhir.config.countdown.CountdownSettings;
import com.musbabaff.menhir.config.lang.LangConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountdownScheduleTest {

    private static final List<Integer> WARN_AT = List.of(300, 60, 10);

    @Test
    @DisplayName("all thresholds are scheduled for a fresh cooldown, ordered by firing time")
    void freshCooldown() {
        long now = 1_000_000L;
        long respawnAt = now + 3600_000L;
        List<CountdownSchedule.Warning> warnings = CountdownSchedule.remaining(WARN_AT, respawnAt, now);
        assertEquals(List.of(300, 60, 10), warnings.stream().map(CountdownSchedule.Warning::secondsLeft).toList());
        assertEquals(respawnAt - 300_000L, warnings.get(0).fireAt());
        assertEquals(respawnAt - 10_000L, warnings.get(2).fireAt());
    }

    @Test
    @DisplayName("thresholds that passed while the server was down are skipped after a restart")
    void skipsPastThresholds() {
        long now = 1_000_000L;
        long respawnAt = now + 45_000L; // 45 s left: the 5 min and 1 min warnings are in the past
        List<CountdownSchedule.Warning> warnings = CountdownSchedule.remaining(WARN_AT, respawnAt, now);
        assertEquals(List.of(10), warnings.stream().map(CountdownSchedule.Warning::secondsLeft).toList());
    }

    @Test
    @DisplayName("a threshold exactly at 'now' is not announced (it would be late)")
    void exactBoundary() {
        long now = 1_000_000L;
        assertTrue(CountdownSchedule.remaining(List.of(10), now + 10_000L, now).isEmpty());
        assertEquals(1, CountdownSchedule.remaining(List.of(10), now + 10_001L, now).size());
    }

    @Test
    @DisplayName("duplicates, zero and negative thresholds are ignored")
    void ignoresInvalid() {
        long now = 0L;
        List<CountdownSchedule.Warning> warnings = CountdownSchedule.remaining(List.of(60, 60, 0, -5, 30), 100_000L, now);
        assertEquals(List.of(60, 30), warnings.stream().map(CountdownSchedule.Warning::secondsLeft).toList());
    }

    @Test
    @DisplayName("ticksUntil rounds up and is at least one tick")
    void ticks() {
        assertEquals(1L, CountdownSchedule.ticksUntil(10L, 10L));
        assertEquals(1L, CountdownSchedule.ticksUntil(10L, 0L));
        assertEquals(2L, CountdownSchedule.ticksUntil(51L, 0L));
        assertEquals(20L, CountdownSchedule.ticksUntil(1000L, 0L));
    }

    @Test
    @DisplayName("settings sort warn-at descending and expose the last threshold")
    void settings() {
        CountdownSettings settings = CountdownSettings.parse(YamlConfiguration.loadConfiguration(new StringReader("""
                warn-at: [10, 300, 60, 60]
                title:
                  only-last: false
                """)));
        assertEquals(List.of(300, 60, 10), settings.getWarnAt());
        assertEquals(10, settings.getLastThreshold());
        CountdownSettings merged = CountdownSettings.BUILT_IN.merge(settings);
        assertEquals(CountdownSettings.BroadcastTo.WORLD, merged.getBroadcastTo());
        assertTrue(merged.getTitle().isEnabled());
        assertEquals(false, merged.getTitle().isOnlyLast());
        assertEquals(40, merged.getTitle().getStay());
    }

    @Test
    @DisplayName("%time% is formatted with the configured units without rounding up")
    void durationFormat() {
        LangConfig lang = new LangConfig(YamlConfiguration.loadConfiguration(new StringReader("""
                timeout:
                  message: "x %time%"
                  max-units: 2
                  units: {hour: " saat", hours: " saat", minute: " dakika", minutes: " dakika", second: " saniye", seconds: " saniye"}
                """)));
        assertEquals("5 dakika", lang.formatDuration(300));
        assertEquals("1 dakika", lang.formatDuration(60));
        assertEquals("10 saniye", lang.formatDuration(10));
        assertEquals("1 saat 1 dakika", lang.formatDuration(3661));
    }

}
