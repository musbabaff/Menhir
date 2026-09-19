/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.reset;

import com.musbabaff.menhir.block.MenhirBlock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Setter
public class ResetOptions {

    @Getter(AccessLevel.NONE)
    private final MenhirBlock block;
    private boolean onRestart;
    private int inactiveTime;
    private String inactiveMessage;
    private BukkitTask inactiveTask;

    public ResetOptions(MenhirBlock block, boolean onRestart, int inactiveTime, String inactiveMessage) {
        this.block = block;
        this.onRestart = onRestart;
        this.inactiveTime = inactiveTime;
        this.inactiveMessage = inactiveMessage;
    }

    public void resetInactive() {
        cancelInactive();
        if (inactiveTime > 0) {
            inactiveTask = new BukkitRunnable() {
                @Override
                public void run() {
                    block.broadcast(inactiveMessage);
                    block.reset();
                }
            }.runTaskLater(block.getPlugin(), inactiveTime * 20L);
        }
    }

    public void cancelInactive() {
        if (inactiveTask != null) {
            inactiveTask.cancel();
            inactiveTask = null;
        }
    }

    public void disable() {
        if (inactiveTask != null) {
            inactiveTask.cancel();
            inactiveTask = null;
        }
    }

}
