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
import com.musbabaff.menhir.util.range.NumberRange;

import java.util.Optional;
import java.util.regex.Pattern;

public interface BreakCondition {

    Pattern COMPARATOR_PATTERN = Pattern.compile("\\D+\\d+");

    static Optional<BreakCondition> parse(String value) {
        if (COMPARATOR_PATTERN.matcher(value).matches()) {
            return Optional.of(new ComparatorCondition(value));
        } else if (value.equalsIgnoreCase("last")) {
            return Optional.of(new LastCondition());
        } else return NumberRange.parse(value)
                .map(RangeCondition::new);
    }

    boolean test(PlayerData player, RewardContext context);
    boolean isLast();

}
