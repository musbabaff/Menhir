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
import com.musbabaff.menhir.gui.item.AbstractItem;

import java.util.regex.Pattern;

public abstract class BlockMenuItem extends AbstractItem<MenhirBlock> {

    protected static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");

    private final BlockMenu<?> blockMenu;

    public BlockMenuItem(BlockMenu<?> editMenu) {
        super(editMenu.getFiller(), editMenu.getBlock());
        setStateUpdater(editMenu.getStateUpdater());
        this.blockMenu = editMenu;
    }

    protected BlockMenu<?> getMenu() {
        return blockMenu;
    }

}
