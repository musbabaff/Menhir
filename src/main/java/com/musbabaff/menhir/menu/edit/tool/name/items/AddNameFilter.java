/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool.name.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.tool.Result;
import com.musbabaff.menhir.block.tool.name.NameFilter;
import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.tool.ToolEditMenu;
import com.musbabaff.menhir.menu.edit.tool.name.NameFilterMenu;
import com.musbabaff.menhir.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeoutException;

public class AddNameFilter extends BlockMenuItem {

    private NameFilterMenu menu;

    public AddNameFilter(NameFilterMenu editMenu) {
        super(editMenu);
        this.menu = editMenu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        Player player = itemClickEvent.getPlayer();
        player.closeInventory();
        Colors.send(player, "#2C74B3Enter new name filter into chat. Learn more on our wiki");
        menu.getBlock().getPlugin().getEditValuesListener().awaitChatInput(player)
                .exceptionally(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        Colors.send(player, "#DF2E38You took too long to enter name filter!");
                    } else {
                        Colors.send(player, "#DF2E38An error occurred");
                        throwable.printStackTrace();
                    }
                    return null;
                })
                .thenAccept(s -> Gui.runSync(() -> {
                    if (s != null) {
                        ToolEditMenu.setToolIfNotPresent(getState())
                                .getNameFilters().add(new NameFilter(s, Result.ALLOWED));
                        menu.saveAndUpdate();
                        menu.setPage(menu.getMaxPage());
                    }
                    getMenu().open(player);
                }));
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        return ItemStackBuilder.create("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWEyZDg5MWM2YWU5ZjZiYWEwNDBkNzM2YWI4NGQ0ODM0NGJiNmI3MGQ3ZjFhMjgwZGQxMmNiYWM0ZDc3NyJ9fX0=")
                .withName(MineDown.parse("&#205295&Create name filter"))
                .build();
    }
}
