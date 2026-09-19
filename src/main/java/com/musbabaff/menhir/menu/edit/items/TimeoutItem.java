/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.timeout.TimeoutMenu;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TimeoutItem extends BlockMenuItem {

    public TimeoutItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        new TimeoutMenu(getState()).open(itemClickEvent.getPlayer());
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        int timeout = state.getCoolDown().getTime();
        Material type = state.getCoolDown().getTypeOverride();
        return ItemStackBuilder.create(timeout > 0 && type != null ? type : Material.CLOCK)
                .withName(MineDown.parse("&#205295&&lTimeout"))
                .withLore(
                        Component.empty(),
                        MineDown.parse("&7Timeout: &#2C74B3&" + (timeout > 0 ? timeout + "s" : "&#DF2E38&No")),
                        Component.empty(),
                        MineDown.parse("&7Click to edit")
                ).build();
    }
}
