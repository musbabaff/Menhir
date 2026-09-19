/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.config.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossBarSettingsTest {

    private static BossBarSettings parse(String yaml) {
        return BossBarSettings.parse(YamlConfiguration.loadConfiguration(new StringReader(yaml)));
    }

    @Test
    @DisplayName("default thresholds pick green / yellow / red by health percentage")
    void defaultThresholds() {
        BossBarSettings s = BossBarSettings.BUILT_IN;
        assertEquals(BossBar.Color.GREEN, s.colorFor(100));
        assertEquals(BossBar.Color.GREEN, s.colorFor(67));
        assertEquals(BossBar.Color.YELLOW, s.colorFor(66));
        assertEquals(BossBar.Color.YELLOW, s.colorFor(34));
        assertEquals(BossBar.Color.RED, s.colorFor(33));
        assertEquals(BossBar.Color.RED, s.colorFor(1));
        // 0 % is not "above 0", so the fixed colour is used
        assertEquals(BossBar.Color.RED, s.colorFor(0));
    }

    @Test
    @DisplayName("thresholds are sorted by 'above' descending regardless of config order")
    void thresholdsSorted() {
        BossBarSettings s = parse("""
                color: WHITE
                color-thresholds:
                  - { above: 0, color: RED }
                  - { above: 50, color: BLUE }
                """);
        assertEquals(BossBar.Color.BLUE, s.colorFor(80));
        assertEquals(BossBar.Color.RED, s.colorFor(20));
        assertEquals(BossBar.Color.WHITE, s.colorFor(0));
    }

    @Test
    @DisplayName("an empty threshold list means the fixed colour is always used")
    void noThresholds() {
        BossBarSettings s = parse("""
                color: PURPLE
                color-thresholds: []
                """);
        assertTrue(s.getColorThresholds().isEmpty());
        assertEquals(BossBar.Color.PURPLE, s.colorFor(100));
        assertEquals(BossBar.Color.PURPLE, s.colorFor(5));
    }

    @Test
    @DisplayName("block overrides win over the global settings only for the keys they set")
    void merge() {
        BossBarSettings global = parse("""
                enabled: true
                show-to: WORLD
                hide-after: 12
                """);
        BossBarSettings block = parse("""
                show-to: hitter
                max-bars-per-player: 2
                """);
        BossBarSettings merged = BossBarSettings.BUILT_IN.merge(global).merge(block);
        assertEquals(BossBarSettings.ShowTo.HITTER, merged.getShowTo());
        assertEquals(12, merged.getHideAfter());
        assertEquals(2, merged.getMaxBarsPerPlayer());
        assertEquals(BossBar.Overlay.NOTCHED_20, merged.getStyle());
    }

    @Test
    @DisplayName("invalid enum values fail with a clear message")
    void invalidValues() {
        assertThrows(IllegalArgumentException.class, () -> parse("style: DOTTED"));
        assertThrows(IllegalArgumentException.class, () -> parse("show-to: NEARBY"));
        assertThrows(IllegalArgumentException.class, () -> parse("color-thresholds: [{above: 5}]"));
    }

}
