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
import com.musbabaff.menhir.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class LocationItem extends BlockMenuItem {

    public LocationItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        Player player = itemClickEvent.getPlayer();
        player.closeInventory();
        Colors.send(player, "#2C74B3Click on a block to move the Menhir block there!");
        MenhirBlock block = getState();
        block.getPlugin().getEditValuesListener().awaitLocationSelection(player)
                .exceptionally(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        Colors.send(player, "#DF2E38You took too long to select the location!");
                    } else {
                        Colors.send(player, "#DF2E38An error occurred");
                        throwable.printStackTrace();
                    }
                    return null;
                }).thenAccept((loc) -> Gui.runSync(() -> {
                    getMenu().open(player);
                    if (loc == null) return;
                    block.getPlugin().getBlockRegistry().changeLocation(block, loc);
                    getMenu().update();
                    getMenu().save();
                }));
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        return ItemStackBuilder.create(Material.COMPASS)
                .withName(MineDown.parse("&#205295&&lLocation"))
                .withLore(
                        Component.empty(),
                        MineDown.parse("&7Current location: &#2C74B3&" + Optional.ofNullable(state.getLocation())
                                .map(loc ->
                                        Optional.ofNullable(loc.getWorld())
                                                .map(World::getName).orElse("unknown world") + ", " +
                                                loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())
                                .orElse("&cUnknown location")),
                        Component.empty(),
                        MineDown.parse("&7Click to change")
                ).build();
    }

}
