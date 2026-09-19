/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.item.render;

import org.bukkit.inventory.ItemStack;

public interface StateRenderer<T> {

    ItemStack render(T state);

    default Renderer<T> toRenderer() {
        return new Renderer<>() {
            @Override
            public ItemStack render(int slot, T state) {
                return StateRenderer.this.render(state);
            }

            @Override
            public boolean requiresPerSlotRendering() {
                return false;
            }
        };
    }

}
