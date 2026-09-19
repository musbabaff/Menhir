/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.gui.filler.map.MapGuiFiller;
import com.musbabaff.menhir.gui.item.GuiItem;
import com.musbabaff.menhir.gui.item.GuiItemBuilder;
import com.musbabaff.menhir.gui.item.render.Renderer;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.gui.meta.GuiMeta;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.edit.items.*;
import com.musbabaff.menhir.menu.general.LearnMoreItem;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class EditMenu extends BlockMenu<MapGuiFiller> {


    public EditMenu(MenhirBlock block) {
        super(new GuiMeta<>(new MapGuiFiller(
                "         ",
                " a b c d ",
                "  e f g  ",
                " h i j k ",
                "         ",
                "l   r   o"
        ), Component.text(block.getId()), InventoryType.CHEST_6), block);
        MapGuiFiller filler = getFiller();
        setPlayerInventoryHandler(itemClickEvent -> {
            int slot = itemClickEvent.getSlot();
            Inventory inventory = itemClickEvent.getPlayer().getInventory();
            if (slot < 0 || slot >= inventory.getSize()) return;
            ItemStack clicked = inventory.getItem(slot);
            if (clicked == null || !clicked.getType().isBlock()) return;
            getBlock().getType().setType(clicked.getType());
            update();
            save();
        });
        filler.setItem('a', new RenameItem(this));
        filler.setItem('b', new LocationItem(this));
        filler.setItem('c', new TypeItem(this));
        filler.setItem('d', new HealthItem(this));

        filler.setItem('e', new HologramItem(this));
        filler.setItem('f', new ToolItem(this));
        filler.setItem('g', new RewardsItem(this));

        filler.setItem('h', new ResetOptionsItem(this));
        filler.setItem('i', new PermissionItem(this));
        filler.setItem('j', new BreakMessageItem(this));
        filler.setItem('k', new TimeoutItem(this));

        filler.setItem('l', new LearnMoreItem(filler));
        filler.setItem('r', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> {
            List<Component> lore = new LinkedList<>(List.of(
                    Component.empty()
            ));
            if (Boolean.TRUE.equals(state)) {
                lore.add(MineDown.parse("&#db464c&Click again to confirm"));
            } else {
                lore.add(MineDown.parse("&#db464c&Click to permanently"));
                lore.add(MineDown.parse("&#db464c&delete this block!"));
            }
            return ItemStackBuilder.create(Material.RED_TERRACOTTA)
                    .withName(MineDown.parse("&#DF2E38&&lREMOVE BLOCK"))
                    .withLore(lore)
                    .build();
        })
                        .withDefaultState(false)
                        .withClickHandler(itemClickEvent -> {
                            GuiItem<Boolean> guiItem = itemClickEvent.getGuiItem();
                            if (Boolean.TRUE.equals(guiItem.getState())) {
                                remove();
                            } else {
                                guiItem.setState(true);
                            }
                        })
                .build());
        filler.setItem('o', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BARRIER)
                .withName(MineDown.parse("&#DF2E38&Close"))
                .build())
                .withClickHandler(itemClickEvent -> itemClickEvent.getPlayer().closeInventory())
                .build());
    }

}
