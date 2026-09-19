/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.rewards.commands.batch;

import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.block.rewards.commands.RewardCommands;
import com.musbabaff.menhir.block.rewards.commands.RewardEntry;
import com.musbabaff.menhir.block.rewards.context.RewardContext;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BatchRewardCommands implements RewardCommands<BatchCommandEntry> {

    static String MODE_NAME = "all";

    private final List<BatchCommandEntry> commands;


    @SneakyThrows
    public BatchRewardCommands(List<String> commands) {
        this.commands = commands.stream().map(BatchCommandEntry::new).collect(Collectors.toList());
    }

    @Override
    public List<BatchCommandEntry> asList() {
        return Collections.unmodifiableList(commands);
    }

    @Override
    public List<String> saveToList() {
        return commands.stream().map(BatchCommandEntry::getCommand).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void addCommand(BatchCommandEntry command) {
        commands.add(command);
    }

    @Override
    public void removeCommand(RewardEntry command) {
        commands.remove(command);
    }

    @Override
    public List<String> rewardPlayer(PlayerData player, RewardContext context) {
        return saveToList();
    }

    @Override
    public String getModeName() {
        return MODE_NAME;
    }

}