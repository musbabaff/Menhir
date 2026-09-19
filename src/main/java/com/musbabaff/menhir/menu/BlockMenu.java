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
import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.filler.GuiFiller;
import com.musbabaff.menhir.gui.item.GuiItem;
import com.musbabaff.menhir.gui.item.state.updater.StateUpdater;
import com.musbabaff.menhir.gui.item.state.updater.StateUpdaterBuilder;
import com.musbabaff.menhir.gui.meta.GuiMeta;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.util.color.Colors;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.Executor;

public abstract class BlockMenu<T extends GuiFiller<T>> extends Gui<T> {

    private final MenhirBlock block;
    private final StateUpdater<MenhirBlock> stateUpdater = StateUpdaterBuilder.sameStateUpdater(MenhirBlock.class)
            .withRefreshRate(Duration.ofSeconds(1))
            .build();

    public BlockMenu(GuiMeta<T> meta, Executor executor, MenhirBlock block) {
        super(meta, executor);
        this.block = block;
    }

    public BlockMenu(GuiMeta<T> meta, MenhirBlock block) {
        super(meta);
        this.block = block;
    }

    public BlockMenu(T filler, Component title, InventoryType type, MenhirBlock block) {
        super(filler, title, type);
        this.block = block;
    }

    public MenhirBlock getBlock() {
        return block;
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

}
