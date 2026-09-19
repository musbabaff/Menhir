/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.impl;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.api.MenhirAPI;
import com.musbabaff.menhir.api.MenhirStone;
import com.musbabaff.menhir.api.TopEntry;
import com.musbabaff.menhir.block.MenhirBlock;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link MenhirAPI} backed by the plugin's block registry and storage.
 */
@ApiStatus.Internal
public final class MenhirAPIImpl implements MenhirAPI {

    private final MenhirPlugin plugin;

    public MenhirAPIImpl(MenhirPlugin plugin) {
        this.plugin = plugin;
    }

    private Optional<MenhirBlock> block(String id) {
        return Optional.ofNullable(plugin.getBlockRegistry().get(id));
    }

    @Override
    public @NotNull Collection<MenhirStone> getStones() {
        return plugin.getBlockRegistry().getBlocks().stream()
                .map(StoneView::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @NotNull Optional<MenhirStone> getStone(@NotNull String id) {
        return block(id).map(StoneView::new);
    }

    @Override
    public boolean isBroken(@NotNull String id) {
        return block(id).map(b -> b.getCoolDown().isActive()).orElse(false);
    }

    @Override
    public @NotNull OptionalLong getRespawnAt(@NotNull String id) {
        return block(id).map(StoneView::new).map(MenhirStone::getRespawnAt).orElse(OptionalLong.empty());
    }

    @Override
    public void breakStone(@NotNull String id) {
        block(id).ifPresent(MenhirBlock::forceBreak);
    }

    @Override
    public void respawnStone(@NotNull String id) {
        block(id).ifPresent(b -> b.getCoolDown().deactivate());
    }

    @Override
    public int getBreaks(@NotNull String stoneId, @NotNull UUID player) {
        return plugin.getStorage().getBreaks(stoneId, player);
    }

    @Override
    public @NotNull List<TopEntry> getTop(@NotNull String stoneId, int limit) {
        return block(stoneId).map(b -> b.getLeaderboard(limit)).orElseGet(() -> plugin.getStorage().getTop(stoneId, limit));
    }

    @Override
    public @NotNull List<TopEntry> getGlobalTop(int limit) {
        return plugin.getStorage().getGlobalTop(limit);
    }

    @Override
    public void resetStone(@NotNull String id) {
        block(id).ifPresent(MenhirBlock::reset);
    }

    @Override
    public void resetPlayer(@NotNull UUID player) {
        for (MenhirBlock block : plugin.getBlockRegistry().getBlocks()) block.removePlayer(player);
        plugin.getStorage().resetPlayer(player);
    }

}
