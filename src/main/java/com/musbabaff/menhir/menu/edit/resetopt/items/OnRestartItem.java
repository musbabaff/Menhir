/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.resetopt.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.reset.ResetOptions;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.BlockMenuItem;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class OnRestartItem extends BlockMenuItem {
    public OnRestartItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> event) {
        ResetOptions resetOptions = getState().getResetOptions();
        resetOptions.setOnRestart(!resetOptions.isOnRestart());
        getMenu().saveAndUpdate();
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        boolean enabled = state.getResetOptions().isOnRestart();
        return ItemStackBuilder.create(enabled ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA)
                .withName(MineDown.parse("&#205295&&lOn restart reset"))
                .withLore(
                        Component.empty(),
                        MineDown.parse("&7Current value: &#2C74B3&" + (enabled ? "&#539165&Yes" : "&#DF2E38&No")),
                        Component.empty(),
                        MineDown.parse("&7Click to change")
                )
                .build();
    }
}
