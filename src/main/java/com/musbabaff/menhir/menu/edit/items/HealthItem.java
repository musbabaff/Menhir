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
import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.util.NumberUtil;
import com.musbabaff.menhir.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeoutException;

public class HealthItem extends BlockMenuItem {

    public HealthItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        Player player = itemClickEvent.getPlayer();
        player.closeInventory();
        MenhirBlock block = getState();
        Colors.send(player, "#2C74B3Enter new health into chat");
        block.getPlugin().getEditValuesListener().awaitChatInput(player)
                .exceptionally(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        Colors.send(player, "#DF2E38You took too long to enter new health!");
                    } else {
                        Colors.send(player, "#DF2E38An error occurred");
                        throwable.printStackTrace();
                    }
                    return null;
                })
                .thenAccept(s -> Gui.runSync(() -> {
                    getMenu().open(player);
                    if (s == null) return;
                    NumberUtil.parseInt(s).ifPresentOrElse(integer -> {
                        block.getHealth().setMaxHealth(integer);
                        getMenu().update();
                        getMenu().save();
                    }, () -> Colors.send(player, "#DF2E38This is not a valid number!"));
                }));
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        return ItemStackBuilder.create(Material.APPLE)
                .withName(MineDown.parse("&#205295&&lHealth"))
                .withLore(
                        Component.empty(),
                        MineDown.parse("&7Current health: &#2C74B3&" + state.getHealth().getMaxHealth()),
                        Component.empty(),
                        MineDown.parse("&7Click to change")
                ).build();
    }
}
