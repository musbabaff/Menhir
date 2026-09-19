/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.afk;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.config.afk.AfkSettings;
import com.musbabaff.menhir.util.color.Colors;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Built-in AFK protection: tracks player movement and tells the break listener whether a hit
 * should be ignored. Replaces the Essentials / CMI / AFKPlus / UltraAFK integrations of MineBlocks.
 */
public final class AfkService implements Listener {

    private final MenhirPlugin plugin;
    private final AfkTracker tracker = new AfkTracker();

    public AfkService(MenhirPlugin plugin) {
        this.plugin = plugin;
    }

    /** Registers the listener and treats every online player as active right now. */
    public void start() {
        long now = System.currentTimeMillis();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            tracker.recordMovement(player.getUniqueId(), now);
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void stop() {
        HandlerList.unregisterAll(this);
        tracker.clear();
    }

    public AfkTracker getTracker() {
        return tracker;
    }

    /**
     * @return {@code true} if AFK protection is enabled in {@code settings} and the player is idle
     */
    public boolean isAfk(Player player, AfkSettings settings) {
        if (!settings.isEnabled()) return false;
        return tracker.isAfk(player.getUniqueId(), settings.getThresholdMillis(), System.currentTimeMillis());
    }

    /** Milliseconds since the player last changed position. */
    public long getIdleMillis(Player player) {
        return tracker.getIdleMillis(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Sends the AFK warning unless one was sent to this player within the last two seconds.
     */
    public void warn(Player player, AfkSettings settings) {
        if (!tracker.shouldWarn(player.getUniqueId(), System.currentTimeMillis())) return;
        String message = plugin.getConfiguration().getLangConfig().getStatusAFK();
        settings.getNotificationType().send(player, Colors.colorize(message));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        // Only a change of position counts; turning the head (yaw/pitch) does not.
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;
        tracker.recordMovement(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        tracker.recordMovement(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        tracker.remove(event.getPlayer().getUniqueId());
    }

}
