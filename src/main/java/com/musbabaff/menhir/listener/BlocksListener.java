/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.listener;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.config.afk.AfkSettings;
import com.musbabaff.menhir.config.lang.LangConfig;
import com.musbabaff.menhir.config.options.NotificationType;
import com.musbabaff.menhir.util.color.Colors;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Turns block break attempts on Menhir blocks into hits, after the permission / AFK / timeout / tool checks.
 */
public class BlocksListener implements Listener {

    private final MenhirPlugin plugin;
    private final Map<UUID, Long> lastBreak = new HashMap<>();

    public BlocksListener(MenhirPlugin plugin) {
        this.plugin = plugin;
    }

    private NotificationType getStatusNotificationType() {
        return plugin.getConfiguration().getOptionsConfig().getStatusNotificationType();
    }

    private int getGlobalBreakLimit() {
        return plugin.getConfiguration().getOptionsConfig().getBlockBreakLimit();
    }

    private boolean hasValidTool(MenhirBlock block, Player player) {
        RequiredTool requiredTool = block.getRequiredTool();
        if (requiredTool == null) return true;
        return requiredTool.test(player.getInventory().getItemInMainHand());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        MenhirBlock block = plugin.getBlockRegistry().get(e.getBlock().getLocation());
        if (block == null) return;

        e.setCancelled(true);

        int breakLimit = block.getBreakLimit();
        if (breakLimit < 0) {
            breakLimit = getGlobalBreakLimit();
        }
        if (breakLimit > 0) {
            long last = lastBreak.getOrDefault(player.getUniqueId(), 0L);
            long curr = System.currentTimeMillis();
            if (last + breakLimit > curr) return;
            lastBreak.put(player.getUniqueId(), curr);
        }

        LangConfig lang = plugin.getConfiguration().getLangConfig();
        String permission = block.getPermission();
        if (block.hasPermission() && !player.hasPermission(permission)) {
            deny(player, e, lang.getStatusNoPermission(), getStatusNotificationType());
            return;
        }

        AfkSettings afk = block.getAfkSettings();
        if (plugin.getAfkService().isAfk(player, afk)
                && plugin.getEvents().afkBlocked(block, player, plugin.getAfkService().getIdleMillis(player))) {
            // The hit is ignored; the warning is rate-limited by the AFK service.
            plugin.getAfkService().warn(player, afk);
            player.playSound(e.getBlock().getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 100);
            return;
        }

        if (block.getCoolDown().isActive()) {
            deny(player, e, lang.getStatusTimeout(), getStatusNotificationType());
            return;
        }

        if (!hasValidTool(block, player)) {
            deny(player, e, lang.getStatusInvalidTool(), getStatusNotificationType());
            return;
        }

        OptionalInt damage = plugin.getEvents().damage(block, player, 1);
        if (damage.isEmpty()) return; // cancelled by a listener
        block.onBreak(player, damage.getAsInt()).run();
        plugin.getBossBarService().onHit(block, player);
    }

    private void deny(Player player, BlockBreakEvent e, String statusMessage, NotificationType type) {
        type.send(player, Colors.colorize(statusMessage));
        player.playSound(e.getBlock().getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 100);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastBreak.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfiguration().getOptionsConfig().hasOfflineRewards()) return;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<String> rewards = plugin.getOfflineRewards().getAndRemoveCommands(e.getPlayer().getUniqueId());
                if (!rewards.isEmpty()) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        CommandSender sender = plugin.getServer().getConsoleSender();
                        for (String reward : rewards) {
                            plugin.getServer().dispatchCommand(sender, reward);
                        }
                    });
                }
            } catch (IOException ex) {
                plugin.getLogger().log(Level.WARNING, "Could not read offline rewards of " + e.getPlayer().getName(), ex);
            }
        });
    }

}
