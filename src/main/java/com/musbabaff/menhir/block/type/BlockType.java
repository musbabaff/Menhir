/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.type;

import com.musbabaff.menhir.block.MenhirBlock;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Material;

import java.util.Optional;

@Getter
public class BlockType {

    @Getter(AccessLevel.NONE)
    private final MenhirBlock block;
    private Material type;
    private Material override;

    public BlockType(MenhirBlock block, Material type) {
        this.block = block;
        this.type = type;
    }

    public void setType(Material type) {
        this.type = type;
        update();
    }

    public void setOverride(Material override) {
        this.override = override;
        update();
    }

    public void update() {
        block.getLocation().getBlock().setType(get(), false);
        block.getHologram().update();
    }

    public Material get() {
        return Optional.ofNullable(override).orElse(type);
    }

}
