/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.countdown;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Settings of the respawn countdown announcements. Fields are optional so a block section can
 * override only some keys of the global {@code options.respawn-countdown} section.
 */
public final class CountdownSettings {

    /** Who receives the announcements. */
    public enum BroadcastTo { SERVER, WORLD, RADIUS }

    /** Title options; {@code null} fields are unset. */
    public record TitleSettings(Boolean enabled, Boolean onlyLast, Integer fadeIn, Integer stay, Integer fadeOut) {

        static final TitleSettings BUILT_IN = new TitleSettings(true, true, 10, 40, 10);
        static final TitleSettings EMPTY = new TitleSettings(null, null, null, null, null);

        static TitleSettings parse(ConfigurationSection section) {
            if (section == null) return EMPTY;
            return new TitleSettings(
                    section.contains("enabled") ? section.getBoolean("enabled") : null,
                    section.contains("only-last") ? section.getBoolean("only-last") : null,
                    section.contains("fade-in") ? section.getInt("fade-in") : null,
                    section.contains("stay") ? section.getInt("stay") : null,
                    section.contains("fade-out") ? section.getInt("fade-out") : null);
        }

        TitleSettings merge(TitleSettings o) {
            if (o == null) return this;
            return new TitleSettings(pick(enabled, o.enabled), pick(onlyLast, o.onlyLast),
                    pick(fadeIn, o.fadeIn), pick(stay, o.stay), pick(fadeOut, o.fadeOut));
        }

        boolean isEmpty() {
            return enabled == null && onlyLast == null && fadeIn == null && stay == null && fadeOut == null;
        }

        public boolean isEnabled() { return pick(BUILT_IN.enabled, enabled); }
        public boolean isOnlyLast() { return pick(BUILT_IN.onlyLast, onlyLast); }
        public int getFadeIn() { return pick(BUILT_IN.fadeIn, fadeIn); }
        public int getStay() { return pick(BUILT_IN.stay, stay); }
        public int getFadeOut() { return pick(BUILT_IN.fadeOut, fadeOut); }

        void write(ConfigurationSection section) {
            if (enabled != null) section.set("enabled", enabled);
            if (onlyLast != null) section.set("only-last", onlyLast);
            if (fadeIn != null) section.set("fade-in", fadeIn);
            if (stay != null) section.set("stay", stay);
            if (fadeOut != null) section.set("fade-out", fadeOut);
        }
    }

    public static final CountdownSettings BUILT_IN = new CountdownSettings(
            true, BroadcastTo.WORLD, 100d, List.of(300, 60, 10), TitleSettings.BUILT_IN, "BLOCK_NOTE_BLOCK_PLING");

    public static final CountdownSettings EMPTY = new CountdownSettings(null, null, null, null, TitleSettings.EMPTY, null);

    private final Boolean enabled;
    private final BroadcastTo broadcastTo;
    private final Double radius;
    private final List<Integer> warnAt;
    private final TitleSettings title;
    private final String sound;

    private CountdownSettings(Boolean enabled, BroadcastTo broadcastTo, Double radius, List<Integer> warnAt,
                              TitleSettings title, String sound) {
        this.enabled = enabled;
        this.broadcastTo = broadcastTo;
        this.radius = radius;
        this.warnAt = warnAt == null ? null : sortedDistinct(warnAt);
        this.title = title == null ? TitleSettings.EMPTY : title;
        this.sound = sound;
    }

    private static List<Integer> sortedDistinct(List<Integer> values) {
        List<Integer> copy = new ArrayList<>();
        for (Integer value : values) {
            if (value != null && value > 0 && !copy.contains(value)) copy.add(value);
        }
        copy.sort(Comparator.reverseOrder());
        return List.copyOf(copy);
    }

    /** Reads a {@code respawn-countdown} section; missing keys stay unset. */
    public static CountdownSettings parse(ConfigurationSection section) {
        if (section == null) return EMPTY;
        BroadcastTo broadcastTo = null;
        String rawTarget = section.getString("broadcast-to");
        if (rawTarget != null && !rawTarget.isBlank()) {
            try {
                broadcastTo = BroadcastTo.valueOf(rawTarget.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid respawn-countdown.broadcast-to '" + rawTarget
                        + "' (allowed: SERVER, WORLD, RADIUS)");
            }
        }
        return new CountdownSettings(
                section.contains("enabled") ? section.getBoolean("enabled") : null,
                broadcastTo,
                section.contains("radius") ? section.getDouble("radius") : null,
                section.contains("warn-at") ? section.getIntegerList("warn-at") : null,
                TitleSettings.parse(section.getConfigurationSection("title")),
                section.contains("sound") ? section.getString("sound", "") : null
        );
    }

    /** Returns a copy where every field set in {@code override} replaces this instance's value. */
    public CountdownSettings merge(CountdownSettings override) {
        if (override == null) return this;
        return new CountdownSettings(
                pick(enabled, override.enabled),
                pick(broadcastTo, override.broadcastTo),
                pick(radius, override.radius),
                pick(warnAt, override.warnAt),
                title.merge(override.title),
                pick(sound, override.sound));
    }

    private static <T> T pick(T base, T override) {
        return override != null ? override : base;
    }

    public boolean isEnabled() { return pick(BUILT_IN.enabled, enabled); }
    public BroadcastTo getBroadcastTo() { return pick(BUILT_IN.broadcastTo, broadcastTo); }
    public double getRadius() { return pick(BUILT_IN.radius, radius); }
    /** Warning thresholds in seconds before the respawn, descending. */
    public List<Integer> getWarnAt() { return pick(BUILT_IN.warnAt, warnAt); }
    public TitleSettings getTitle() { return TitleSettings.BUILT_IN.merge(title); }
    /** Sound name, or an empty string for no sound. */
    public String getSound() { return pick(BUILT_IN.sound, sound); }

    /** The smallest threshold, i.e. the last warning before the respawn. */
    public int getLastThreshold() {
        List<Integer> thresholds = getWarnAt();
        return thresholds.isEmpty() ? -1 : thresholds.get(thresholds.size() - 1);
    }

    public boolean isEmpty() {
        return enabled == null && broadcastTo == null && radius == null && warnAt == null && title.isEmpty() && sound == null;
    }

    /** Writes only the explicitly set fields into the section. */
    public void write(ConfigurationSection section) {
        if (enabled != null) section.set("enabled", enabled);
        if (broadcastTo != null) section.set("broadcast-to", broadcastTo.name());
        if (radius != null) section.set("radius", radius);
        if (warnAt != null) section.set("warn-at", warnAt);
        if (!title.isEmpty()) title.write(section.createSection("title"));
        if (sound != null) section.set("sound", sound);
    }

}
