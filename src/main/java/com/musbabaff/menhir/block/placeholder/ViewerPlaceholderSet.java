/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.placeholder;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.config.lang.LangConfig;
import com.musbabaff.menhir.util.placeholders.PlaceholderSet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Placeholders that depend on who is looking: {@code %my_breaks%} and {@code %my_rank%}.
 */
public class ViewerPlaceholderSet extends PlaceholderSet {

    public ViewerPlaceholderSet(MenhirBlock block, UUID viewer) {
        LangConfig lang = block.getPlugin().getConfiguration().getLangConfig();
        addPlaceholder("my_breaks", () -> String.valueOf(
                Optional.ofNullable(block.getPlayerDataMap().get(viewer)).map(PlayerData::getBreaks).orElse(0)));
        addPlaceholder("my_rank", () -> rank(block, viewer).map(String::valueOf).orElse(lang.getNotRanked()));
    }

    /** 1-based rank of the player in the block's top list. */
    public static Optional<Integer> rank(MenhirBlock block, UUID player) {
        List<PlayerData> top = block.getTop().getPlayers();
        for (int i = 0; i < top.size(); i++) {
            if (top.get(i).getUuid().equals(player)) return Optional.of(i + 1);
        }
        return Optional.empty();
    }

}
