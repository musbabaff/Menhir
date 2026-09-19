/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.config.blocks.BlocksConfig;
import org.bukkit.Location;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BlockRegistry {

    private final Map<String, MenhirBlock> blockMap = new ConcurrentHashMap<>();
    private final Map<Location, MenhirBlock> blockByLocation = new ConcurrentHashMap<>();

    public void changeId(MenhirBlock mineBlock, String id) {
        BlocksConfig config = mineBlock.getPlugin().getConfiguration().getBlocksConfig();
        config.removeBlock(mineBlock.getId());
        mineBlock.hide();
        blockMap.remove(mineBlock.getId(), mineBlock);
        mineBlock.getPlugin().getStorage().renameBlock(mineBlock.getId(), id);
        mineBlock.setId(id);
        blockMap.put(mineBlock.getId(), mineBlock);
        mineBlock.show();
        mineBlock.getPlugin().saveConfiguration();
    }

    public void changeLocation(MenhirBlock mineBlock, Location location) {
        mineBlock.hide();
        blockByLocation.remove(mineBlock.getLocation(), mineBlock);
        mineBlock.teleport(location);
        blockByLocation.put(mineBlock.getLocation(), mineBlock);
        mineBlock.show();
    }

    public void register(MenhirBlock mineBlock) {
        blockMap.put(mineBlock.getId(), mineBlock);
        blockByLocation.put(mineBlock.getLocation(), mineBlock);
        mineBlock.show();
    }

    public void unregister(MenhirBlock mineBlock) {
        blockMap.remove(mineBlock.getId(), mineBlock);
        blockByLocation.remove(mineBlock.getLocation(), mineBlock);
        mineBlock.destroy();
    }

    public List<MenhirBlock> unregisterAll(Consumer<MenhirBlock> beforeUnload) {
        List<MenhirBlock> blocks = new LinkedList<>(blockMap.values());
        for (MenhirBlock block : blocks) {
            beforeUnload.accept(block);
            unregister(block);
        }
        return blocks;
    }

    public void delete(MenhirBlock block) {
        MenhirPlugin plugin = block.getPlugin();
        plugin.getConfiguration().getBlocksConfig().removeBlock(block.getId());
        plugin.saveConfiguration();
        unregister(block);
        plugin.getStorage().deleteBlock(block.getId());
    }

    public MenhirBlock get(String id) {
        return blockMap.get(id);
    }

    public MenhirBlock get(Location location) {
        return blockByLocation.get(location);
    }

    public Collection<MenhirBlock> getBlocks() {
        return blockMap.values();
    }

}
