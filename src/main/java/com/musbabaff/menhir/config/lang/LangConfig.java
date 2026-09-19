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
        ConfigurationSection unitSection = config.getConfigurationSection("timeout.units");
        if (unitSection == null) return "Invalid timeout configuration";
        List<String> units = new LinkedList<>();
        long relative = endMillis - now + 1000;
        long hours = relative / HOUR_MS;
        relative -= hours * HOUR_MS;
        if (hours > 0)
            units.add(hours <= 1 ? (hours + unitSection.getString("hour", "hour")) : (hours + unitSection.getString("hours", "hours")));
        long minutes = relative / MINUTE_MS;
        relative -= minutes * MINUTE_MS;
        if (minutes > 0)
            units.add(minutes <= 1 ? (minutes + unitSection.getString("minute", "minute")) : (minutes + unitSection.getString("minutes", "minutes")));
        long seconds = relative / SECOND_MS;
        if (seconds > 0)
            units.add(seconds <= 1 ? (seconds + unitSection.getString("second", "second")) : (seconds + unitSection.getString("seconds", "seconds")));
        return getTimeoutFormat().replace("%time%", String.join(" ", units.stream()
                .limit(config.getInt("timeout.max-units", 2))
                .toArray(String[]::new)));
    }

}
