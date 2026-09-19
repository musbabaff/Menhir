/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool.materials.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.tool.materials.MaterialFilterMenu;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class AddMaterialFilter extends BlockMenuItem {

    private MaterialFilterMenu menu;

    public AddMaterialFilter(MaterialFilterMenu editMenu) {
        super(editMenu);
        this.menu = editMenu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {}

    @Override
    public ItemStack render(MenhirBlock state) {
        return ItemStackBuilder.create("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWEyZDg5MWM2YWU5ZjZiYWEwNDBkNzM2YWI4NGQ0ODM0NGJiNmI3MGQ3ZjFhMjgwZGQxMmNiYWM0ZDc3NyJ9fX0=")
                .withName(MineDown.parse("&#205295&Add type filter"))
                .withLore(
                        Component.empty(),
                        MineDown.parse("&7Click on item in your"),
                        MineDown.parse("&7inventory to create new"),
                        MineDown.parse("&7type filter")
                )
                .build();
    }
}
