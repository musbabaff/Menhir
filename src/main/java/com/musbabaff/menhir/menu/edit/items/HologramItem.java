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
import com.musbabaff.menhir.commands.MenhirCommand;
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

public class HologramItem extends BlockMenuItem {

    public HologramItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        itemClickEvent.getPlayer().closeInventory();
        MenhirCommand.showHologram(itemClickEvent.getPlayer(), getState());
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        List<Component> lore = new LinkedList<>();
        lore.add(Component.empty());

        List<Component> preview = getState().getHologram().getAdventurePreview();

        if (preview != null) {
            for (Component component : preview) {
                lore.add(MineDown.parse("&8- ").append(component));
            }
            lore.add(Component.empty());
        }

        lore.add(MineDown.parse("&7Click to edit"));
        return ItemStackBuilder.create(Material.OAK_SIGN)
                .withName(MineDown.parse("&#205295&&lHologram"))
                .withLore(lore)
                .build();
    }

}
