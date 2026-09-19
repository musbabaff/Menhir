/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.lang;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The {@code lang} section: top placeholders, timeout formatting and status messages.
 */
@Getter
public class LangConfig {

    private static final long HOUR_MS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
    private static final long MINUTE_MS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    private static final long SECOND_MS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);

    private final ConfigurationSection config;

    public LangConfig(ConfigurationSection config) {
        this.config = config != null ? config : new MemoryConfiguration();
    }

    public String getNobodyName() {
        return config.getString("top.nobody", "Nobody");
    }

    public String getNobodyBreaks() {
        return config.getString("top.nobody-breaks", "0");
    }

    public String getNotRanked() {
        return config.getString("top.not_ranked", "Not ranked");
    }

    public String getStatusTimeout() {
        return config.getString("status.timeout", "You can't destroy the block now!");
    }

    public String getStatusAFK() {
        return config.getString("status.afk", "You are AFK!");
    }

    public String getStatusNoPermission() {
        return config.getString("status.no-permission", "You don't have permission to break this block!");
    }

    public String getStatusInvalidTool() {
        return config.getString("status.invalid-tool", "You can't use this tool to break this block!");
    }

    public String getCountdownChat() {
        return config.getString("countdown.chat", "&8» &e&l%block_name% &7yeniden doğmasına &e&l%time% &7kaldı!");
    }

    public String getCountdownTitle() {
        return config.getString("countdown.title", "&e&l%time%");
    }

    public String getCountdownSubtitle() {
        return config.getString("countdown.subtitle", "&7%block_name% yeniden doğuyor");
    }

    /** Formats a duration of whole seconds with the configured units only, e.g. {@code 5 minutes}. */
    public String formatDuration(int seconds) {
        return formatUnits(seconds * 1000L);
    }

    public String getTimeoutFormat() {
        return config.getString("timeout.message", "%time%");
    }

    public String getTimeoutFormatted(Date end) {
        return getTimeoutFormatted(end.getTime(), System.currentTimeMillis());
    }

    /**
     * Formats the time remaining between {@code now} and {@code endMillis} with the configured units,
     * limited to {@code timeout.max-units} units, and inserts it into {@code timeout.message}.
     */
    public String getTimeoutFormatted(long endMillis, long now) {
        if (config.getConfigurationSection("timeout.units") == null) return "Invalid timeout configuration";
        return getTimeoutFormat().replace("%time%", formatUnits(endMillis - now + 1000));
    }

    /**
     * Splits {@code relativeMillis} into hours / minutes / seconds using the configured unit names,
     * keeping at most {@code timeout.max-units} units.
     */
    public String formatUnits(long relativeMillis) {
        ConfigurationSection unitSection = config.getConfigurationSection("timeout.units");
        List<String> units = new LinkedList<>();
        long relative = relativeMillis;
        long hours = relative / HOUR_MS;
        relative -= hours * HOUR_MS;
        if (hours > 0)
            units.add(hours <= 1 ? (hours + unit(unitSection, "hour")) : (hours + unit(unitSection, "hours")));
        long minutes = relative / MINUTE_MS;
        relative -= minutes * MINUTE_MS;
        if (minutes > 0)
            units.add(minutes <= 1 ? (minutes + unit(unitSection, "minute")) : (minutes + unit(unitSection, "minutes")));
        long seconds = relative / SECOND_MS;
        if (seconds > 0)
            units.add(seconds <= 1 ? (seconds + unit(unitSection, "second")) : (seconds + unit(unitSection, "seconds")));
        return String.join(" ", units.stream()
                .limit(config.getInt("timeout.max-units", 2))
                .toArray(String[]::new));
    }

    private static String unit(ConfigurationSection section, String key) {
        return section == null ? " " + key : section.getString(key, " " + key);
    }

}
