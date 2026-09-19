/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.cooldown;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class ActiveCoolDown {

    private final Date end;
    private final CompletableFuture<Void> future;
    private final BukkitTask task;
    private final BukkitTask updateTask;

}
