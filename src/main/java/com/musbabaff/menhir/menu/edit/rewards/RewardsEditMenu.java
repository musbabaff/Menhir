/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.rewards;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.rewards.Reward;
import com.musbabaff.menhir.gui.filler.map.MapGuiFiller;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.menu.PageableBlockMenu;
import com.musbabaff.menhir.menu.edit.EditMenu;
import com.musbabaff.menhir.menu.edit.rewards.items.CreateBreakCountReward;
import com.musbabaff.menhir.menu.edit.rewards.items.CreateBreakReward;
import com.musbabaff.menhir.menu.edit.rewards.items.CreateTopReward;
import com.musbabaff.menhir.menu.edit.rewards.items.RewardItem;
import com.musbabaff.menhir.menu.general.BackItem;
import com.musbabaff.menhir.menu.general.LearnMoreItem;
import com.musbabaff.menhir.menu.general.NextPageItem;
import com.musbabaff.menhir.menu.general.PreviousPageItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class RewardsEditMenu extends PageableBlockMenu<MapGuiFiller> {

    public RewardsEditMenu(MenhirBlock block) {
        super(new MapGuiFiller(
                "aaaaaaaaa",
                "aaaaaaaaa",
                "h ptbcf x"
        ), Component.text(block.getId() + " | Rewards"), InventoryType.CHEST_3, block);

        MapGuiFiller filler = getFiller();

        filler.setItem('a', new RewardItem(this));

        filler.setItem('p', new PreviousPageItem(this));
        filler.setItem('t', new CreateTopReward(this));
        filler.setItem('b', new CreateBreakReward(this));
        filler.setItem('c', new CreateBreakCountReward(this));
        filler.setItem('f', new NextPageItem(this));

        filler.setItem('h', new LearnMoreItem(filler));
        filler.setItem('x', new BackItem(filler, () -> new EditMenu(getBlock())));
    }

    @Override
    public int getPageSize() {
        return 18;
    }

    public List<Reward> getEntries() {
        List<Reward> rewards = new ArrayList<>();
        rewards.addAll(getBlock().getRewards().getLastRewards());
        rewards.addAll(getBlock().getRewards().getRewards());
        return rewards;
    }

    public void removeReward(Reward reward) {
        getBlock().getRewards().getLastRewards().remove(reward);
        getBlock().getRewards().getRewards().remove(reward);
        saveAndUpdate();
    }

    public String getRewardName(String type) {
        Set<String> names = getEntries().stream().map(Reward::getName).collect(Collectors.toUnmodifiableSet());
        for (int i = 0; i < 1000; i++) {
            String n = i > 0 ? type + i : type;
            if (!names.contains(n)) return n;
        }
        throw new RuntimeException("Name for new reward could not be created");
    }

    public void addReward(Reward reward) {
        if (reward.isLast()) {
            getBlock().getRewards().getLastRewards().add(reward);
        } else getBlock().getRewards().getRewards().add(reward);
    }

}
