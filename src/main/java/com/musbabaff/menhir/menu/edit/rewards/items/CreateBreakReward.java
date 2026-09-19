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
import com.musbabaff.menhir.block.rewards.breaks.BreakReward;
import com.musbabaff.menhir.block.rewards.breaks.condition.BreakCondition;
import com.musbabaff.menhir.block.rewards.breaks.condition.IntervalCondition;
import com.musbabaff.menhir.block.rewards.commands.random.RandomRewardCommands;
import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.rewards.RewardsEditMenu;
import com.musbabaff.menhir.menu.edit.rewards.edit.RewardEditMenu;
import com.musbabaff.menhir.util.NumberUtil;
import com.musbabaff.menhir.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

public class CreateBreakReward extends BlockMenuItem {

    private final RewardsEditMenu menu;

    public CreateBreakReward(RewardsEditMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> event) {
        Player player = event.getPlayer();
        player.closeInventory();
        MenhirBlock block = getState();
        Colors.send(player, "#2C74B3Enter condition (less than 5, more than 5), interval (10, 50), range (10, 50) or last into chat. Learn more on our wiki");
        block.getPlugin().getEditValuesListener().awaitChatInput(player)
                .exceptionally(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        Colors.send(player, "#DF2E38You took too long to enter break condition/interval!");
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
                    NumberUtil.parseInt(s)
                                    .ifPresentOrElse(integer -> {
                                        Reward reward = new BreakReward(menu.getRewardName("break"), new IntervalCondition(integer), new RandomRewardCommands(new LinkedList<>()));
                                        menu.addReward(reward);
                                        getMenu().saveAndUpdate();
                                        new RewardEditMenu(block, reward).open(player);
                                    }, () -> BreakCondition.parse(s)
                                            .ifPresentOrElse(breakCondition -> {
                                                Reward reward = new BreakReward(menu.getRewardName("break"), breakCondition, new RandomRewardCommands(new LinkedList<>()));
                                                block.getRewards().getLastRewards().add(reward);
                                                getMenu().saveAndUpdate();
                                                new RewardEditMenu(block, reward).open(player);
                                            }, () -> Colors.send(player, "#DF2E38Invalid break condition/interval. Learn more on our wiki")));
                }));
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        return ItemStackBuilder.create("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UzYTU5ZTRjNTc1MzI5NWQ4ZDY5YzIxNDM0NGViYjNlNTQ3ZjkzNmI4NjdhZDlkNWViZDUxOWZhZDg1Y2UzIn19fQ==")
                .withName(MineDown.parse("&#205295&Create break reward"))
                .build();
    }

}
