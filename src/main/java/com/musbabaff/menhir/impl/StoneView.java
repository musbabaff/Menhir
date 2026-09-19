/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.impl;

import com.musbabaff.menhir.api.MenhirStone;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.cooldown.BlockCoolDown;
import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * Live {@link MenhirStone} view over an internal {@link MenhirBlock}.
 */
@ApiStatus.Internal
public final class StoneView implements MenhirStone {

    private final MenhirBlock block;

    public StoneView(MenhirBlock block) {
        this.block = block;
    }

    /** The wrapped internal block. */
    public MenhirBlock getBlock() {
        return block;
    }

    @Override
    public @NotNull String getId() {
        return block.getId();
    }

    @Override
    public @NotNull String getDisplayName() {
        return block.getDisplayName();
    }

    @Override
    public @NotNull Location getLocation() {
        return block.getLocation().clone();
    }

    @Override
    public int getHealth() {
        return block.getHealth().getHealth();
    }

    @Override
    public int getMaxHealth() {
        return block.getHealth().getMaxHealth();
    }

    @Override
    public boolean isBroken() {
        BlockCoolDown coolDown = block.getCoolDown();
        return coolDown != null && coolDown.isActive();
    }

    @Override
    public @NotNull OptionalLong getRespawnAt() {
        BlockCoolDown coolDown = block.getCoolDown();
        if (coolDown == null || !coolDown.isActive()) return OptionalLong.empty();
        return OptionalLong.of(coolDown.getActive().getEnd().getTime());
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return block.hasPermission() ? Optional.of(block.getPermission()) : Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StoneView other && other.block == block;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(block);
    }

    @Override
    public String toString() {
        return "MenhirStone[" + block.getId() + "]";
    }

}
