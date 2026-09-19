/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage;

import com.musbabaff.menhir.api.TopEntry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Base of the providers: all reads and writes go to a {@link MemoryStore}; subclasses only decide
 * how a {@link MemoryStore.ChangeSet} is written and how the initial data is read.
 */
public abstract class AbstractStorage implements StorageProvider {

    protected final MemoryStore memory = new MemoryStore();
    protected final Logger logger;

    protected AbstractStorage(Logger logger) {
        this.logger = logger;
    }

    @Override
    public int getBreaks(String blockId, UUID player) {
        return memory.getBreaks(blockId, player);
    }

    @Override
    public void addBreaks(String blockId, UUID player, String name, int amount) {
        memory.addBreaks(blockId, player, name, amount);
    }

    @Override
    public List<TopEntry> getTop(String blockId, int limit) {
        return memory.getTop(blockId, limit);
    }

    @Override
    public List<TopEntry> getGlobalTop(int limit) {
        return memory.getGlobalTop(limit);
    }

    @Override
    public Map<UUID, TopEntry> getPlayers(String blockId) {
        return memory.getPlayers(blockId);
    }

    @Override
    public void resetBlock(String blockId) {
        memory.resetBlock(blockId);
    }

    @Override
    public void resetPlayer(UUID player) {
        memory.resetPlayer(player);
    }

    @Override
    public void resetAll() {
        memory.resetAll();
    }

    @Override
    public Optional<StoneState> getState(String blockId) {
        return memory.getState(blockId);
    }

    @Override
    public void setState(String blockId, int health, boolean broken, long respawnAt) {
        memory.setState(blockId, new StoneState(health, broken, broken ? respawnAt : 0L));
    }

    @Override
    public void deleteBlock(String blockId) {
        memory.deleteBlock(blockId);
    }

    @Override
    public void renameBlock(String oldId, String newId) {
        memory.renameBlock(oldId, newId);
    }

    @Override
    public Collection<String> getBlockIds() {
        return memory.getBlockIds();
    }

}
