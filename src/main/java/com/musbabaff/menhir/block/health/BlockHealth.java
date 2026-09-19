/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.health;

import com.musbabaff.menhir.block.MenhirBlock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockHealth {

    @Getter(AccessLevel.NONE)
    private final MenhirBlock block;
    private int maxHealth;
    private int health;

    public BlockHealth(MenhirBlock block, int maxHealth) {
        this.block = block;
        this.maxHealth = Math.max(1, maxHealth);
        this.health = maxHealth;
    }

    public void reset() {
        health = maxHealth;
    }

    public void setHealth(int health) {
        this.health = Math.min(health, getMaxHealth());
    }

    public void decrement() {
        health--;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        block.reset();
    }

}
