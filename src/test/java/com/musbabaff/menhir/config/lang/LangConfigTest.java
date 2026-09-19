/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.config.lang;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LangConfigTest {

    private static LangConfig lang(int maxUnits) {
        String yaml = """
                top:
                  nobody: "&cNobody"
                timeout:
                  message: "&cRespawns in %time%"
                  max-units: MAX
                  units:
                    hour: " hour"
                    hours: " hours"
                    minute: " minute"
                    minutes: " minutes"
                    second: " second"
                    seconds: " seconds"
                """.replace("MAX", String.valueOf(maxUnits));
        return new LangConfig(YamlConfiguration.loadConfiguration(new StringReader(yaml)));
    }

    @Test
    @DisplayName("remaining time is split into units, limited by max-units")
    void formatsUnits() {
        long now = 1_000_000L;
        long end = now + (2 * 3600 + 5 * 60 + 9) * 1000L - 1; // one millisecond short of exactly 9 s
        assertEquals("&cRespawns in 2 hours 5 minutes", lang(2).getTimeoutFormatted(end, now));
        assertEquals("&cRespawns in 2 hours 5 minutes 9 seconds", lang(3).getTimeoutFormatted(end, now));
    }

    @Test
    @DisplayName("singular unit names are used for a value of one")
    void singular() {
        assertEquals("&cRespawns in 1 minute 1 second", lang(2).getTimeoutFormatted(60_999L, 0L));
    }

    @Test
    @DisplayName("sub-second remainders round up to one second")
    void roundsUp() {
        assertEquals("&cRespawns in 1 second", lang(2).getTimeoutFormatted(400L, 0L));
    }

    @Test
    @DisplayName("defaults are used for missing keys")
    void defaults() {
        LangConfig lang = new LangConfig(null);
        assertEquals("Nobody", lang.getNobodyName());
        assertEquals("0", lang.getNobodyBreaks());
        assertEquals("Not ranked", lang.getNotRanked());
        assertEquals("You are AFK!", lang.getStatusAFK());
    }

}
