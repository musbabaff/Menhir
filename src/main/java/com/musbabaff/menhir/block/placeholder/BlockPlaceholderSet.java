/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.placeholder;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.cooldown.BlockCoolDown;
import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.block.top.BlockTop;
import com.musbabaff.menhir.config.MenhirConfig;
import com.musbabaff.menhir.integration.PrefixProvider;
import com.musbabaff.menhir.util.placeholders.PlaceholderSet;

public class BlockPlaceholderSet extends PlaceholderSet {

    public BlockPlaceholderSet(MenhirBlock block) {
        MenhirConfig config = block.getPlugin().getConfiguration();
        PrefixProvider prefixProvider = block.getPlugin().getPrefixProvider();

        for (int i = 0; i < BlockTop.MAX_TOP_SIZE; i++) {
            int pos = i;
            String posStr = "player_" + (pos + 1);
            addPlaceholder(posStr, () -> block.getTop()
                    .getPlayer(pos)
                    .map(PlayerData::getDisplayName)
                    .orElse(config.getLangConfig().getNobodyName())
            );
            addPlaceholder(posStr + "_breaks", () -> block.getTop()
                    .getPlayer(pos)
                    .map(PlayerData::getBreaks)
                    .map(String::valueOf)
                    .orElse(config.getLangConfig().getNobodyBreaks())
            );
            if (prefixProvider != null) {
                addPlaceholder(posStr + "_prefix", () -> block.getTop()
                        .getPlayer(pos)
                        .map(PlayerData::getUuid)
                        .map(prefixProvider::provide)
                        .filter(java.util.Objects::nonNull)
                        .orElse("")
                );
            }
        }
        addPlaceholder("type", () -> String.valueOf(block.getType().getType()));
        addPlaceholder("health", () -> String.valueOf(block.getHealth().getHealth()));
        addPlaceholder("max_health", () -> String.valueOf(block.getHealth().getMaxHealth()));
        addPlaceholder("timeout", () -> {
            BlockCoolDown coolDown = block.getCoolDown();
            if (!coolDown.isActive()) return "";
            return block.getPlugin().getConfiguration().getLangConfig().getTimeoutFormatted(coolDown.getActive().getEnd());
        });
    }

}
