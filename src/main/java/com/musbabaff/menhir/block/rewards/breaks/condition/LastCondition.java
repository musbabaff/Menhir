/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.rewards.breaks.condition;

import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.block.rewards.context.RewardContext;

public class LastCondition implements BreakCondition {

    @Override
    public boolean test(PlayerData player, RewardContext context) {
        return player.getUuid().equals(context.getLastBreaker());
    }

    @Override
    public boolean isLast() {
        return true;
    }

    @Override
    public String toString() {
        return "last";
    }

}
