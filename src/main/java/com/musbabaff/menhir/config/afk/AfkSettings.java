/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.afk;

import com.musbabaff.menhir.config.options.NotificationType;
import org.bukkit.configuration.ConfigurationSection;

/**
 * AFK protection settings. Fields are optional so a block section can override only some of them.
 */
public final class AfkSettings {

    public static final AfkSettings BUILT_IN = new AfkSettings(false, 15, NotificationType.ACTIONBAR);
    public static final AfkSettings EMPTY = new AfkSettings(null, null, null);

    private final Boolean enabled;
    private final Integer seconds;
    private final NotificationType notificationType;

    public AfkSettings(Boolean enabled, Integer seconds, NotificationType notificationType) {
        this.enabled = enabled;
        this.seconds = seconds;
        this.notificationType = notificationType;
    }

    /** Reads an {@code afk} section; missing keys stay unset. */
    public static AfkSettings parse(ConfigurationSection section) {
        if (section == null) return EMPTY;
        return new AfkSettings(
                section.contains("enabled") ? section.getBoolean("enabled") : null,
                section.contains("seconds") ? section.getInt("seconds") : null,
                section.contains("notification-type")
                        ? NotificationType.getByName(section.getString("notification-type"))
                            .orElseThrow(() -> new IllegalArgumentException("Invalid afk.notification-type '"
                                    + section.getString("notification-type") + "'"))
                        : null
        );
    }

    public AfkSettings merge(AfkSettings override) {
        if (override == null) return this;
        return new AfkSettings(
                override.enabled != null ? override.enabled : enabled,
                override.seconds != null ? override.seconds : seconds,
                override.notificationType != null ? override.notificationType : notificationType
        );
    }

    public boolean isEnabled() { return enabled != null ? enabled : BUILT_IN.enabled; }
    public int getSeconds() { return seconds != null ? seconds : BUILT_IN.seconds; }
    public NotificationType getNotificationType() { return notificationType != null ? notificationType : BUILT_IN.notificationType; }
    public long getThresholdMillis() { return getSeconds() * 1000L; }

    public boolean isEmpty() { return enabled == null && seconds == null && notificationType == null; }

    public void write(ConfigurationSection section) {
        if (enabled != null) section.set("enabled", enabled);
        if (seconds != null) section.set("seconds", seconds);
        if (notificationType != null) section.set("notification-type", notificationType.name());
    }

}
