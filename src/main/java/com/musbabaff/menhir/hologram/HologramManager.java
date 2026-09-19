/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.hologram;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.block.hologram.BlockHologram;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the hologram tick loop and the entity marker used to recognise stale hologram entities.
 *
 * <p>Life cycle: {@link #start()} on enable / reload, {@link #stop()} on disable / reload.
 * Stopping removes every hologram entity, so nothing is left behind in the world.</p>
 */
public final class HologramManager implements Listener {

    /** PDC key marking Menhir hologram entities; the value is the block id. */
    public static final String MARKER_KEY = "menhir_hologram";

    private final MenhirPlugin plugin;
    private final NamespacedKey markerKey;
    private final Set<BlockHologram> holograms = ConcurrentHashMap.newKeySet();
    private BukkitTask task;

    public HologramManager(MenhirPlugin plugin) {
        this.plugin = plugin;
        this.markerKey = new NamespacedKey(plugin, MARKER_KEY);
    }

    public NamespacedKey getMarkerKey() {
        return markerKey;
    }

    /** Registers the listener, removes leftover entities in loaded chunks and starts ticking. */
    public void start() {
        if (task != null) return;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        sweepLoadedChunks();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    /** Stops ticking and removes every hologram entity. */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        HandlerList.unregisterAll(this);
        for (BlockHologram hologram : holograms) hologram.hide();
        holograms.clear();
        sweepLoadedChunks();
    }

    public void register(BlockHologram hologram) {
        holograms.add(hologram);
    }

    public void unregister(BlockHologram hologram) {
        holograms.remove(hologram);
    }

    /** Creates the entity wrapper for a block; the wrapper spawns nothing until asked to. */
    public HologramEntity newEntity(String blockId) {
        return new HologramEntity(markerKey, blockId);
    }

    private void tick() {
        for (BlockHologram hologram : holograms) {
            try {
                hologram.tick();
            } catch (Exception e) {
                plugin.getLogger().warning("Hologram of block " + hologram.getBlockId() + " failed to update: " + e);
            }
        }
    }

    private boolean isOwned(UUID entityId) {
        for (BlockHologram hologram : holograms) {
            if (hologram.owns(entityId)) return true;
        }
        return false;
    }

    /** Removes marked entities that are not owned by a live hologram. */
    private void removeStale(Collection<? extends Entity> entities) {
        for (Entity entity : entities) {
            if (HologramEntity.isMarked(entity, markerKey) && !isOwned(entity.getUniqueId())) {
                entity.remove();
            }
        }
    }

    /** Scans all loaded chunks of all worlds for leftover hologram entities. */
    public void sweepLoadedChunks() {
        for (World world : plugin.getServer().getWorlds()) {
            removeStale(world.getEntitiesByClass(Display.class));
        }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        removeStale(event.getEntities());
    }

}
