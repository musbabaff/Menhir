/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.rewards.breaks;

import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.block.rewards.Reward;
import com.musbabaff.menhir.block.rewards.RewardType;
import com.musbabaff.menhir.block.rewards.breaks.condition.BreakCondition;
import com.musbabaff.menhir.block.rewards.commands.RewardCommands;
import com.musbabaff.menhir.block.rewards.commands.RewardEntry;
import com.musbabaff.menhir.block.rewards.context.RewardContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BreakReward implements Reward {

    private final String name;
    private final BreakCondition condition;
    private final RewardCommands<? extends RewardEntry> commands;

    @Override
    public RewardType getType() {
        return RewardType.BREAK;
    }

    @Override
    public boolean canGet(PlayerData player, RewardContext context) {
        if (condition == null) return true;
        return condition.test(player, context);
    }

    @Override
    public boolean isLast() {
        if (condition == null) return false;
        return condition.isLast();
    }

}
