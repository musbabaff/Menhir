/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.options;

import com.musbabaff.menhir.config.afk.AfkSettings;
import com.musbabaff.menhir.config.bossbar.BossBarSettings;
import com.musbabaff.menhir.config.countdown.CountdownSettings;
import com.musbabaff.menhir.config.hologram.HologramSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * The {@code options} section.
 *
 * <p>Legacy MineBlocks keys are still honoured so an existing config keeps working:</p>
 * <ul>
 *   <li>{@code afk-integration-enabled} → {@code afk.enabled}</li>
 *   <li>{@code notification-type} → {@code afk.notification-type} (and the default for other status messages)</li>
 *   <li>{@code hologram-update-interval} (ticks) → {@code hologram.update-interval} (seconds)</li>
 * </ul>
 * A deprecation warning is logged once per key when a legacy key is used.
 */
public class OptionsConfig {

    /** Legacy keys that were already reported, so each warning is printed once per server session. */
    private static final Set<String> WARNED_LEGACY_KEYS = ConcurrentHashMap.newKeySet();

    private final ConfigurationSection config;
    private final AfkSettings afkSettings;
    private final HologramSettings hologramDefaults;
    private final NotificationType statusNotificationType;
    private final BossBarSettings bossBarSettings;
    private final CountdownSettings countdownSettings;

    public OptionsConfig(ConfigurationSection config, Logger logger) {
        this.config = config != null ? config : new MemoryConfiguration();
        this.afkSettings = readAfk(logger);
        this.hologramDefaults = readHologram(logger);
        this.bossBarSettings = BossBarSettings.BUILT_IN.merge(BossBarSettings.parse(this.config.getConfigurationSection("bossbar")));
        this.countdownSettings = CountdownSettings.BUILT_IN.merge(CountdownSettings.parse(this.config.getConfigurationSection("respawn-countdown")));
        this.statusNotificationType = Optional.ofNullable(this.config.getString("notification-type"))
                .flatMap(NotificationType::getByName)
                .orElse(NotificationType.ACTIONBAR);
    }

    private AfkSettings readAfk(Logger logger) {
        AfkSettings fromSection = AfkSettings.parse(config.getConfigurationSection("afk"));
        Boolean legacyEnabled = null;
        NotificationType legacyType = null;
        if (config.contains("afk-integration-enabled")) {
            legacyEnabled = config.getBoolean("afk-integration-enabled");
            if (!config.contains("afk.enabled"))
                warnDeprecated(logger, "options.afk-integration-enabled", "options.afk.enabled");
        }
        if (config.contains("notification-type")) {
            legacyType = NotificationType.getByName(config.getString("notification-type")).orElse(null);
            if (!config.contains("afk.notification-type"))
                warnDeprecated(logger, "options.notification-type", "options.afk.notification-type");
        }
        AfkSettings legacy = new AfkSettings(legacyEnabled, null, legacyType);
        // explicit new keys win over legacy keys
        return AfkSettings.BUILT_IN.merge(legacy).merge(fromSection);
    }

    private HologramSettings readHologram(Logger logger) {
        HologramSettings fromSection = HologramSettings.parse(config.getConfigurationSection("hologram"));
        HologramSettings legacy = HologramSettings.EMPTY;
        if (config.contains("hologram-update-interval")) {
            int ticks = config.getInt("hologram-update-interval", -1);
            legacy = legacy.withUpdateIntervalTicks(ticks <= 0 ? HologramSettings.UPDATE_LIVE : ticks);
            if (!config.contains("hologram.update-interval"))
                warnDeprecated(logger, "options.hologram-update-interval (ticks)", "options.hologram.update-interval (seconds)");
        }
        return HologramSettings.BUILT_IN.merge(legacy).merge(fromSection);
    }

    private static void warnDeprecated(Logger logger, String oldKey, String newKey) {
        if (WARNED_LEGACY_KEYS.add(oldKey))
            logger.warning("Config key '" + oldKey + "' is deprecated, use '" + newKey + "' instead.");
    }

    /** Global AFK settings (already merged with built-in defaults). */
    public AfkSettings getAfkSettings() {
        return afkSettings;
    }

    /** Global respawn countdown settings (already merged with built-in defaults). */
    public CountdownSettings getCountdownSettings() {
        return countdownSettings;
    }

    /** Global boss bar settings (already merged with built-in defaults). */
    public BossBarSettings getBossBarSettings() {
        return bossBarSettings;
    }

    /** Global hologram defaults (already merged with built-in defaults). */
    public HologramSettings getHologramDefaults() {
        return hologramDefaults;
    }

    /** Notification type for non-AFK status messages (timeout, no permission, invalid tool). */
    public NotificationType getStatusNotificationType() {
        return statusNotificationType;
    }

    public int getBlockBreakLimit() {
        return config.getInt("block-break-limit", 0);
    }

    public boolean hasOfflineRewards() {
        return config.getBoolean("offline-rewards", false);
    }

}
