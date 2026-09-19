/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.rewards.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.rewards.Reward;
import com.musbabaff.menhir.block.rewards.commands.random.RandomRewardCommands;
import com.musbabaff.menhir.block.rewards.top.TopReward;
import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.rewards.RewardsEditMenu;
import com.musbabaff.menhir.menu.edit.rewards.edit.RewardEditMenu;
import com.musbabaff.menhir.util.color.Colors;
import com.musbabaff.menhir.util.range.NumberRange;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

public class CreateTopReward extends BlockMenuItem {

    private final RewardsEditMenu menu;

    public CreateTopReward(RewardsEditMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> event) {
        Player player = event.getPlayer();
        player.closeInventory();
        MenhirBlock block = getState();
        Colors.send(player, "#2C74B3Enter top position into chat. Enter number or interval (E.g. 1-3)");
        block.getPlugin().getEditValuesListener().awaitChatInput(player)
                .exceptionally(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        Colors.send(player, "#DF2E38You took too long to enter the position!");
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
                    NumberRange.parse(s)
                            .ifPresentOrElse(numberRange -> {
                                Reward reward = new TopReward(menu.getRewardName("top"), numberRange, new RandomRewardCommands(new LinkedList<>()));
                                menu.addReward(reward);
                                getMenu().saveAndUpdate();
                                new RewardEditMenu(block, reward).open(player);
                            }, () -> Colors.send(player, "#DF2E38Invalid position entered!"));
                }));
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        return ItemStackBuilder.create("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNjYmY5ODgzZGQzNTlmZGYyMzg1YzkwYTQ1OWQ3Mzc3NjUzODJlYzQxMTdiMDQ4OTVhYzRkYzRiNjBmYyJ9fX0=")
                .withName(MineDown.parse("&#205295&Create top reward"))
                .build();
    }

}
