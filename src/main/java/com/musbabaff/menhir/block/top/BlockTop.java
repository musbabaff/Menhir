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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BlockTop {

    public static final int MAX_TOP_SIZE = 10;

    private final List<PlayerData> players = new ArrayList<>();

    /**
     * Inserts or re-positions the player so the list stays sorted by breaks (descending).
     * Players with equal breaks keep their existing order; a player that would fall outside
     * the top {@value #MAX_TOP_SIZE} is not added.
     */
    public void update(PlayerData player) {
        players.removeIf(p -> p.getUuid().equals(player.getUuid()));
        int index = 0;
        while (index < players.size() && players.get(index).getBreaks() >= player.getBreaks()) index++;
        if (index >= MAX_TOP_SIZE) return;
        players.add(index, player);
        if (players.size() > MAX_TOP_SIZE)
            players.remove(players.size() - 1);
    }

    public Optional<PlayerData> getPlayer(int pos) {
        if (pos < players.size()) return Optional.ofNullable(players.get(pos));
        return Optional.empty();
    }

    public void clear() {
        players.clear();
    }

    public List<PlayerData> getPlayers() {
        return Collections.unmodifiableList(players);
    }

}
