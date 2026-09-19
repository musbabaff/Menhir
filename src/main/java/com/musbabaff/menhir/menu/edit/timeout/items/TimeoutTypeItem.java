/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.timeout.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.BlockMenuItem;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class TimeoutTypeItem extends BlockMenuItem {

    public TimeoutTypeItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        getState().getCoolDown().setTypeOverride(null);
        getMenu().saveAndUpdate();
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        List<Component> lore = new LinkedList<>();

        lore.add(Component.empty());
        lore.add(MineDown.parse("&7Click on block in your"));
        lore.add(MineDown.parse("&7inventory to change"));

        Material override = getState().getCoolDown().getTypeOverride();

        if (override != null) {
            lore.add(Component.empty());
            lore.add(MineDown.parse("&7Click to &#DF2E38&remove"));
        }

        return ItemStackBuilder.create(override == null ? Material.STRUCTURE_VOID : override)
                .withName(MineDown.parse("&#205295&&lTimeout type"))
                .withLore(lore)
                .build();
    }

}
