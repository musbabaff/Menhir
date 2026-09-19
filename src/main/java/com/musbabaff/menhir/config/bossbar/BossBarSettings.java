/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Settings of the health boss bar shown to players who hit a stone. Every field is optional so a
 * block section can override only some keys of the global {@code options.bossbar} section.
 */
public final class BossBarSettings {

    /** Who receives the bar when a stone is hit. */
    public enum ShowTo { HITTER, RADIUS, WORLD }

    /** A colour used while the health percentage is strictly above {@code above}. */
    public record ColorThreshold(int above, BossBar.Color color) {
    }

    public static final BossBarSettings BUILT_IN = new BossBarSettings(
            true, ShowTo.RADIUS, 32d, 8, 5, 1,
            BossBar.Color.RED, BossBar.Overlay.NOTCHED_20,
            List.of(new ColorThreshold(66, BossBar.Color.GREEN),
                    new ColorThreshold(33, BossBar.Color.YELLOW),
                    new ColorThreshold(0, BossBar.Color.RED)),
            "&6&l%block_name% &8| &f%health%&7/&f%max_health% &8(&e%percent%%&8)",
            "&c&l%block_name% &8| &7Yok edildi!",
            3);

    public static final BossBarSettings EMPTY = new BossBarSettings(
            null, null, null, null, null, null, null, null, null, null, null, null);

    private final Boolean enabled;
    private final ShowTo showTo;
    private final Double radius;
    private final Integer hideAfter;
    private final Integer updateInterval;
    private final Integer maxBarsPerPlayer;
    private final BossBar.Color color;
    private final BossBar.Overlay style;
    private final List<ColorThreshold> colorThresholds;
    private final String title;
    private final String brokenTitle;
    private final Integer brokenLinger;

    private BossBarSettings(Boolean enabled, ShowTo showTo, Double radius, Integer hideAfter, Integer updateInterval,
                            Integer maxBarsPerPlayer, BossBar.Color color, BossBar.Overlay style,
                            List<ColorThreshold> colorThresholds, String title, String brokenTitle, Integer brokenLinger) {
        this.enabled = enabled;
        this.showTo = showTo;
        this.radius = radius;
        this.hideAfter = hideAfter;
        this.updateInterval = updateInterval;
        this.maxBarsPerPlayer = maxBarsPerPlayer;
        this.color = color;
        this.style = style;
        this.colorThresholds = colorThresholds == null ? null : sortedCopy(colorThresholds);
        this.title = title;
        this.brokenTitle = brokenTitle;
        this.brokenLinger = brokenLinger;
    }

    private static List<ColorThreshold> sortedCopy(List<ColorThreshold> thresholds) {
        List<ColorThreshold> copy = new ArrayList<>(thresholds);
        copy.sort(Comparator.comparingInt(ColorThreshold::above).reversed());
        return List.copyOf(copy);
    }

    /** Reads a {@code bossbar} section; missing keys stay unset. */
    public static BossBarSettings parse(ConfigurationSection section) {
        if (section == null) return EMPTY;
        return new BossBarSettings(
                section.contains("enabled") ? section.getBoolean("enabled") : null,
                parseEnum(ShowTo.class, section.getString("show-to"), "show-to"),
                section.contains("radius") ? section.getDouble("radius") : null,
                section.contains("hide-after") ? section.getInt("hide-after") : null,
                section.contains("update-interval") ? section.getInt("update-interval") : null,
                section.contains("max-bars-per-player") ? section.getInt("max-bars-per-player") : null,
                parseColor(section.getString("color"), "color"),
                parseEnum(BossBar.Overlay.class, section.getString("style"), "style"),
                section.contains("color-thresholds") ? parseThresholds(section.getMapList("color-thresholds")) : null,
                section.getString("title"),
                section.getString("broken-title"),
                section.contains("broken-linger") ? section.getInt("broken-linger") : null
        );
    }

    private static List<ColorThreshold> parseThresholds(List<Map<?, ?>> entries) {
        List<ColorThreshold> thresholds = new ArrayList<>();
        for (Map<?, ?> entry : entries) {
            Object above = entry.get("above");
            Object color = entry.get("color");
            if (above == null || color == null) {
                throw new IllegalArgumentException("Each color-thresholds entry needs 'above' and 'color'");
            }
            thresholds.add(new ColorThreshold(Integer.parseInt(String.valueOf(above).trim()),
                    parseColor(String.valueOf(color), "color-thresholds.color")));
        }
        return thresholds;
    }

