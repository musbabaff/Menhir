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
import com.musbabaff.menhir.block.reset.ResetOptions;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.resetopt.ResetOptionsMenu;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ResetOptionsItem extends BlockMenuItem {
    public ResetOptionsItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        new ResetOptionsMenu(getState()).open(itemClickEvent.getPlayer());
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        ResetOptions resetOptions = state.getResetOptions();
        int inactiveReset = resetOptions.getInactiveTime();
        boolean hasInactiveReset = inactiveReset > 0;
        return ItemStackBuilder.create(Material.RED_DYE)
                .withName(MineDown.parse("&#205295&&lReset options"))
                .withLore(
                        Component.empty(),
                        MineDown.parse("&7Inactive reset: &#2C74B3&" + (hasInactiveReset ? inactiveReset + "s" : "&#DF2E38&No")),
                        MineDown.parse("&7On restart reset: &#2C74B3&" + (resetOptions.isOnRestart() ? "&#539165&Yes" : "&#DF2E38&No")),
                        Component.empty(),
                        MineDown.parse("&7Click to edit")
                ).build();
    }

}
