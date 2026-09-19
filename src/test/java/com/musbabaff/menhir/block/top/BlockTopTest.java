/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.top;

import com.musbabaff.menhir.block.playerdata.PlayerData;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockTopTest {

    @Test
    void test() {
        BlockTop top = new BlockTop();
        List<PlayerData> players = new LinkedList<>();
        // distinct break counts so the expected order is unambiguous
        List<Integer> breaks = new ArrayList<>();
        for (int i = 0; i < 100; i++) breaks.add(i * 7);
        Collections.shuffle(breaks, new Random(42));

        for (int i = 0; i < 100; i++) {
            PlayerData playerData = new PlayerData(UUID.randomUUID(), "player" + (i + 1), breaks.get(i));
            players.add(playerData);
            top.update(playerData);
        }

        assertEquals(
                players.stream()
                        .sorted(Comparator.comparingInt(PlayerData::getBreaks).reversed())
                        .limit(10)
                        .collect(Collectors.toList()),
                top.getPlayers()
        );
    }

}