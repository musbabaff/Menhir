/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.rewards.context;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.block.MenhirBlock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RewardContext {

    private final MenhirBlock block;
    private final Random random;
    @Getter(AccessLevel.NONE)
    private final Map<UUID, Integer> positions;
    private final UUID lastBreaker;

    public int getPosition(UUID id) {
        return positions.getOrDefault(id, -1);
    }

    public MenhirPlugin getPlugin() {
        return block.getPlugin();
    }

}
