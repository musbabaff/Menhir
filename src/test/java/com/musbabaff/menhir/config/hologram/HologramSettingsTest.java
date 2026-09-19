/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.config.hologram;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HologramSettingsTest {

    private static HologramSettings parse(String yaml) {
        return HologramSettings.parse(YamlConfiguration.loadConfiguration(new StringReader(yaml)));
    }

    @Test
    @DisplayName("built-in defaults are applied when nothing is set")
    void defaults() {
        HologramSettings s = HologramSettings.EMPTY;
        assertEquals(1.0, s.getScale());
        assertEquals(Display.Billboard.CENTER, s.getBillboard());
        assertEquals(HologramSettings.BACKGROUND_TRANSPARENT, s.getBackground());
        assertEquals(48, s.getViewDistance());
        assertEquals(HologramSettings.UPDATE_LIVE, s.getUpdateIntervalTicks());
        assertFalse(s.hasLines());
        assertFalse(s.hasOffset());
    }

    @Test
    @DisplayName("a MineBlocks-style hologram section (offset + lines only) parses")
    void legacySection() {
        HologramSettings s = parse("""
                offset:
                  x: 0
                  y: 2
                  z: 0
                lines:
                  - '#ICON: %type%'
                  - '&7line'
                """);
        assertTrue(s.hasOffset());
        assertEquals(2.0, s.getOffsetY());
        assertEquals(List.of("#ICON: %type%", "&7line"), s.getLines());
        assertTrue(s.hasLines());
        assertTrue(s.getTemplate().isEmpty());
    }

    @Test
    @DisplayName("merge lets the override win only for keys it sets")
    void merge() {
        HologramSettings template = parse("""
                scale: 1.5
                billboard: VERTICAL
                lines: ['a']
                """);
        HologramSettings block = parse("""
                template: default
                billboard: FIXED
                offset: {y: 1}
                """);
        HologramSettings merged = HologramSettings.BUILT_IN.merge(template).merge(block);
        assertEquals(1.5, merged.getScale());
        assertEquals(Display.Billboard.FIXED, merged.getBillboard());
        assertEquals(List.of("a"), merged.getLines());
        assertEquals(1.0, merged.getOffsetY());
        assertEquals("default", merged.getTemplate().orElseThrow());
    }

    @Test
    @DisplayName("update-interval seconds convert to ticks, -1 stays live")
    void updateInterval() {
        assertEquals(20, HologramSettings.secondsToTicks(1));
        assertEquals(10, HologramSettings.secondsToTicks(0.5));
        assertEquals(HologramSettings.UPDATE_LIVE, HologramSettings.secondsToTicks(-1));
        assertEquals(HologramSettings.UPDATE_LIVE, HologramSettings.secondsToTicks(0));
        assertEquals(1, HologramSettings.secondsToTicks(0.01));
    }

    @Test
    @DisplayName("background accepts transparent, default, #RRGGBB and #AARRGGBB")
    void background() {
        assertEquals(0, HologramSettings.parseBackground("transparent"));
        assertEquals(HologramSettings.BACKGROUND_DEFAULT, HologramSettings.parseBackground("default"));
        assertEquals(0xFF112233, HologramSettings.parseBackground("#112233"));
        assertEquals(0x80112233, HologramSettings.parseBackground("#80112233"));
        assertThrows(IllegalArgumentException.class, () -> HologramSettings.parseBackground("blue"));
    }

    @Test
    @DisplayName("enum values are case-insensitive and invalid values fail loudly")
    void enums() {
        assertEquals(TextDisplay.TextAlignment.LEFT, parse("alignment: left").getAlignment());
        assertThrows(IllegalArgumentException.class, () -> parse("billboard: sideways"));
    }

}
