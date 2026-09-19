/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.config;

import com.musbabaff.menhir.config.bossbar.BossBarSettings;
import com.musbabaff.menhir.config.countdown.CountdownSettings;
import com.musbabaff.menhir.config.lang.LangConfig;
import com.musbabaff.menhir.config.options.OptionsConfig;
import com.musbabaff.menhir.config.storage.StorageSettings;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The shipped {@code config.yml} must parse and expose the documented defaults.
 */
class DefaultConfigTest {

    private static final Logger LOGGER = Logger.getLogger("menhir-test");

    private static YamlConfiguration load(String path) {
        File file = new File(path);
        assertTrue(file.isFile(), "missing: " + file.getAbsolutePath());
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        assertFalse(yaml.getKeys(false).isEmpty(), "file did not parse: " + path);
        return yaml;
    }

    @Test
    @DisplayName("default config: boss bar, countdown and storage defaults")
    void defaults() {
        YamlConfiguration yaml = load("src/main/resources/config.yml");
        OptionsConfig options = new OptionsConfig(yaml.getConfigurationSection("options"), LOGGER);

        BossBarSettings bossBar = options.getBossBarSettings();
        assertTrue(bossBar.isEnabled());
        assertEquals(BossBarSettings.ShowTo.RADIUS, bossBar.getShowTo());
        assertEquals(BossBar.Color.GREEN, bossBar.colorFor(100));
        assertEquals(3, bossBar.getBrokenLinger());

        CountdownSettings countdown = options.getCountdownSettings();
        assertTrue(countdown.isEnabled());
        assertEquals(List.of(300, 60, 10), countdown.getWarnAt());
        assertEquals("BLOCK_NOTE_BLOCK_PLING", countdown.getSound());

        StorageSettings storage = new StorageSettings(yaml.getConfigurationSection("storage"));
        assertEquals(StorageSettings.Type.YAML, storage.getType());
        assertEquals("survival", storage.getServerId());
        assertFalse(storage.isCrossServerLeaderboard());
        assertEquals(30, storage.getFlushInterval());
        assertEquals("menhir_", storage.getMysql().tablePrefix());

        LangConfig lang = new LangConfig(yaml.getConfigurationSection("lang"));
        assertTrue(lang.getCountdownChat().contains("%time%"));
        assertTrue(yaml.contains("blocks.example.display-name"));
    }

    @Test
    @DisplayName("a config without the new sections still yields the safe defaults")
    void missingSectionsAreSafe() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new java.io.StringReader("""
                options:
                  block-break-limit: 20
                blocks: {}
                """));
        OptionsConfig options = new OptionsConfig(yaml.getConfigurationSection("options"), LOGGER);
        assertTrue(options.getBossBarSettings().isEnabled());
        assertTrue(options.getCountdownSettings().isEnabled());
        assertEquals(StorageSettings.Type.YAML, new StorageSettings(yaml.getConfigurationSection("storage")).getType());
        assertEquals(StorageSettings.Type.YAML, new StorageSettings(null).getType());
        assertTrue(new LangConfig(null).getCountdownChat().contains("%time%"));
    }

}
