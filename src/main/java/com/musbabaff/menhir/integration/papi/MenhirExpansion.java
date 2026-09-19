/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.integration.papi;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.api.TopEntry;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.cooldown.BlockCoolDown;
import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.config.lang.LangConfig;
import com.musbabaff.menhir.util.NumberUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * PlaceholderAPI expansion with identifier {@code menhir}.
 *
 * <pre>
 * %menhir_&lt;block&gt;_health%        %menhir_&lt;block&gt;_max_health%    %menhir_&lt;block&gt;_percent%
 * %menhir_&lt;block&gt;_status%        %menhir_&lt;block&gt;_timeout%
 * %menhir_&lt;block&gt;_top_&lt;n&gt;_name%  %menhir_&lt;block&gt;_top_&lt;n&gt;_breaks%
 * %menhir_&lt;block&gt;_my_breaks%     %menhir_&lt;block&gt;_my_rank%
 * %menhir_global_top_&lt;n&gt;_name%   %menhir_global_top_&lt;n&gt;_breaks%
 * %menhir_next_respawn%
 * </pre>
 * The MineBlocks names ({@code hp, max_hp, breaks, rank, top_<n>, top_breaks_<n>}) are still accepted.
 */
public class MenhirExpansion extends PlaceholderExpansion {

    private static final String STATUS_ALIVE = "alive";
    private static final String STATUS_BROKEN = "broken";

    private final MenhirPlugin plugin;

    public MenhirExpansion(MenhirPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "menhir";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String lower = params.toLowerCase(Locale.ROOT);
        if (lower.startsWith("global_top_")) return globalTop(lower.substring("global_top_".length()));
        if (lower.equals("next_respawn")) return nextRespawn();

        Optional<MenhirBlock> blockOpt = findBlock(List.of(params.split("_")));
        if (blockOpt.isEmpty()) return "block_not_found";
        MenhirBlock block = blockOpt.get();
        String value = params.substring(block.getId().length());
        if (value.startsWith("_")) value = value.substring(1);
        return blockPlaceholder(block, player, value.toLowerCase(Locale.ROOT));
    }

    /** Block ids may contain underscores: try the longest prefix first. */
    private Optional<MenhirBlock> findBlock(List<String> params) {
        MenhirBlock block = plugin.getBlockRegistry().get(String.join("_", params));
        if (block != null) return Optional.of(block);
        if (params.size() <= 1) return Optional.empty();
        return findBlock(params.subList(0, params.size() - 1));
    }

    private String blockPlaceholder(MenhirBlock block, OfflinePlayer player, String value) {
        LangConfig lang = plugin.getConfiguration().getLangConfig();
        if (value.startsWith("top_")) return top(block, value.substring(4), lang);
        return switch (value) {
            case "health", "hp" -> String.valueOf(block.getHealth().getHealth());
            case "max_health", "max_hp" -> String.valueOf(block.getHealth().getMaxHealth());
            case "percent" -> percent(block);
            case "status" -> block.getCoolDown().isActive() ? STATUS_BROKEN : STATUS_ALIVE;
            case "timeout" -> timeout(block, lang);
            case "my_breaks", "breaks" -> player == null ? "0" : String.valueOf(
                    Optional.ofNullable(block.getPlayerDataMap().get(player.getUniqueId()))
                            .map(PlayerData::getBreaks)
                            .orElse(0));
            case "my_rank", "rank" -> player == null ? lang.getNotRanked() : rank(block, player.getUniqueId())
                    .map(String::valueOf)
                    .orElse(lang.getNotRanked());
            default -> null;
        };
    }

    /**
     * Handles {@code top_<n>_name}, {@code top_<n>_breaks}, legacy {@code top_<n>} (name) and {@code top_breaks_<n>}.
     */
    private String top(MenhirBlock block, String rest, LangConfig lang) {
        boolean breaks;
        String position;
        if (rest.startsWith("breaks_")) {
            breaks = true;
            position = rest.substring("breaks_".length());
        } else if (rest.endsWith("_breaks")) {
            breaks = true;
            position = rest.substring(0, rest.length() - "_breaks".length());
        } else if (rest.endsWith("_name")) {
            breaks = false;
            position = rest.substring(0, rest.length() - "_name".length());
        } else {
            breaks = false;
            position = rest;
        }
        Optional<TopEntry> data = NumberUtil.parseInt(position)
                .map(i -> i - 1)
                .filter(i -> i >= 0)
                .flatMap(p -> {
                    List<TopEntry> leaderboard = block.getLeaderboard(p + 1);
                    return p < leaderboard.size() ? Optional.of(leaderboard.get(p)) : Optional.empty();
                });
        return breaks
                ? data.map(TopEntry::breaks).map(String::valueOf).orElse(lang.getNobodyBreaks())
                : data.map(TopEntry::name).orElse(lang.getNobodyName());
    }

    private String percent(MenhirBlock block) {
        int max = Math.max(1, block.getHealth().getMaxHealth());
        int health = Math.max(0, block.getHealth().getHealth());
        return String.valueOf(Math.round(health * 100d / max));
    }

    private String timeout(MenhirBlock block, LangConfig lang) {
        BlockCoolDown coolDown = block.getCoolDown();
        if (!coolDown.isActive()) return "";
        return lang.getTimeoutFormatted(coolDown.getActive().getEnd());
    }

    private Optional<Integer> rank(MenhirBlock block, UUID playerId) {
        List<PlayerData> topPlayers = block.getTop().getPlayers();
        for (int i = 0; i < topPlayers.size(); i++) {
            if (topPlayers.get(i).getUuid().equals(playerId)) {
                return Optional.of(i + 1); // 1-based
            }
        }
        return Optional.empty();
    }

    /** {@code global_top_<n>_name} / {@code global_top_<n>_breaks}: totals over all blocks. */
    private String globalTop(String rest) {
        LangConfig lang = plugin.getConfiguration().getLangConfig();
        boolean breaks = rest.endsWith("_breaks");
        String position = breaks ? rest.substring(0, rest.length() - "_breaks".length())
                : rest.endsWith("_name") ? rest.substring(0, rest.length() - "_name".length()) : rest;
        Optional<TopEntry> data = NumberUtil.parseInt(position)
                .map(i -> i - 1)
                .filter(i -> i >= 0)
                .flatMap(i -> {
                    List<TopEntry> total = plugin.getStorage().getGlobalTop(i + 1);
                    return i < total.size() ? Optional.of(total.get(i)) : Optional.empty();
                });
        return breaks
                ? data.map(TopEntry::breaks).map(String::valueOf).orElse(lang.getNobodyBreaks())
                : data.map(TopEntry::name).orElse(lang.getNobodyName());
    }

    /** Formatted time until the nearest respawn, or an empty string if no block is broken. */
    private String nextRespawn() {
        Date nearest = null;
        for (MenhirBlock block : plugin.getBlockRegistry().getBlocks()) {
            BlockCoolDown coolDown = block.getCoolDown();
            if (!coolDown.isActive()) continue;
            Date end = coolDown.getActive().getEnd();
            if (nearest == null || end.before(nearest)) nearest = end;
        }
        if (nearest == null) return "";
        return plugin.getConfiguration().getLangConfig().getTimeoutFormatted(nearest);
    }

}
