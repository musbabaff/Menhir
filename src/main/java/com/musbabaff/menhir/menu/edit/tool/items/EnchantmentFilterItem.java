/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.tool.ToolEditMenu;
import com.musbabaff.menhir.menu.edit.tool.enchantments.EnchantmentFilterMenu;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class EnchantmentFilterItem extends BlockMenuItem {
    public EnchantmentFilterItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        RequiredTool tool = ToolEditMenu.setToolIfNotPresent(getState());
        if (itemClickEvent.getType().equals(ClickType.RIGHT)) {
            tool.setEnchantmentDefault(tool.getEnchantmentDefault().getOther());
            BlockMenu<?> menu = getMenu();
            menu.saveAndUpdate();
        } else {
            new EnchantmentFilterMenu(getState()).open(itemClickEvent.getPlayer());
        }
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        Optional<RequiredTool> requiredTool = Optional.ofNullable(getState().getRequiredTool());
        int count = requiredTool.map(t -> t.getEnchantmentFilters().size()).orElse(0);
        return ItemStackBuilder.create(Material.ENCHANTED_BOOK)
                .withName(MineDown.parse("&#205295&&lEnchantment filters"))
                .withLore(
                        Component.empty(),
                        MineDown.parse("&7Default value: " +
                                (requiredTool.map(v -> v.getEnchantmentDefault().getBooleanValue()).orElse(true)
                                        ? "&#539165&Allowed" : "&#DF2E38&Denied")
                        ),
                        Component.empty(),
                        MineDown.parse("&7There "+ (count == 1 ? "is" : "are") +" &#2C74B3&" + count + " &7enchantment filters"),
                        Component.empty(),
                        MineDown.parse("&7Left click to edit"),
                        MineDown.parse("&7Right click to change default")
                ).build();
    }
}
