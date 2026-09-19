/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.rewards.commands;

import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.block.rewards.commands.batch.BatchRewardCommands;
import com.musbabaff.menhir.block.rewards.commands.random.RandomRewardCommands;
import com.musbabaff.menhir.block.rewards.context.RewardContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface RewardCommands<T extends RewardEntry> {

    static RewardCommands<? extends RewardEntry> parse(@Nullable String mode, List<String> commands) {
        switch (Optional.ofNullable(mode).map(String::toLowerCase).orElse("")) {
            case "all":
                return new BatchRewardCommands(commands);
            default:
                return new RandomRewardCommands(commands);
        }
    }

    List<T> asList();

    List<String> saveToList();

    void addCommand(T command);

    void removeCommand(RewardEntry command);

    List<String> rewardPlayer(PlayerData player, RewardContext random);

    String getModeName();

}