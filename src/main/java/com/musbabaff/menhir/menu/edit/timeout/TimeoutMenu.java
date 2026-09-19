/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.timeout;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.gui.filler.map.MapGuiFiller;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.edit.EditMenu;
import com.musbabaff.menhir.menu.edit.timeout.items.TimeoutMessageItem;
import com.musbabaff.menhir.menu.edit.timeout.items.TimeoutTimeItem;
import com.musbabaff.menhir.menu.edit.timeout.items.TimeoutTypeItem;
import com.musbabaff.menhir.menu.general.BackItem;
import com.musbabaff.menhir.menu.general.LearnMoreItem;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TimeoutMenu extends BlockMenu<MapGuiFiller> {

    public TimeoutMenu(MenhirBlock block) {
        super(new MapGuiFiller(
                "         ",
                "  a b c  ",
                "h       x"
        ), Component.text(block.getId() + " | Timeout"), InventoryType.CHEST_3, block);

        setPlayerInventoryHandler(itemClickEvent -> {
            int slot = itemClickEvent.getSlot();
            Inventory inventory = itemClickEvent.getPlayer().getInventory();
            if (slot < 0 || slot >= inventory.getSize()) return;
            ItemStack clicked = inventory.getItem(slot);
            if (clicked == null || !clicked.getType().isBlock()) return;
            getBlock().getCoolDown().setTypeOverride(clicked.getType());
            update();
            save();
        });

        MapGuiFiller filler = getFiller();

        filler.setItem('a', new TimeoutTimeItem(this));
        filler.setItem('b', new TimeoutMessageItem(this));
        filler.setItem('c', new TimeoutTypeItem(this));

        filler.setItem('h', new LearnMoreItem(filler));
        filler.setItem('x', new BackItem(filler, () -> new EditMenu(getBlock())));
    }

}
