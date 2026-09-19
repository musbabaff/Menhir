/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.bossbar;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.placeholder.BlockPlaceholderSet;
import com.musbabaff.menhir.block.placeholder.ViewerPlaceholderSet;
import com.musbabaff.menhir.config.bossbar.BossBarSettings;
import com.musbabaff.menhir.integration.papi.PapiHook;
import com.musbabaff.menhir.text.TextRenderer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shows a health boss bar to players while a stone is being hit.
 *
 * <p>One {@link BossBar} exists per (viewer, stone) pair so viewer placeholders such as
 * {@code %my_rank%} can differ, while health and progress are the same for everyone. A single
 * repeating task runs only while at least one bar is visible; bars are re-rendered when the stone
 * changed, at most once per {@code update-interval} ticks, and packets are sent only when the
 * rendered title, progress or colour actually changed.</p>
 */
public final class BossBarService implements Listener {

    /** A visible bar of one viewer for one stone. */
    private static final class ActiveBar {
        final MenhirBlock block;
        final UUID viewer;
        final BossBar bar;
        long hideAt;
        boolean dirty = true;
        boolean broken;
        long lastRenderTick = -1_000_000L;
        Component lastTitle;
        float lastProgress = -1f;
        BossBar.Color lastColor;

        ActiveBar(MenhirBlock block, UUID viewer, BossBar bar) {
            this.block = block;
            this.viewer = viewer;
            this.bar = bar;
        }
    }

    private final MenhirPlugin plugin;
    /** viewer → (block id → bar), insertion ordered so the oldest bar can be evicted. */
    private final Map<UUID, LinkedHashMap<String, ActiveBar>> bars = new HashMap<>();
    private BukkitTask task;
    private long tick;

