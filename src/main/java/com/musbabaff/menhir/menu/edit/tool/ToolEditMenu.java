/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.block.tool.Result;
import com.musbabaff.menhir.gui.filler.map.MapGuiFiller;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.edit.EditMenu;
import com.musbabaff.menhir.menu.edit.tool.items.EnchantmentFilterItem;
import com.musbabaff.menhir.menu.edit.tool.items.MaterialFilterItem;
import com.musbabaff.menhir.menu.edit.tool.items.NameFilterItem;
import com.musbabaff.menhir.menu.general.BackItem;
import com.musbabaff.menhir.menu.general.LearnMoreItem;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.LinkedList;

public class ToolEditMenu extends BlockMenu<MapGuiFiller> {

    public static RequiredTool setToolIfNotPresent(MenhirBlock block) {
        if (block.getRequiredTool() == null)
            block.setRequiredTool(new RequiredTool(
                    new LinkedList<>(),
                    Result.ALLOWED,
                    new HashMap<>(),
                    Result.ALLOWED,
                    new LinkedList<>(),
                    Result.ALLOWED
            ));
        return block.getRequiredTool();
    }

    public ToolEditMenu(MenhirBlock block) {
        super(new MapGuiFiller(
                "         ",
                "  a b c  ",
                "h       x"
        ), Component.text(block.getId() + " | Tool requirements"), InventoryType.CHEST_3, block);

        MapGuiFiller filler = getFiller();

        filler.setItem('a', new MaterialFilterItem(this));
        filler.setItem('b', new EnchantmentFilterItem(this));
        filler.setItem('c', new NameFilterItem(this));

        filler.setItem('h', new LearnMoreItem(filler));
        filler.setItem('x', new BackItem(filler, () -> new EditMenu(getBlock())));
    }

}
