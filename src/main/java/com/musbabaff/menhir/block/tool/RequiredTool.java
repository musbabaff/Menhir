/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.tool;

import com.musbabaff.menhir.block.tool.enchantment.ToolEnchantment;
import com.musbabaff.menhir.block.tool.material.MaterialFilter;
import com.musbabaff.menhir.block.tool.name.NameFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
@Setter
@AllArgsConstructor
public class RequiredTool implements Predicate<ItemStack> {

    private final List<MaterialFilter> materialFilters;
    private Result materialDefault;
    private final Map<Enchantment, ToolEnchantment> enchantmentFilters;
    private Result enchantmentDefault;
    private final List<NameFilter> nameFilters;
    private Result nameDefault;

    @Override
    public boolean test(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (!NameFilter.matches(
                Optional.ofNullable(meta).map(ItemMeta::displayName).orElse(null),
                nameFilters,
                nameDefault
        ).getBooleanValue()) {
            return false;
        }
        if (!ToolEnchantment.matches(
                Optional.ofNullable(meta).map(ItemMeta::getEnchants).orElseGet(Map::of),
                enchantmentFilters,
                enchantmentDefault
        ).getBooleanValue()) {
            return false;
        }
        if (!MaterialFilter.matches(
                itemStack.getType(),
                materialFilters,
                materialDefault
        ).getBooleanValue()) {
            return false;
        }
        return true;
    }

}