    private static BossBar.Color parseColor(String value, String key) {
        return parseEnum(BossBar.Color.class, value, key);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String key) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid bossbar." + key + " '" + value + "' (allowed: "
                    + java.util.Arrays.toString(type.getEnumConstants()) + ")");
        }
    }

    /** Returns a copy where every field set in {@code override} replaces this instance's value. */
    public BossBarSettings merge(BossBarSettings override) {
        if (override == null) return this;
        return new BossBarSettings(
                pick(enabled, override.enabled),
                pick(showTo, override.showTo),
                pick(radius, override.radius),
                pick(hideAfter, override.hideAfter),
                pick(updateInterval, override.updateInterval),
                pick(maxBarsPerPlayer, override.maxBarsPerPlayer),
                pick(color, override.color),
                pick(style, override.style),
                pick(colorThresholds, override.colorThresholds),
                pick(title, override.title),
                pick(brokenTitle, override.brokenTitle),
                pick(brokenLinger, override.brokenLinger)
        );
    }

    private static <T> T pick(T base, T override) {
        return override != null ? override : base;
    }

    /**
     * Colour for a health percentage: the first threshold (highest {@code above} first) that the
     * percentage is strictly above; the fixed {@link #getColor()} when none matches or no thresholds exist.
     */
    public BossBar.Color colorFor(double percent) {
        List<ColorThreshold> thresholds = getColorThresholds();
        for (ColorThreshold threshold : thresholds) {
            if (percent > threshold.above()) return threshold.color();
        }
        return getColor();
    }

    public boolean isEnabled() { return pick(BUILT_IN.enabled, enabled); }
    public ShowTo getShowTo() { return pick(BUILT_IN.showTo, showTo); }
    public double getRadius() { return pick(BUILT_IN.radius, radius); }
    public int getHideAfter() { return pick(BUILT_IN.hideAfter, hideAfter); }
    public int getUpdateInterval() { return Math.max(1, pick(BUILT_IN.updateInterval, updateInterval)); }
    public int getMaxBarsPerPlayer() { return Math.max(1, pick(BUILT_IN.maxBarsPerPlayer, maxBarsPerPlayer)); }
    public BossBar.Color getColor() { return pick(BUILT_IN.color, color); }
    public BossBar.Overlay getStyle() { return pick(BUILT_IN.style, style); }
    public List<ColorThreshold> getColorThresholds() { return pick(BUILT_IN.colorThresholds, colorThresholds); }
    public String getTitle() { return pick(BUILT_IN.title, title); }
    public String getBrokenTitle() { return pick(BUILT_IN.brokenTitle, brokenTitle); }
    public int getBrokenLinger() { return Math.max(0, pick(BUILT_IN.brokenLinger, brokenLinger)); }

    public boolean isEmpty() {
        return enabled == null && showTo == null && radius == null && hideAfter == null && updateInterval == null
                && maxBarsPerPlayer == null && color == null && style == null && colorThresholds == null
                && title == null && brokenTitle == null && brokenLinger == null;
    }

    /** Writes only the explicitly set fields into the section. */
    public void write(ConfigurationSection section) {
        if (enabled != null) section.set("enabled", enabled);
        if (showTo != null) section.set("show-to", showTo.name());
        if (radius != null) section.set("radius", radius);
        if (hideAfter != null) section.set("hide-after", hideAfter);
        if (updateInterval != null) section.set("update-interval", updateInterval);
        if (maxBarsPerPlayer != null) section.set("max-bars-per-player", maxBarsPerPlayer);
        if (color != null) section.set("color", color.name());
        if (style != null) section.set("style", style.name());
        if (colorThresholds != null) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (ColorThreshold t : colorThresholds) list.add(Map.of("above", t.above(), "color", t.color().name()));
            section.set("color-thresholds", list);
        }
        if (title != null) section.set("title", title);
        if (brokenTitle != null) section.set("broken-title", brokenTitle);
        if (brokenLinger != null) section.set("broken-linger", brokenLinger);
    }

}
