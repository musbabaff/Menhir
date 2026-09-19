/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool.enchantments.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.tool.Result;
import com.musbabaff.menhir.block.tool.enchantment.ToolEnchantment;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.tool.ToolEditMenu;
import com.musbabaff.menhir.menu.edit.tool.enchantments.EnchantmentFilterMenu;
import com.musbabaff.menhir.menu.edit.tool.enchantments.select.EnchantmentSelectMenu;
import com.musbabaff.menhir.menu.edit.tool.enchantments.select.LevelSelectMenu;
import com.musbabaff.menhir.util.range.NumberRange;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class AddEnchantmentFilter extends BlockMenuItem {

    private EnchantmentFilterMenu menu;

    public AddEnchantmentFilter(EnchantmentFilterMenu editMenu) {
        super(editMenu);
        this.menu = editMenu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        Player player = itemClickEvent.getPlayer();
        new EnchantmentSelectMenu(getMenu(), getState(), enchantment -> selectLevels(player, enchantment, numberRange -> {
            ToolEditMenu.setToolIfNotPresent(getState())
                    .getEnchantmentFilters().put(enchantment, new ToolEnchantment(numberRange, Result.ALLOWED));
            menu.saveAndUpdate();
            menu.open(player);
        })).open(player);
    }

    private void selectLevels(Player player, Enchantment enchantment, Consumer<NumberRange> consumer) {
        if (enchantment.getMaxLevel() <= 1) {
            consumer.accept(null);
        } else new LevelSelectMenu(getMenu(), enchantment, consumer).open(player);
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        return ItemStackBuilder.create("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWEyZDg5MWM2YWU5ZjZiYWEwNDBkNzM2YWI4NGQ0ODM0NGJiNmI3MGQ3ZjFhMjgwZGQxMmNiYWM0ZDc3NyJ9fX0=")
                .withName(MineDown.parse("&#205295&Create enchantment filter"))
                .build();
    }
}
