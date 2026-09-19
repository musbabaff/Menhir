/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.hologram;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Appearance settings of a hologram. Every field is optional ("unset") so that settings can be
 * layered: built-in defaults, then {@code options.hologram}, then a template, then the block itself.
 *
 * <p>Instances are immutable; {@link #merge(HologramSettings)} returns a new instance.</p>
 */
public final class HologramSettings {

    /** Fully transparent background (ARGB 0). */
    public static final int BACKGROUND_TRANSPARENT = 0;
    /** The vanilla default text display background (ARGB 0x40000000). */
    public static final int BACKGROUND_DEFAULT = 0x40000000;
    /** Value of {@code update-interval} meaning "re-check every tick, send only when the text changed". */
    public static final int UPDATE_LIVE = -1;

    /** Built-in defaults used when nothing else is configured. */
    public static final HologramSettings BUILT_IN = new HologramSettings(
            0d, 0d, 0d,
            1d,
            Display.Billboard.CENTER,
            BACKGROUND_TRANSPARENT,
            false,
            false,
            TextDisplay.TextAlignment.CENTER,
            48d,
            UPDATE_LIVE,
            1000,
            Collections.emptyList(),
            null
    );

    /** Empty settings — nothing is set. */
    public static final HologramSettings EMPTY = new HologramSettings(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

    private final Double offsetX;
    private final Double offsetY;
    private final Double offsetZ;
    private final Double scale;
    private final Display.Billboard billboard;
    private final Integer background;
    private final Boolean textShadow;
    private final Boolean seeThrough;
    private final TextDisplay.TextAlignment alignment;
    private final Double viewDistance;
    private final Integer updateIntervalTicks;
    private final Integer lineWidth;
    private final List<String> lines;
    private final String template;

    private HologramSettings(Double offsetX, Double offsetY, Double offsetZ, Double scale,
                             Display.Billboard billboard, Integer background, Boolean textShadow,
                             Boolean seeThrough, TextDisplay.TextAlignment alignment, Double viewDistance,
                             Integer updateIntervalTicks, Integer lineWidth, List<String> lines, String template) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.scale = scale;
        this.billboard = billboard;
        this.background = background;
        this.textShadow = textShadow;
        this.seeThrough = seeThrough;
        this.alignment = alignment;
        this.viewDistance = viewDistance;
        this.updateIntervalTicks = updateIntervalTicks;
        this.lineWidth = lineWidth;
        this.lines = lines == null ? null : List.copyOf(lines);
        this.template = template;
    }

    /**
     * Reads the settings present in a configuration section. Missing keys stay unset.
     *
     * @param section the {@code hologram} (or template) section, may be {@code null}
     */
    public static HologramSettings parse(ConfigurationSection section) {
        if (section == null) return EMPTY;
        Double offsetX = null, offsetY = null, offsetZ = null;
        ConfigurationSection offset = section.getConfigurationSection("offset");
        if (offset != null) {
            if (offset.contains("x")) offsetX = offset.getDouble("x");
            if (offset.contains("y")) offsetY = offset.getDouble("y");
            if (offset.contains("z")) offsetZ = offset.getDouble("z");
        }
        return new HologramSettings(
                offsetX, offsetY, offsetZ,
                section.contains("scale") ? section.getDouble("scale") : null,
                parseEnum(Display.Billboard.class, section.getString("billboard")),
                section.contains("background") ? parseBackground(section.getString("background")) : null,
                section.contains("text-shadow") ? section.getBoolean("text-shadow") : null,
                section.contains("see-through") ? section.getBoolean("see-through") : null,
                parseEnum(TextDisplay.TextAlignment.class, section.getString("alignment")),
                section.contains("view-distance") ? section.getDouble("view-distance") : null,
                section.contains("update-interval") ? secondsToTicks(section.getDouble("update-interval")) : null,
                section.contains("line-width") ? section.getInt("line-width") : null,
                section.isList("lines") ? section.getStringList("lines") : null,
                section.getString("template")
        );
    }

    /**
     * Converts an {@code update-interval} value in seconds to ticks. Values {@code <= 0} mean live updates.
     */
    public static int secondsToTicks(double seconds) {
        if (seconds <= 0) return UPDATE_LIVE;
        return Math.max(1, (int) Math.round(seconds * 20));
    }

    /**
     * Parses a background value: {@code transparent}, {@code default}, {@code #AARRGGBB} or {@code #RRGGBB}.
     *
     * @throws IllegalArgumentException if the value is not recognised
     */
    public static int parseBackground(String value) {
        if (value == null) return BACKGROUND_TRANSPARENT;
        String v = value.trim().toLowerCase(Locale.ROOT);
        if (v.isEmpty() || v.equals("transparent") || v.equals("none")) return BACKGROUND_TRANSPARENT;
        if (v.equals("default") || v.equals("vanilla")) return BACKGROUND_DEFAULT;
        if (v.startsWith("#")) v = v.substring(1);
        if (v.length() == 6) return (int) (0xFF000000L | Long.parseLong(v, 16));
        if (v.length() == 8) return (int) Long.parseLong(v, 16);
        throw new IllegalArgumentException("Invalid hologram background '" + value + "' (use transparent, default, #RRGGBB or #AARRGGBB)");
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value '" + value + "' for " + type.getSimpleName()
                    + " (allowed: " + java.util.Arrays.toString(type.getEnumConstants()) + ")");
        }
    }

    /**
     * Returns a copy where every field set in {@code override} replaces the value of this instance.
     */
    public HologramSettings merge(HologramSettings override) {
        if (override == null) return this;
        return new HologramSettings(
                pick(offsetX, override.offsetX),
                pick(offsetY, override.offsetY),
                pick(offsetZ, override.offsetZ),
                pick(scale, override.scale),
                pick(billboard, override.billboard),
                pick(background, override.background),
                pick(textShadow, override.textShadow),
                pick(seeThrough, override.seeThrough),
                pick(alignment, override.alignment),
                pick(viewDistance, override.viewDistance),
                pick(updateIntervalTicks, override.updateIntervalTicks),
                pick(lineWidth, override.lineWidth),
                pick(lines, override.lines),
                pick(template, override.template)
        );
    }

    private static <T> T pick(T base, T override) {
        return override != null ? override : base;
    }

    /** Copy with the given lines. */
    public HologramSettings withLines(List<String> newLines) {
        return new HologramSettings(offsetX, offsetY, offsetZ, scale, billboard, background, textShadow,
                seeThrough, alignment, viewDistance, updateIntervalTicks, lineWidth, newLines, template);
    }

    /** Copy with the given offset. */
    public HologramSettings withOffset(double x, double y, double z) {
        return new HologramSettings(x, y, z, scale, billboard, background, textShadow,
                seeThrough, alignment, viewDistance, updateIntervalTicks, lineWidth, lines, template);
    }

    /** Copy with the given update interval in ticks. */
    public HologramSettings withUpdateIntervalTicks(Integer ticks) {
        return new HologramSettings(offsetX, offsetY, offsetZ, scale, billboard, background, textShadow,
                seeThrough, alignment, viewDistance, ticks, lineWidth, lines, template);
    }

    // --- resolved accessors (fall back to built-in defaults so callers never see null) ---

    public double getOffsetX() { return pick(BUILT_IN.offsetX, offsetX); }
    public double getOffsetY() { return pick(BUILT_IN.offsetY, offsetY); }
    public double getOffsetZ() { return pick(BUILT_IN.offsetZ, offsetZ); }
    public double getScale() { return pick(BUILT_IN.scale, scale); }
    public Display.Billboard getBillboard() { return pick(BUILT_IN.billboard, billboard); }
    public int getBackground() { return pick(BUILT_IN.background, background); }
    public boolean isTextShadow() { return pick(BUILT_IN.textShadow, textShadow); }
    public boolean isSeeThrough() { return pick(BUILT_IN.seeThrough, seeThrough); }
    public TextDisplay.TextAlignment getAlignment() { return pick(BUILT_IN.alignment, alignment); }
    public double getViewDistance() { return pick(BUILT_IN.viewDistance, viewDistance); }
    public int getUpdateIntervalTicks() { return pick(BUILT_IN.updateIntervalTicks, updateIntervalTicks); }
    public int getLineWidth() { return pick(BUILT_IN.lineWidth, lineWidth); }
    public List<String> getLines() { return pick(BUILT_IN.lines, lines); }
    public Optional<String> getTemplate() { return Optional.ofNullable(template); }

    // --- "is set" queries, used when writing a block back to the config ---

    public boolean hasOffset() { return offsetX != null || offsetY != null || offsetZ != null; }
    public boolean hasLines() { return lines != null; }
    public boolean isUpdateIntervalSet() { return updateIntervalTicks != null; }

    /** Update interval in seconds as written in the config ({@code -1} for live). */
    public double getUpdateIntervalSeconds() {
        int ticks = getUpdateIntervalTicks();
        return ticks <= 0 ? UPDATE_LIVE : ticks / 20d;
    }

    /** Background as a config string. */
    public String getBackgroundString() {
        int bg = getBackground();
        if (bg == BACKGROUND_TRANSPARENT) return "transparent";
        if (bg == BACKGROUND_DEFAULT) return "default";
        return String.format("#%08X", bg);
    }

    /** Writes only the explicitly set fields (except offset/lines/template) into the section. */
    public void writeDisplayOptions(ConfigurationSection section) {
        if (scale != null) section.set("scale", scale);
        if (billboard != null) section.set("billboard", billboard.name());
        if (background != null) section.set("background", getBackgroundString());
        if (textShadow != null) section.set("text-shadow", textShadow);
        if (seeThrough != null) section.set("see-through", seeThrough);
        if (alignment != null) section.set("alignment", alignment.name());
        if (viewDistance != null) section.set("view-distance", viewDistance);
        if (updateIntervalTicks != null) section.set("update-interval", getUpdateIntervalSeconds());
        if (lineWidth != null) section.set("line-width", lineWidth);
    }

}
