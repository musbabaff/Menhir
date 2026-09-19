/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.rewards.BlockRewards;
import com.musbabaff.menhir.gui.filler.GuiFiller;
import com.musbabaff.menhir.gui.item.GuiItem;
import com.musbabaff.menhir.gui.item.state.updater.StateUpdater;
import com.musbabaff.menhir.gui.item.state.updater.StateUpdaterBuilder;
import com.musbabaff.menhir.gui.meta.GuiMeta;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.util.color.Colors;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.concurrent.Executor;

@Getter
public abstract class PageableBlockMenu<T extends GuiFiller<T>> extends BlockMenu<T> {

    private int page = 0;
    private final StateUpdater<MenhirBlock> stateUpdater = StateUpdaterBuilder.sameStateUpdater(MenhirBlock.class)
            .withRefreshRate(Duration.ofSeconds(1))
            .build();

    public PageableBlockMenu(GuiMeta<T> meta, Executor executor, MenhirBlock block) {
        super(meta, executor, block);
    }

    public PageableBlockMenu(GuiMeta<T> meta, MenhirBlock block) {
        super(meta, block);
    }

    public PageableBlockMenu(T filler, Component title, InventoryType type, MenhirBlock block) {
        super(filler, title, type, block);
    }

    public StateUpdater<MenhirBlock> getStateUpdater() {
        return stateUpdater;
    }

    public void update() {
        for (GuiItem<?> item : getFiller().getItems()) {
            item.stateUpdated();
        }
    }

    public void save() {
        MenhirBlock block = getBlock();
        block.getPlugin().getConfiguration().getBlocksConfig().setBlock(block);
        block.getPlugin().saveConfiguration();
    }

    public void saveAndUpdate() {
        update();
        save();
    }

    public void remove() {
        MenhirBlock block = getBlock();
        block.getPlugin().getBlockRegistry().delete(block);
        for (Player viewer : getViewers()) {
            Colors.send(viewer, "#2C74B3Block " + block.getId() + " was successfully deleted!");
            viewer.closeInventory();
        }
    }

    public abstract int getPageSize();

    public int getPage(int index) {
        return (int) (Math.ceil(index / ((double) getPageSize())) - 1);
    }

    public int getMaxPage() {
        BlockRewards rewards = getBlock().getRewards();
        double size = rewards.getRewards().size() + rewards.getLastRewards().size();
        return Math.max(((int) Math.ceil(size / getPageSize())) - 1, 0);
    }

    public boolean hasNextPage() {
        return page < getMaxPage();
    }

    public boolean hasPreviousPage() {
        return page > 0;
    }

    public void setPage(int page) {
        this.page = page;
        update();
    }



}
