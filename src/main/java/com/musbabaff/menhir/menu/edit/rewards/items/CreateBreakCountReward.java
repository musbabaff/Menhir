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
import com.musbabaff.menhir.block.rewards.breakcount.BreakCountReward;
import com.musbabaff.menhir.block.rewards.commands.random.RandomRewardCommands;
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

public class CreateBreakCountReward extends BlockMenuItem {

    private final RewardsEditMenu menu;

    public CreateBreakCountReward(RewardsEditMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> event) {
        Player player = event.getPlayer();
        player.closeInventory();
        MenhirBlock block = getState();
        Colors.send(player, "#2C74B3Enter break count range (E.g. 10-50) into chat. Learn more on our wiki");
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
                            .ifPresentOrElse(range -> {
                                Reward reward = new BreakCountReward(menu.getRewardName("break_count"), range, new RandomRewardCommands(new LinkedList<>()));
                                menu.addReward(reward);
                                getMenu().saveAndUpdate();
                                new RewardEditMenu(block, reward).open(player);
                            }, () -> Colors.send(player, "#DF2E38Invalid break count range. Learn more on our wiki"));
                }));
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        return ItemStackBuilder.create("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJhNmYwZTg0ZGFlZmM4YjIxYWE5OTQxNWIxNmVkNWZkYWE2ZDhkYzBjM2NkNTkxZjQ5Y2E4MzJiNTc1In19fQ==")
                .withName(MineDown.parse("&#205295&Create break count reward"))
                .build();
    }

}