    public BossBarService(MenhirPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Hides every bar and stops the task. */
    public void stop() {
        HandlerList.unregisterAll(this);
        for (UUID viewer : new ArrayList<>(bars.keySet())) clearViewer(viewer);
        stopTask();
    }

    /** Called after a successful hit on {@code block} by {@code hitter}. */
    public void onHit(MenhirBlock block, Player hitter) {
        BossBarSettings settings = block.getBossBarSettings();
        if (!settings.isEnabled()) return;
        // The hit that broke the stone already switched the bars to the broken title.
        if (block.getCoolDown().isActive()) return;
        long now = System.currentTimeMillis();
        long hideAt = now + settings.getHideAfter() * 1000L;
        for (Player viewer : resolveViewers(block, hitter, settings)) {
            ActiveBar active = getOrCreate(block, viewer, settings);
            active.hideAt = hideAt;
            active.broken = false;
            active.dirty = true;
        }
        // Everyone already watching this stone must see the new health too.
        markDirty(block);
        ensureTask();
    }

    /** Called when {@code block} was fully broken: shows the broken title, then hides the bars. */
    public void onBroken(MenhirBlock block) {
        BossBarSettings settings = block.getBossBarSettings();
        long now = System.currentTimeMillis();
        int linger = settings.getBrokenLinger();
        for (LinkedHashMap<String, ActiveBar> viewerBars : bars.values()) {
            ActiveBar active = viewerBars.get(block.getId());
            if (active == null) continue;
            active.broken = true;
            active.dirty = true;
            active.hideAt = linger <= 0 ? now : now + linger * 1000L;
        }
        if (linger <= 0) {
            hideBlock(block);
        } else {
            renderBlock(block, true);
            ensureTask();
        }
    }

    /** Hides all bars of the block immediately (block removed, reset, ...). */
    public void hideBlock(MenhirBlock block) {
        for (Iterator<Map.Entry<UUID, LinkedHashMap<String, ActiveBar>>> it = bars.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, LinkedHashMap<String, ActiveBar>> entry = it.next();
            ActiveBar removed = entry.getValue().remove(block.getId());
            if (removed != null) hide(removed);
            if (entry.getValue().isEmpty()) it.remove();
        }
        if (bars.isEmpty()) stopTask();
    }

    private Collection<Player> resolveViewers(MenhirBlock block, Player hitter, BossBarSettings settings) {
        Location location = block.getLocation();
        World world = location.getWorld();
        if (world == null) return List.of(hitter);
        return switch (settings.getShowTo()) {
            case HITTER -> List.of(hitter);
            case WORLD -> new ArrayList<>(world.getPlayers());
            case RADIUS -> {
                double rangeSq = settings.getRadius() * settings.getRadius();
                List<Player> players = new ArrayList<>();
                for (Player player : world.getPlayers()) {
                    if (player.getLocation().distanceSquared(location) <= rangeSq) players.add(player);
                }
                if (!players.contains(hitter)) players.add(hitter);
                yield players;
            }
        };
    }

    private ActiveBar getOrCreate(MenhirBlock block, Player viewer, BossBarSettings settings) {
        LinkedHashMap<String, ActiveBar> viewerBars = bars.computeIfAbsent(viewer.getUniqueId(), k -> new LinkedHashMap<>());
        ActiveBar active = viewerBars.get(block.getId());
        if (active != null) return active;
        // Enforce max-bars-per-player: the newest stone wins, the oldest bar goes away.
        while (viewerBars.size() >= settings.getMaxBarsPerPlayer() && !viewerBars.isEmpty()) {
            String oldest = viewerBars.keySet().iterator().next();
            hide(viewerBars.remove(oldest));
        }
        BossBar bar = BossBar.bossBar(Component.empty(), progressOf(block), settings.getColor(), settings.getStyle());
        active = new ActiveBar(block, viewer.getUniqueId(), bar);
        viewerBars.put(block.getId(), active);
        render(active, true);
        viewer.showBossBar(bar);
        return active;
    }

    private void markDirty(MenhirBlock block) {
        for (LinkedHashMap<String, ActiveBar> viewerBars : bars.values()) {
            ActiveBar active = viewerBars.get(block.getId());
            if (active != null) active.dirty = true;
        }
    }

    private void renderBlock(MenhirBlock block, boolean force) {
        for (LinkedHashMap<String, ActiveBar> viewerBars : bars.values()) {
            ActiveBar active = viewerBars.get(block.getId());
            if (active != null) render(active, force);
        }
    }

    private void ensureTask() {
        if (task != null || bars.isEmpty()) return;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::onTick, 1L, 1L);
    }

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void onTick() {
        tick++;
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<UUID, LinkedHashMap<String, ActiveBar>>> it = bars.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, LinkedHashMap<String, ActiveBar>> entry = it.next();
            Player viewer = plugin.getServer().getPlayer(entry.getKey());
            for (Iterator<ActiveBar> barIt = entry.getValue().values().iterator(); barIt.hasNext(); ) {
                ActiveBar active = barIt.next();
                if (viewer == null || now >= active.hideAt) {
                    hide(active);
                    barIt.remove();
                    continue;
                }
                render(active, false);
            }
            if (entry.getValue().isEmpty()) it.remove();
        }
        if (bars.isEmpty()) stopTask();
    }

    /** Re-renders the bar when it is dirty and its update interval has elapsed; sends only real changes. */
    private void render(ActiveBar active, boolean force) {
        if (!active.dirty && !force) return;
        BossBarSettings settings = active.block.getBossBarSettings();
        if (!force && tick - active.lastRenderTick < settings.getUpdateInterval()) return;
        active.dirty = false;
        active.lastRenderTick = tick;

        MenhirBlock block = active.block;
        float progress = active.broken ? 0f : progressOf(block);
        BossBar.Color color = active.broken ? settings.getColor() : settings.colorFor(block.getHealth().getPercent());
        Component title = renderTitle(block, active.viewer, active.broken ? settings.getBrokenTitle() : settings.getTitle());

        if (!title.equals(active.lastTitle)) {
            active.bar.name(title);
            active.lastTitle = title;
        }
        if (progress != active.lastProgress) {
            active.bar.progress(progress);
            active.lastProgress = progress;
        }
        if (color != active.lastColor) {
            active.bar.color(color);
            active.lastColor = color;
        }
        if (active.bar.overlay() != settings.getStyle()) active.bar.overlay(settings.getStyle());
    }

    private Component renderTitle(MenhirBlock block, UUID viewer, String raw) {
        String text = new BlockPlaceholderSet(block).parse(raw);
        text = new ViewerPlaceholderSet(block, viewer).parse(text);
        text = PapiHook.setPlaceholders(plugin.getServer().getOfflinePlayer(viewer), text);
        return TextRenderer.render(text);
    }

    private static float progressOf(MenhirBlock block) {
        int max = Math.max(1, block.getHealth().getMaxHealth());
        return Math.max(0f, Math.min(1f, block.getHealth().getHealth() / (float) max));
    }

    private void hide(ActiveBar active) {
        Player player = plugin.getServer().getPlayer(active.viewer);
        if (player != null) player.hideBossBar(active.bar);
    }

    private void clearViewer(UUID viewer) {
        LinkedHashMap<String, ActiveBar> viewerBars = bars.remove(viewer);
        if (viewerBars == null) return;
        for (ActiveBar active : viewerBars.values()) hide(active);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearViewer(event.getPlayer().getUniqueId());
        if (bars.isEmpty()) stopTask();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        clearViewer(event.getPlayer().getUniqueId());
        if (bars.isEmpty()) stopTask();
    }

}
