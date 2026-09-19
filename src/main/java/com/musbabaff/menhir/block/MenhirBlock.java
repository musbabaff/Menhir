/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.block.cooldown.BlockCoolDown;
import com.musbabaff.menhir.block.health.BlockHealth;
import com.musbabaff.menhir.block.hologram.BlockHologram;
import com.musbabaff.menhir.block.messages.BlockMessages;
import com.musbabaff.menhir.block.placeholder.BlockPlaceholderSet;
import com.musbabaff.menhir.block.playerdata.PlayerData;
import com.musbabaff.menhir.block.playerdata.placeholder.PlayerDataPlaceholderSet;
import com.musbabaff.menhir.block.reset.ResetOptions;
import com.musbabaff.menhir.block.rewards.BlockRewards;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.block.top.BlockTop;
import com.musbabaff.menhir.block.type.BlockType;
import com.musbabaff.menhir.config.afk.AfkSettings;
import com.musbabaff.menhir.config.bossbar.BossBarSettings;
import com.musbabaff.menhir.config.countdown.CountdownSettings;
import com.musbabaff.menhir.util.color.Colors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.musbabaff.menhir.api.TopEntry;
import com.musbabaff.menhir.storage.StoneState;

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class MenhirBlock {

    private final MenhirPlugin plugin;
    private String id;
    private BlockHologram hologram;
    private BlockHealth health;
    private Location location;
    private BlockType type;
    private BlockCoolDown coolDown;
    private ResetOptions resetOptions;
    private BlockMessages messages;
    private BlockRewards rewards;
    private String permission;
    private RequiredTool requiredTool;
    private BlockTop top = new BlockTop();
    private int breakLimit = 0;
    /** Per-block AFK overrides; merged with the global settings by {@link #getAfkSettings()}. */
    private AfkSettings afkOverride = AfkSettings.EMPTY;
    /** Per-block boss bar overrides; merged with the global settings by {@link #getBossBarSettings()}. */
    private BossBarSettings bossBarOverride = BossBarSettings.EMPTY;
    /** Per-block respawn countdown overrides. */
    private CountdownSettings countdownOverride = CountdownSettings.EMPTY;
    /** Optional display name used by {@code %block_name%}; falls back to the id. */
    private String displayName;
    private Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public Runnable onBreak(Player player) {
        return onBreak(player, 1);
    }

    /**
     * Applies a hit of {@code damage} health by {@code player}. The returned runnable dispatches the
     * reward commands and must be run by the caller (on the main thread).
     */
    public Runnable onBreak(Player player, int damage) {
        health.damage(damage);
        resetOptions.resetInactive();

        List<Runnable> runnables = new LinkedList<>();

        PlayerData playerData = playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(uuid, player.getName()));
        playerData.incrementBreaks();
        top.update(playerData);
        plugin.getStorage().addBreaks(id, player.getUniqueId(), player.getName(), 1);
        runnables.add(rewards.giveRewards(playerData));

        if (health.getHealth() <= 0) runnables.add(onLastBreak(player));
        else persistState();

        hologram.update();

        return () -> runnables.forEach(Runnable::run);
    }

    /**
     * Breaks the stone now, as if the last hit had just happened (rewards, message, cooldown).
     * Does nothing while the stone is already broken.
     */
    public void forceBreak() {
        if (coolDown.isActive()) return;
        health.setHealth(0);
        Runnable rewardsRun = onLastBreak(null);
        hologram.update();
        rewardsRun.run();
    }

    /** Removes a player's hits from the current round (API). */
    public void removePlayer(UUID player) {
        if (playerDataMap.remove(player) != null) {
            top.clear();
            for (PlayerData data : playerDataMap.values()) top.update(data);
            hologram.update();
        }
    }

    private Runnable onLastBreak(Player player) {
        // The event describes the round that just ended on this server, so use the local top list.
        List<TopEntry> roundTop = new ArrayList<>();
        for (PlayerData data : top.getPlayers()) roundTop.add(new TopEntry(data.getUuid(), data.getDisplayName(), data.getBreaks()));
        plugin.getEvents().broken(this, player, roundTop,
                playerDataMap.values().stream().mapToInt(PlayerData::getBreaks).sum());
        Runnable runnable = rewards.giveLastRewards(player == null ? null : player.getUniqueId());
        broadcast(messages.getBreakMessage());
        plugin.getBossBarService().onBroken(this);
        reset();
        coolDown.activate();
        return runnable;
    }

    public void show() {
        hologram.show();
        type.update();
        hologram.update();
    }

    public void hide() {
        hologram.hide();
        getLocation().getBlock().setType(Material.AIR, false);
        coolDown.deactivate(BlockCoolDown.EndReason.SILENT);
    }

    public void destroy() {
        hide();
        hologram.delete();
        resetOptions.cancelInactive();
        if (plugin.getBossBarService() != null) plugin.getBossBarService().hideBlock(this);
    }

    public void reset() {
        health.reset();
        playerDataMap.clear();
        resetOptions.cancelInactive();
        coolDown.deactivate();
        top.clear();
        plugin.getStorage().resetBlock(id);
        persistState();
        hologram.update();
    }

    public void broadcast(String message) {
        if (message == null || message.isEmpty()) return;
        message = new BlockPlaceholderSet(this).parse(message);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData playerData = playerDataMap.getOrDefault(player.getUniqueId(), new PlayerData(player.getUniqueId(), player.getName()));
            player.sendMessage(Colors.colorize(
                    new PlayerDataPlaceholderSet(playerData).parse(message)
            ));
        }
    }

    public void setCoolDown(BlockCoolDown coolDown) {
        if (this.coolDown != null) this.coolDown.deactivate(BlockCoolDown.EndReason.SILENT);
        this.coolDown = coolDown;
    }

    public void teleport(Location location) {
        hide();
        this.location = location;
        hologram.updateLocation();
        show();
    }

    public boolean hasPermission() {
        return permission != null && !permission.isBlank();
    }

    /** Display name for messages ({@code display-name} in the config), or the id when not set. */
    public String getDisplayName() {
        return displayName == null || displayName.isBlank() ? id : displayName;
    }

    /** Effective respawn countdown settings for this block. */
    public CountdownSettings getCountdownSettings() {
        return plugin.getConfiguration().getOptionsConfig().getCountdownSettings().merge(countdownOverride);
    }

    /** Writes the current health and cooldown state to the storage layer. */
    public void persistState() {
        if (id == null || health == null || coolDown == null) return;
        boolean broken = coolDown.isActive();
        long respawnAt = broken ? coolDown.getActive().getEnd().getTime() : 0L;
        plugin.getStorage().setState(id, health.getHealth(), broken, respawnAt);
    }

    /**
     * Restores health, cooldown and the hit counts of the current round from the storage layer.
     * Honours {@code reset.onrestart}: such blocks start fresh and the stored round is discarded.
     */
    public void restoreFromStorage() {
        if (resetOptions.isOnRestart()) {
            plugin.getStorage().resetBlock(id);
            persistState();
            return;
        }
        StoneState state = plugin.getStorage().getState(id).orElse(null);
        if (state != null && state.health() >= 0) {
            health.setHealth(state.health());
            if (state.broken() && state.respawnAt() > System.currentTimeMillis()) {
                coolDown.activate(new Date(state.respawnAt()));
            } else {
                coolDown.deactivate(BlockCoolDown.EndReason.SILENT);
            }
        }
        playerDataMap.clear();
        top.clear();
        for (TopEntry entry : plugin.getStorage().getPlayers(id).values()) {
            PlayerData playerData = new PlayerData(entry.uuid(), entry.name(), entry.breaks());
            playerDataMap.put(playerData.getUuid(), playerData);
            top.update(playerData);
        }
        if (health.getHealth() != health.getMaxHealth()) resetOptions.resetInactive();
        hologram.update();
    }

    /**
     * Leaderboard of this stone: the local top list, or the cross-server list when the storage
     * combines several servers.
     */
    public List<TopEntry> getLeaderboard(int limit) {
        if (plugin.getStorage().isCrossServer()) return plugin.getStorage().getTop(id, limit);
        List<TopEntry> entries = new ArrayList<>();
        for (PlayerData data : top.getPlayers()) {
            if (entries.size() >= limit) break;
            entries.add(new TopEntry(data.getUuid(), data.getDisplayName(), data.getBreaks()));
        }
        return entries;
    }

    /** Effective boss bar settings for this block (global settings with the block's overrides applied). */
    public BossBarSettings getBossBarSettings() {
        return plugin.getConfiguration().getOptionsConfig().getBossBarSettings().merge(bossBarOverride);
    }

    /** Effective AFK settings for this block (global settings with the block's overrides applied). */
    public AfkSettings getAfkSettings() {
        return plugin.getConfiguration().getOptionsConfig().getAfkSettings().merge(afkOverride);
    }

}
