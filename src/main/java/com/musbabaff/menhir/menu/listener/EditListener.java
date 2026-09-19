/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.listener;

import com.musbabaff.menhir.MenhirPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class EditListener implements Listener {

    private final MenhirPlugin plugin;
    private final Map<UUID, CompletableFuture<String>> chatInputFutures = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Location>> locationFutures = new ConcurrentHashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        UUID player = e.getPlayer().getUniqueId();
        CompletableFuture<Location> future = locationFutures.remove(player);
        if (future != null && !future.isDone()) {
            e.setCancelled(true);
            future.complete(e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        UUID player = e.getPlayer().getUniqueId();
        CompletableFuture<String> future = chatInputFutures.remove(player);
        if (future != null && !future.isDone()) {
            e.setCancelled(true);
            future.complete(LegacyComponentSerializer.legacyAmpersand().serialize(e.message()));
        }
    }

    public CompletableFuture<String> awaitChatInput(Player player) {
        return chatInputFutures.compute(player.getUniqueId(), (uuid, future) -> {
            if (future != null && !future.isDone()) return future;
            return new CompletableFuture<String>()
                    .orTimeout(30, TimeUnit.SECONDS);
        });
    }

    public CompletableFuture<Location> awaitLocationSelection(Player player) {
        return locationFutures.compute(player.getUniqueId(), (uuid, future) -> {
            if (future != null && !future.isDone()) return future;
            return new CompletableFuture<Location>()
                    .orTimeout(30, TimeUnit.SECONDS);
        });
    }

    public void removeChat(Player player) {
        chatInputFutures.remove(player.getUniqueId());
    }

    public void removeLocation(Player player) {
        locationFutures.remove(player.getUniqueId());
    }

}
