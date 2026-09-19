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
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class BreakMessageItem extends BlockMenuItem {
    public BreakMessageItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        Player player = itemClickEvent.getPlayer();
        player.closeInventory();
        MenhirBlock block = getState();
        Colors.send(player, "#2C74B3Enter new break message into chat. You can use \\n to indicate new line. Enter none to remove the message");
        block.getPlugin().getEditValuesListener().awaitChatInput(player)
                .exceptionally(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        Colors.send(player, "#DF2E38You took too long to enter new message!");
                    } else {
                        Colors.send(player, "#DF2E38An error occurred");
                        throwable.printStackTrace();
                    }
                    return null;
                })
                .thenAccept(s -> Gui.runSync(() -> {
                    if (s == null) {
                        getMenu().open(player);
                        return;
                    }
                    if ("none".equalsIgnoreCase(s)) {
                        getState().getMessages().setBreakMessage(null);
                    } else {
                        getState().getMessages().setBreakMessage(s.replace("\\n", "\n"));
                    }
                    getMenu().saveAndUpdate();
                    getMenu().open(player);
                }));
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        List<Component> lore = new LinkedList<>();
        
        lore.add(Component.empty());
        lore.add(MineDown.parse("&7Current message:"));
        
        String message = state.getMessages().getBreakMessage();

        if (message == null || message.isBlank()) {
            lore.add(MineDown.parse("&#2C74B3& There is no message"));
        } else {
            for (String s : message.split("\n")) {
                lore.add(MineDown.parse("&8- ")
                        .append(Component.text(s).color(TextColor.color(44, 116, 179))));
            }
        }

        lore.add(Component.empty());
        lore.add(MineDown.parse("&7Click to change"));
        
        return ItemStackBuilder.create(Material.BOOK)
                .withName(MineDown.parse("&#205295&&lBreak message"))
                .withLore(lore)
                .build();
    }
}
