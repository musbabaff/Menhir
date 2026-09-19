/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.hologram;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.placeholder.BlockPlaceholderSet;
import com.musbabaff.menhir.config.hologram.HologramSettings;
import com.musbabaff.menhir.hologram.HologramEntity;
import com.musbabaff.menhir.integration.papi.PapiHook;
import com.musbabaff.menhir.text.TextRenderer;
import com.musbabaff.menhir.util.placeholders.PlaceholderSet;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The hologram of one block: its resolved settings, its editable lines and the entities showing it.
 *
 * <p>Rendering pipeline for every line: Menhir placeholders → PlaceholderAPI → {@link TextRenderer}.
 * A line whose text is empty after placeholder replacement is not shown at all (this is how the
 * {@code %timeout%} line disappears while the block is alive).</p>
 *
 * <p>Ticking happens on the main thread through {@link #tick()}; {@link #update()} may be called from
 * any thread and only marks the hologram as dirty.</p>
 */
public class BlockHologram {

    /** Prefix of a line that shows an item model instead of text, e.g. {@code #ICON: %type%}. */
    public static final String ICON_PREFIX = "#ICON:";

    private final MenhirBlock block;
    @Getter
    private HologramSettings settings;
    private final List<String> lines;
    /** True when the lines belong to this block (config or edited), false when they come from a template. */
    @Getter
    private boolean ownLines;
    /** Extra keys of the block's {@code hologram} section that Menhir does not interpret (kept on save). */
    @Getter
    private final Map<String, Object> extraConfig;
    private final PlaceholderSet placeholders;
    private final Set<String> warnedIcons = new HashSet<>();

    private HologramEntity entity;
    private volatile boolean dirty = true;
    private boolean visible;
    private int tickCounter;
    private Rendered lastRendered;

    /** Result of rendering the lines: the icon (if any) and the visible text lines. */
    public record Rendered(Material icon, List<Component> lines) {
    }

    /**
     * @param settings   fully resolved settings (defaults ← template ← block)
     * @param ownLines   whether {@code settings.getLines()} come from the block itself
     * @param extraConfig unknown keys of the block's hologram section, preserved on save
     */
    public BlockHologram(MenhirBlock block, HologramSettings settings, boolean ownLines, Map<String, Object> extraConfig) {
        this.block = block;
        this.settings = settings;
        this.lines = new LinkedList<>(settings.getLines());
        this.ownLines = ownLines;
        this.extraConfig = extraConfig == null ? Collections.emptyMap() : Map.copyOf(extraConfig);
        this.placeholders = new BlockPlaceholderSet(block);
    }

    /** Convenience constructor for blocks created in game: global defaults with the given lines. */
    public BlockHologram(MenhirBlock block, List<String> lines) {
        this(block, block.getPlugin().getConfiguration().getOptionsConfig().getHologramDefaults().withLines(lines), true, null);
    }

    public String getBlockId() {
        return block.getId();
    }

    public List<String> getLines() {
        return lines;
    }

    /** Anchor (top) location of the hologram: block centre + 1.5 + configured offset. */
    public Location getLocation() {
        return block.getLocation().clone()
                .add(.5, 1.5, .5)
                .add(settings.getOffsetX(), settings.getOffsetY(), settings.getOffsetZ());
    }

    public void setOffset(double x, double y, double z) {
        settings = settings.withOffset(x, y, z);
        updateLocation();
    }

    /** Marks the hologram for re-rendering on the next tick. Safe to call from any thread. */
    public void update() {
        dirty = true;
    }

    public void updateLocation() {
        if (entity != null && entity.isValid()) entity.move(getLocation());
    }

    public void show() {
        visible = true;
        dirty = true;
        block.getPlugin().getHologramManager().register(this);
    }

    public void hide() {
        visible = false;
        block.getPlugin().getHologramManager().unregister(this);
        if (entity != null) {
            entity.remove();
            entity = null;
        }
        lastRendered = null;
    }

    public void delete() {
        hide();
    }

    public boolean owns(UUID entityId) {
        return entity != null && entity.owns(entityId);
    }

    /**
     * One server tick: (re)spawns the entity if needed and re-renders when due and someone can see it.
     */
    public void tick() {
        if (!visible) return;
        Location anchor = getLocation();
        if (anchor.getWorld() == null || !anchor.isChunkLoaded()) return;

        if (entity == null || !entity.isValid()) {
            Rendered rendered = render();
            if (entity == null) entity = block.getPlugin().getHologramManager().newEntity(block.getId());
            entity.spawn(anchor, settings, rendered.lines(), rendered.icon());
            lastRendered = rendered;
            dirty = false;
            return;
        }

        tickCounter++;
        boolean hasIcon = lastRendered != null && lastRendered.icon() != null;
        int interval = settings.getUpdateIntervalTicks();
        boolean due = dirty || interval <= 0 || tickCounter % interval == 0;
        boolean rotate = hasIcon && tickCounter % HologramEntity.ICON_ROTATION_PERIOD == 0;
        if (!due && !rotate) return;
        if (!hasNearbyPlayers(anchor)) return;

        if (rotate) entity.rotateIcon();
        if (!due) return;

        dirty = false;
        Rendered rendered = render();
        if (!rendered.equals(lastRendered)) {
            entity.apply(rendered.lines(), rendered.icon());
            lastRendered = rendered;
        }
    }

    private boolean hasNearbyPlayers(Location anchor) {
        double range = settings.getViewDistance();
        double rangeSq = range * range;
        for (Player player : anchor.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(anchor) <= rangeSq) return true;
        }
        return false;
    }

    /** Renders the lines with all placeholders resolved. */
    public Rendered render() {
        Material icon = null;
        List<Component> rendered = new ArrayList<>(lines.size());
        for (String line : lines) {
            if (line == null) continue;
            String trimmed = line.trim();
            if (trimmed.regionMatches(true, 0, ICON_PREFIX, 0, ICON_PREFIX.length())) {
                Material material = parseIcon(trimmed.substring(ICON_PREFIX.length()).trim());
                if (material != null && icon == null) icon = material;
                continue;
            }
            String text = placeholders.parse(line);
            text = PapiHook.setPlaceholders(null, text);
            Component component = TextRenderer.render(text);
            if (!line.isBlank() && TextRenderer.plain(component).isBlank()) continue;
            rendered.add(component);
        }
        return new Rendered(icon, rendered);
    }

    /** Preview of the hologram text (without placeholders resolved against a viewer). */
    public List<Component> getAdventurePreview() {
        return render().lines();
    }

    private Material parseIcon(String value) {
        String resolved = placeholders.parse(value).trim();
        Material material = Material.matchMaterial(resolved.toUpperCase(Locale.ROOT));
        if (material != null && !material.isItem()) material = null; // e.g. WATER has no item model
        if (material == null && warnedIcons.add(resolved)) {
            MenhirPlugin plugin = block.getPlugin();
            plugin.logWarn("Block {0}: unknown or non-item icon material \"{1}\" in hologram line \"{2}\" - the line is skipped.",
                    block.getId(), resolved, ICON_PREFIX + " " + value);
        }
        return material;
    }

    public void setLine(int line, String value) {
        lines.set(line, value);
        ownLines = true;
        update();
    }

    public void removeLine(int line) {
        lines.remove(line);
        ownLines = true;
        update();
    }

    public void addLine(String value) {
        lines.add(value);
        ownLines = true;
        update();
    }

}
