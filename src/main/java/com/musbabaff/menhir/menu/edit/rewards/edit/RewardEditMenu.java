/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.rewards.edit;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.rewards.Reward;
import com.musbabaff.menhir.block.rewards.commands.RewardEntry;
import com.musbabaff.menhir.gui.filler.map.MapGuiFiller;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.menu.PageableBlockMenu;
import com.musbabaff.menhir.menu.edit.rewards.RewardsEditMenu;
import com.musbabaff.menhir.menu.edit.rewards.edit.items.AddCommandItem;
import com.musbabaff.menhir.menu.edit.rewards.edit.items.RewardCommandItem;
import com.musbabaff.menhir.menu.general.BackItem;
import com.musbabaff.menhir.menu.general.LearnMoreItem;
import com.musbabaff.menhir.menu.general.NextPageItem;
import com.musbabaff.menhir.menu.general.PreviousPageItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.List;

@Getter
public class RewardEditMenu extends PageableBlockMenu<MapGuiFiller> {

    private final Reward reward;

    public RewardEditMenu(MenhirBlock block, Reward reward) {
        super(new MapGuiFiller(
                "aaaaaaaaa",
                "aaaaaaaaa",
                "h  pnf  x"
        ), Component.text(block.getId() + " | Reward commands"), InventoryType.CHEST_3, block);

        this.reward = reward;

        MapGuiFiller filler = getFiller();

        filler.setItem('a', new RewardCommandItem(this));

        filler.setItem('p', new PreviousPageItem(this));
        filler.setItem('n', new AddCommandItem(this));
        filler.setItem('f', new NextPageItem(this));

        filler.setItem('h', new LearnMoreItem(filler));
        filler.setItem('x', new BackItem(filler, () -> new RewardsEditMenu(getBlock())));
    }

    @Override
    public int getPageSize() {
        return 18;
    }

    public List<? extends RewardEntry> getEntries() {
        return reward.getCommands().asList();
    }

}
