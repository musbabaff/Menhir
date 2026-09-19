/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.config.options;

import com.musbabaff.menhir.config.afk.AfkSettings;
import com.musbabaff.menhir.config.blocks.BlocksConfig;
import com.musbabaff.menhir.config.hologram.HologramSettings;
import com.musbabaff.menhir.config.hologram.HologramTemplates;
import com.musbabaff.menhir.config.lang.LangConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads {@code examples/escraft-config.yml} (a MineBlocks-era config) through the parsers that do not
 * need a running server and checks that the legacy keys map to the new settings.
 */
class LegacyConfigCompatibilityTest {

    private static final Logger LOGGER = Logger.getLogger("menhir-test");

    private static YamlConfiguration example() {
        File file = new File("examples/escraft-config.yml");
        assertTrue(file.isFile(), "example config missing: " + file.getAbsolutePath());
        return YamlConfiguration.loadConfiguration(file);
    }

    @Test
    @DisplayName("legacy AFK keys map to options.afk")
    void legacyAfkKeys() {
        OptionsConfig options = new OptionsConfig(example().getConfigurationSection("options"), LOGGER);
        AfkSettings afk = options.getAfkSettings();
        assertTrue(afk.isEnabled());
        assertEquals(15, afk.getSeconds());
        assertEquals(NotificationType.ACTIONBAR, afk.getNotificationType());
        assertEquals(NotificationType.ACTIONBAR, options.getStatusNotificationType());
    }

    @Test
    @DisplayName("new options.afk keys override the legacy ones")
    void newAfkKeysWin() {
        ConfigurationSection section = YamlConfiguration.loadConfiguration(new StringReader("""
                afk-integration-enabled: false
                notification-type: CHAT
                afk:
                  enabled: true
                  seconds: 30
                  notification-type: TITLE
                """));
        AfkSettings afk = new OptionsConfig(section, LOGGER).getAfkSettings();
        assertTrue(afk.isEnabled());
        assertEquals(30, afk.getSeconds());
        assertEquals(NotificationType.TITLE, afk.getNotificationType());
    }

    @Test
    @DisplayName("legacy hologram-update-interval (ticks) maps to the hologram defaults")
    void legacyUpdateInterval() {
        assertEquals(HologramSettings.UPDATE_LIVE,
                new OptionsConfig(example().getConfigurationSection("options"), LOGGER)
                        .getHologramDefaults().getUpdateIntervalTicks());
        ConfigurationSection ticks = YamlConfiguration.loadConfiguration(new StringReader("hologram-update-interval: 40"));
        assertEquals(40, new OptionsConfig(ticks, LOGGER).getHologramDefaults().getUpdateIntervalTicks());
    }

    @Test
    @DisplayName("every block keeps its own hologram lines and offset")
    void blockHolograms() {
        YamlConfiguration config = example();
        ConfigurationSection blocks = config.getConfigurationSection("blocks");
        assertNotNull(blocks);
        assertEquals(Set.of("metin_1", "metin_2"), blocks.getKeys(false));
        for (String id : blocks.getKeys(false)) {
            HologramSettings settings = HologramSettings.parse(blocks.getConfigurationSection(id + ".hologram"));
            assertTrue(settings.hasLines(), id);
            assertEquals(1.2, settings.getOffsetY(), id);
            assertEquals("#ICON: %type%", settings.getLines().get(0), id);
            assertTrue(settings.getTemplate().isEmpty(), id);
        }
        assertTrue(new HologramTemplates(config.getConfigurationSection("hologram-templates"), LOGGER).isEmpty());
    }

    @Test
    @DisplayName("tool.types accepts the string form ('default: ALLOWED') and the map form")
    void toolTypes() {
        ConfigurationSection listForm = example().getConfigurationSection("blocks.metin_1.tool");
        assertEquals(Map.of("default", " ALLOWED"), BlocksConfig.readTypeEntries(listForm));

        ConfigurationSection mapForm = YamlConfiguration.loadConfiguration(new StringReader("""
                types:
                  default: DENIED
                  "STONE_.*": ALLOWED
                """));
        assertEquals(Map.of("default", "DENIED", "STONE_.*", "ALLOWED"), BlocksConfig.readTypeEntries(mapForm));

        assertNull(BlocksConfig.readTypeEntries(YamlConfiguration.loadConfiguration(new StringReader("names: {}"))));
    }

    @Test
    @DisplayName("the 2.1.0 sections of the example parse with per-block overrides")
    void newSections() {
        YamlConfiguration config = example();
        OptionsConfig options = new OptionsConfig(config.getConfigurationSection("options"), LOGGER);
        assertEquals(com.musbabaff.menhir.config.bossbar.BossBarSettings.ShowTo.RADIUS, options.getBossBarSettings().getShowTo());
        com.musbabaff.menhir.config.bossbar.BossBarSettings metin2 = options.getBossBarSettings()
                .merge(com.musbabaff.menhir.config.bossbar.BossBarSettings.parse(config.getConfigurationSection("blocks.metin_2.bossbar")));
        assertEquals(com.musbabaff.menhir.config.bossbar.BossBarSettings.ShowTo.HITTER, metin2.getShowTo());
        assertEquals(5, metin2.getHideAfter());
        com.musbabaff.menhir.config.countdown.CountdownSettings metin1 = options.getCountdownSettings()
                .merge(com.musbabaff.menhir.config.countdown.CountdownSettings.parse(config.getConfigurationSection("blocks.metin_1.respawn-countdown")));
        assertEquals(List.of(600, 300, 60, 10), metin1.getWarnAt());
        assertEquals(com.musbabaff.menhir.config.countdown.CountdownSettings.BroadcastTo.SERVER, metin1.getBroadcastTo());
        assertEquals("survival", new com.musbabaff.menhir.config.storage.StorageSettings(config.getConfigurationSection("storage")).getServerId());
        assertEquals("&6Kuzey Metin Taşı", config.getString("blocks.metin_1.display-name"));
    }

    @Test
    @DisplayName("lang keys are read")
    void lang() {
        LangConfig lang = new LangConfig(example().getConfigurationSection("lang"));
        assertEquals("&7-", lang.getNobodyName());
        assertEquals("&cYenilenme: &f%time%", lang.getTimeoutFormat());
        assertEquals(List.of("&cYenilenme: &f1 saniye"), List.of(lang.getTimeoutFormatted(999L, 0L)));
    }

}
