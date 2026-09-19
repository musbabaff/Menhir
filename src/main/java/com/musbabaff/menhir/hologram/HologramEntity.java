/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.hologram;

import com.musbabaff.menhir.config.hologram.HologramSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

/**
 * The Minecraft entities that make up one hologram: a {@link TextDisplay} for the text and an optional
 * {@link ItemDisplay} for the {@code #ICON:} line.
 *
 * <p>The anchor location is the <em>top</em> of the hologram (like DecentHolograms' "down origin"),
 * so existing offsets keep their meaning. Entities are non-persistent and carry a marker in their
 * persistent data container so leftovers can be recognised and removed.</p>
 *
 * <p>All methods must be called from the main server thread.</p>
 */
public final class HologramEntity {

    /** Height of one text line at scale 1.0 (10 px * 0.025 blocks/px). */
    static final double LINE_HEIGHT = 0.25;
    /** Vertical space reserved for the icon. */
    static final double ICON_HEIGHT = 0.6;
    /** Rendered size of the icon block model. */
    private static final float ICON_SCALE = 0.5f;
    /** Ticks between two icon rotation steps; the client interpolates in between. */
    public static final int ICON_ROTATION_PERIOD = 20;
    /** Rotation applied per step (a quarter turn → one full turn every 4 seconds). */
    private static final float ICON_ROTATION_STEP = (float) (Math.PI / 2);

    private final NamespacedKey markerKey;
    private final String blockId;

    private TextDisplay text;
    private ItemDisplay icon;
    private Location anchor;
    private HologramSettings settings;
    private int lineCount = -1;
    private Material iconMaterial;
    private float iconAngle;

    public HologramEntity(NamespacedKey markerKey, String blockId) {
        this.markerKey = markerKey;
        this.blockId = blockId;
    }

    /** Whether the entity carries the Menhir marker. */
    public static boolean isMarked(Entity entity, NamespacedKey markerKey) {
        return entity instanceof Display && entity.getPersistentDataContainer().has(markerKey, PersistentDataType.STRING);
    }

    public boolean isValid() {
        return text != null && text.isValid();
    }

    public boolean owns(UUID entityId) {
        return (text != null && text.getUniqueId().equals(entityId))
                || (icon != null && icon.getUniqueId().equals(entityId));
    }

    /**
     * Spawns the entities at the anchor with the given content. Any previous entities are removed first.
     */
    public void spawn(Location anchor, HologramSettings settings, List<Component> lines, Material iconMaterial) {
        remove();
        this.anchor = anchor.clone();
        this.settings = settings;
        this.lineCount = lines.size();
        this.iconMaterial = iconMaterial;
        this.text = anchor.getWorld().spawn(textLocation(), TextDisplay.class, display -> {
            mark(display);
            applySettings(display);
            display.text(join(lines));
        });
        if (iconMaterial != null) spawnIcon();
    }

    /** Updates the content; repositions the text if the number of lines changed. */
    public void apply(List<Component> lines, Material newIcon) {
        if (!isValid()) return;
        boolean iconChanged = newIcon != iconMaterial;
        this.iconMaterial = newIcon;
        if (iconChanged) {
            removeIcon();
            if (newIcon != null) spawnIcon();
        }
        if (lines.size() != lineCount || iconChanged) {
            lineCount = lines.size();
            text.teleport(textLocation());
        }
        text.text(join(lines));
    }

    /** Moves the hologram to a new anchor. */
    public void move(Location newAnchor) {
        this.anchor = newAnchor.clone();
        if (text != null && text.isValid()) text.teleport(textLocation());
        if (icon != null && icon.isValid()) icon.teleport(iconLocation());
    }

    /** Advances the icon rotation by one step (the client interpolates the movement). */
    public void rotateIcon() {
        if (icon == null || !icon.isValid()) return;
        iconAngle += ICON_ROTATION_STEP;
        if (iconAngle > Math.PI * 2) iconAngle -= (float) (Math.PI * 2);
        icon.setInterpolationDelay(0);
        icon.setInterpolationDuration(ICON_ROTATION_PERIOD);
        icon.setTransformation(iconTransformation(iconAngle));
    }

    public void remove() {
        if (text != null) {
            text.remove();
            text = null;
        }
        removeIcon();
        lineCount = -1;
    }

    private void removeIcon() {
        if (icon != null) {
            icon.remove();
            icon = null;
        }
    }

    private void spawnIcon() {
        this.icon = anchor.getWorld().spawn(iconLocation(), ItemDisplay.class, display -> {
            mark(display);
            display.setItemStack(new ItemStack(iconMaterial));
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            display.setBillboard(Display.Billboard.FIXED);
            display.setViewRange(viewRange());
            display.setTransformation(iconTransformation(iconAngle));
        });
    }

    private void mark(Display display) {
        display.setPersistent(false);
        display.getPersistentDataContainer().set(markerKey, PersistentDataType.STRING, blockId);
    }

    private void applySettings(TextDisplay display) {
        display.setBillboard(settings.getBillboard());
        display.setAlignment(settings.getAlignment());
        display.setShadowed(settings.isTextShadow());
        display.setSeeThrough(settings.isSeeThrough());
        display.setLineWidth(settings.getLineWidth());
        display.setViewRange(viewRange());
        display.setTeleportDuration(0);
        int background = settings.getBackground();
        if (background == HologramSettings.BACKGROUND_DEFAULT) {
            display.setDefaultBackground(true);
        } else {
            display.setDefaultBackground(false);
            display.setBackgroundColor(Color.fromARGB(background));
        }
        float scale = (float) settings.getScale();
        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(0, 0, 1, 0),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f(0, 0, 1, 0)
        ));
    }

    private float viewRange() {
        // Vanilla view range is a multiplier of 64 blocks.
        return (float) (settings.getViewDistance() / 64d);
    }

    private static Transformation iconTransformation(float angle) {
        return new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf().rotateY(angle),
                new Vector3f(ICON_SCALE, ICON_SCALE, ICON_SCALE),
                new Quaternionf()
        );
    }

    private Location textLocation() {
        double height = Math.max(lineCount, 0) * LINE_HEIGHT * settings.getScale();
        double iconSpace = iconMaterial != null ? ICON_HEIGHT : 0;
        return anchor.clone().subtract(0, iconSpace + height, 0);
    }

    private Location iconLocation() {
        return anchor.clone().subtract(0, ICON_HEIGHT / 2, 0);
    }

    private static Component join(List<Component> lines) {
        return Component.join(JoinConfiguration.newlines(), lines);
    }

}
