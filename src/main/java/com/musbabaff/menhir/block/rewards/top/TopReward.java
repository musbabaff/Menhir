/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.rewards.top;

import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.block.rewards.Reward;
import com.musbabaff.menhir.block.rewards.RewardType;
import com.musbabaff.menhir.block.rewards.commands.RewardCommands;
import com.musbabaff.menhir.block.rewards.commands.RewardEntry;
import com.musbabaff.menhir.block.rewards.context.RewardContext;
import com.musbabaff.menhir.util.range.NumberRange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TopReward implements Reward {

    private final String name;
    private final NumberRange range;
    private final RewardCommands<? extends RewardEntry> commands;

    @Override
    public RewardType getType() {
        return RewardType.TOP;
    }

    @Override
    public boolean canGet(PlayerData player, RewardContext context) {
        return range.test(context.getPosition(player.getUuid()));
    }

    @Override
    public boolean isLast() {
        return true;
    }

}
