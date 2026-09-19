/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.cooldown;

import com.musbabaff.menhir.block.MenhirBlock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class BlockCoolDown {

    @Getter(AccessLevel.NONE)
    private final MenhirBlock block;
    private int time;
    private Material typeOverride;
    private String respawnMessage;
    @Setter(AccessLevel.NONE)
    private ActiveCoolDown active;

    public BlockCoolDown(MenhirBlock block, int time, Material typeOverride, String respawnMessage) {
        this.block = block;
        this.time = time;
        this.typeOverride = typeOverride;
        this.respawnMessage = respawnMessage;
    }

    public ActiveCoolDown activate() {
        if (time <= 0) return null;
        return activate(new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, TimeUnit.SECONDS)));
    }

    public ActiveCoolDown activate(Date end) {
        deactivate();
        long remaining = end.getTime() - System.currentTimeMillis();
        if (remaining <= 0) return null;
        block.getType().setOverride(typeOverride);
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.active = new ActiveCoolDown(
                end,
                future,
                Bukkit.getScheduler().runTaskLater(block.getPlugin(), this::deactivate, TimeUnit.SECONDS.convert(remaining, TimeUnit.MILLISECONDS) * 20L),
                Bukkit.getScheduler().runTaskTimer(block.getPlugin(), () -> block.getHologram().update(), 0, 10)
        );
        return active;
    }

    public boolean deactivate() {
        if (!isActive()) return false;
        active.getTask().cancel();
        active.getUpdateTask().cancel();
        active.getFuture().complete(null);
        block.getType().setOverride(null);
        block.broadcast(respawnMessage);
        this.active = null;
        block.getHologram().update();
        return true;
    }


    public boolean isActive() {
        return active != null;
    }

    public void setTypeOverride(Material typeOverride) {
        this.typeOverride = typeOverride;
        if (isActive()) {
            block.getType().setOverride(typeOverride);
        }
    }
}
