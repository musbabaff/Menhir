/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool.enchantments.select;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.menu.edit.tool.enchantments.EnchantmentFilterMenu;
import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.filler.map.MapGuiFiller;
import com.musbabaff.menhir.gui.item.GuiItemBuilder;
import com.musbabaff.menhir.gui.item.render.Renderer;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.menu.general.BackItem;
import com.musbabaff.menhir.menu.general.LearnMoreItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EnchantmentSelectMenu extends Gui<MapGuiFiller> {

    private static MapGuiFiller getEnchantmentsFiller(MenhirBlock block) {
        List<String> list = new LinkedList<>();
        InventoryType type = InventoryType.toFitChest(EnchantmentFilterMenu.ENCHANTMENTS.size() + 9 -
                Optional.ofNullable(block.getRequiredTool()).map(t -> t.getEnchantmentFilters().size()).orElse(0));
        int rows = type.getSize() / type.getRowLength();
        for (int i = 1; i < rows  ; i++) {
            list.add("aaaaaaaaa");
        }
        list.add("h       x");
        return new MapGuiFiller(list.toArray(String[]::new));
    }

    public EnchantmentSelectMenu(Gui<?> parent, MenhirBlock block, Consumer<Enchantment> callback) {
        super(getEnchantmentsFiller(block), Component.text("Enchantment selection"), InventoryType.toFitChest(EnchantmentFilterMenu.ENCHANTMENTS.size() + 9 -
                Optional.ofNullable(block.getRequiredTool()).map(t -> t.getEnchantmentFilters().size()).orElse(0)));

        MapGuiFiller filler = getFiller();

        List<Enchantment> enchantments = new LinkedList<>(EnchantmentFilterMenu.ENCHANTMENTS);
        enchantments.removeAll(Optional.of(block.getRequiredTool()).map(t -> t.getEnchantmentFilters().keySet()).orElse(Collections.emptySet()));

        filler.setItem('a', new GuiItemBuilder<>(filler, (Renderer<Consumer<Enchantment>>) (slot, state) -> {
            if (slot < enchantments.size()) {
                NamespacedKey key = enchantments.get(slot).getKey();
                return ItemStackBuilder.create(Material.ENCHANTED_BOOK)
                        .withName(
                                Component.translatable("enchantment." + key.getNamespace() + "." + key.getKey())
                                        .color(TextColor.color(154, 32, 140))
                        )
                        .build();
            }
            return null;
        })
                .withClickHandler(itemClickEvent -> {
                    int slot = itemClickEvent.getSlot();
                    if (slot < enchantments.size()) {
                        itemClickEvent.getGuiItem().getState().accept(enchantments.get(slot));
                    }
                })
                .withDefaultState(callback)
                .build());

        filler.setItem('h', new LearnMoreItem(filler));
        filler.setItem('x', new BackItem(filler, () -> parent));
    }

}
