/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool.enchantments;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.block.tool.enchantment.ToolEnchantment;
import com.musbabaff.menhir.gui.filler.map.MapGuiFiller;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.menu.PageableBlockMenu;
import com.musbabaff.menhir.menu.edit.tool.ToolEditMenu;
import com.musbabaff.menhir.menu.edit.tool.enchantments.items.AddEnchantmentFilter;
import com.musbabaff.menhir.menu.edit.tool.enchantments.items.EnchantmentFilterItem;
import com.musbabaff.menhir.menu.general.BackItem;
import com.musbabaff.menhir.menu.general.LearnMoreItem;
import com.musbabaff.menhir.menu.general.NextPageItem;
import com.musbabaff.menhir.menu.general.PreviousPageItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class EnchantmentFilterMenu extends PageableBlockMenu<MapGuiFiller> {

    public static final List<Enchantment> ENCHANTMENTS = io.papermc.paper.registry.RegistryAccess.registryAccess()
            .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT).stream().toList();

    public EnchantmentFilterMenu(MenhirBlock block) {
        super(new MapGuiFiller(
                "aaaaaaaaa",
                "aaaaaaaaa",
                "h  pnf  x"
        ), Component.text(block.getId() + " | Enchantment filters"), InventoryType.CHEST_3, block);

        MapGuiFiller filler = getFiller();

        filler.setItem('a', new EnchantmentFilterItem(this));

        filler.setItem('p', new PreviousPageItem(this));
        filler.setItem('n', new AddEnchantmentFilter(this));
        filler.setItem('f', new NextPageItem(this));

        filler.setItem('h', new LearnMoreItem(filler));
        filler.setItem('x', new BackItem(filler, () -> new ToolEditMenu(getBlock())));
    }

    public List<Map.Entry<Enchantment, ToolEnchantment>> getEnchantments() {
        Map<Enchantment, ToolEnchantment> enchantmentMap = Optional.ofNullable(getBlock().getRequiredTool())
                        .map(RequiredTool::getEnchantmentFilters)
                        .orElse(Collections.emptyMap());
        if (enchantmentMap.isEmpty()) return Collections.emptyList();
        return ENCHANTMENTS.stream()
                .map(e -> {
                    ToolEnchantment enchantment = enchantmentMap.get(e);
                    if (enchantment == null) return null;
                    return Map.entry(e, enchantment);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public int getPageSize() {
        return 18;
    }
}
