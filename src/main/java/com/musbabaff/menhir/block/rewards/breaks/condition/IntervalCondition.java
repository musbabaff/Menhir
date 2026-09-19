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
import com.musbabaff.menhir.util.NumberUtil;

public class IntervalCondition implements BreakCondition {

    private int value;

    public IntervalCondition(String value) {
        this.value = NumberUtil.parseInt(value)
                .orElseThrow(() -> new IllegalArgumentException("Invalid break interval: " + value));
    }

    public IntervalCondition(int value) {
        this.value = value;
    }

    @Override
    public boolean test(PlayerData player, RewardContext context) {
        int count = player.getBreaks();
        return count % value == 0 && count >= value;
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
