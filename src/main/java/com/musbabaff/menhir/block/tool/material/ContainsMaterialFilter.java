/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.tool.material;

import com.musbabaff.menhir.block.tool.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

@Getter
@Setter
@AllArgsConstructor
public class ContainsMaterialFilter implements MaterialFilter {

    private final String str;
    private Result result;

    @Override
    public boolean test(Material material) {
        return material.name().contains(str);
    }

    @Override
    public String toString() {
        return str;
    }
}
