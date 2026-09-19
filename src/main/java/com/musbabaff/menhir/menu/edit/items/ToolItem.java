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
import com.musbabaff.menhir.menu.edit.tool.ToolEditMenu;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class ToolItem extends BlockMenuItem {
    public ToolItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        new ToolEditMenu(getState()).open(itemClickEvent.getPlayer());
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        int totalRules = Optional.ofNullable(getState().getRequiredTool())
                .map(requiredTool -> requiredTool.getEnchantmentFilters().size() + requiredTool.getNameFilters().size() + requiredTool.getMaterialFilters().size())
                .orElse(0);
        return ItemStackBuilder.create(Material.IRON_PICKAXE)
                .withName(MineDown.parse("&#205295&&lTool requirements"))
                .withLore(List.of(
                        Component.empty(),
                        MineDown.parse("&7There are &#2C74B3&" + totalRules + " &7tool"),
                        MineDown.parse("&7filter rules in total"),
                        Component.empty(),
                        MineDown.parse("&7Click to edit")
                ))
                .addItemFlags(ItemFlag.values())
                .build();
    }
}
