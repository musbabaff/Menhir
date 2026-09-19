/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.placeholder;

import com.musbabaff.menhir.api.TopEntry;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.cooldown.BlockCoolDown;
import com.musbabaff.menhir.block.top.BlockTop;
import com.musbabaff.menhir.config.MenhirConfig;
import com.musbabaff.menhir.integration.PrefixProvider;
import com.musbabaff.menhir.util.placeholders.PlaceholderSet;

import java.util.List;
import java.util.Optional;

public class BlockPlaceholderSet extends PlaceholderSet {

    /** Leaderboard row {@code pos} (0-based) of the stone, cross-server aware. */
    private static Optional<TopEntry> entry(MenhirBlock block, int pos) {
        List<TopEntry> leaderboard = block.getLeaderboard(BlockTop.MAX_TOP_SIZE);
        return pos < leaderboard.size() ? Optional.of(leaderboard.get(pos)) : Optional.empty();
    }

    public BlockPlaceholderSet(MenhirBlock block) {
        MenhirConfig config = block.getPlugin().getConfiguration();
        PrefixProvider prefixProvider = block.getPlugin().getPrefixProvider();

        for (int i = 0; i < BlockTop.MAX_TOP_SIZE; i++) {
            int pos = i;
            String posStr = "player_" + (pos + 1);
            addPlaceholder(posStr, () -> entry(block, pos)
                    .map(TopEntry::name)
                    .orElse(config.getLangConfig().getNobodyName())
            );
            addPlaceholder(posStr + "_breaks", () -> entry(block, pos)
                    .map(TopEntry::breaks)
                    .map(String::valueOf)
                    .orElse(config.getLangConfig().getNobodyBreaks())
            );
            if (prefixProvider != null) {
                addPlaceholder(posStr + "_prefix", () -> entry(block, pos)
                        .map(TopEntry::uuid)
                        .map(prefixProvider::provide)
                        .filter(java.util.Objects::nonNull)
                        .orElse("")
                );
            }
        }
        addPlaceholder("type", () -> String.valueOf(block.getType().getType()));
        addPlaceholder("block_id", block::getId);
        addPlaceholder("block_name", block::getDisplayName);
        addPlaceholder("percent", () -> String.valueOf(block.getHealth().getPercent()));
        addPlaceholder("health", () -> String.valueOf(block.getHealth().getHealth()));
        addPlaceholder("max_health", () -> String.valueOf(block.getHealth().getMaxHealth()));
        addPlaceholder("timeout", () -> {
            BlockCoolDown coolDown = block.getCoolDown();
            if (!coolDown.isActive()) return "";
            return block.getPlugin().getConfiguration().getLangConfig().getTimeoutFormatted(coolDown.getActive().getEnd());
        });
    }

}
