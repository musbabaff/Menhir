/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.rewards;

import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.block.rewards.commands.RewardCommands;
import com.musbabaff.menhir.block.rewards.commands.RewardEntry;
import com.musbabaff.menhir.block.rewards.context.RewardContext;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Supplier;

public interface Reward {

    @SneakyThrows
    static Reward parse(ConfigurationSection section) {
        RewardType type = RewardType.getByName(section.getString("type", "null"))
                .orElseThrow((Supplier<Throwable>) () -> new IllegalArgumentException("Invalid reward type for reward named " + section.getName()));
        return type.parse(section.getName(), section);
    }

    static void save(ConfigurationSection section, Reward reward) {
        RewardType type = reward.getType();
        section.set("type", type.name().toLowerCase());
        section.set("mode", reward.getCommands().getModeName());
        type.set(section, reward);
    }

    String getName();
    RewardType getType();
    /**
     * @param context If the block is not fully broken, partial reward context is supplied
     * */
    boolean canGet(PlayerData player, RewardContext context);
    boolean isLast();
    RewardCommands<? extends RewardEntry> getCommands();

}
