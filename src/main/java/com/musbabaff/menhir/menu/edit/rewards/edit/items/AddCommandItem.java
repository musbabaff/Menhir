/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.rewards.edit.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.rewards.commands.RewardCommands;
import com.musbabaff.menhir.block.rewards.commands.RewardEntry;
import com.musbabaff.menhir.block.rewards.commands.batch.BatchCommandEntry;
import com.musbabaff.menhir.block.rewards.commands.batch.BatchRewardCommands;
import com.musbabaff.menhir.block.rewards.commands.random.RandomCommandEntry;
import com.musbabaff.menhir.block.rewards.commands.random.RandomRewardCommands;
import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.rewards.edit.RewardEditMenu;
import com.musbabaff.menhir.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeoutException;

public class AddCommandItem extends BlockMenuItem {

    private final RewardEditMenu menu;

    public AddCommandItem(RewardEditMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> event) {
        Player player = event.getPlayer();
        player.closeInventory();
        Colors.send(player, "#2C74B3Enter new reward command into chat.");
        getState().getPlugin().getEditValuesListener().awaitChatInput(player)
                .exceptionally(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        Colors.send(player, "#DF2E38You took too long to enter name chance!");
                    } else {
                        Colors.send(player, "#DF2E38An error occurred");
                        throwable.printStackTrace();
                    }
                    return null;
                })
                .thenAccept(s -> Gui.runSync(() -> {
                    if (s != null) {
                        RewardCommands<? extends RewardEntry> commands = menu.getReward().getCommands();
                        if (commands instanceof RandomRewardCommands) {
                            ((RandomRewardCommands) commands).addCommand(new RandomCommandEntry(s, 100));
                        } else if (commands instanceof BatchRewardCommands) {
                            ((BatchRewardCommands) commands).addCommand(new BatchCommandEntry(s));
                        } else Colors.send(player, "#DF2E38Could not create command, because target reward mode is not supported!");
                        menu.saveAndUpdate();
                        menu.open(player);
                    }
                    getMenu().open(player);
                }));
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        return ItemStackBuilder.create("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWEyZDg5MWM2YWU5ZjZiYWEwNDBkNzM2YWI4NGQ0ODM0NGJiNmI3MGQ3ZjFhMjgwZGQxMmNiYWM0ZDc3NyJ9fX0=")
                .withName(MineDown.parse("&#205295&Add command"))
                .build();
    }

}
