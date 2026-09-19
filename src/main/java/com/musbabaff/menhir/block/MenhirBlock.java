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
import com.musbabaff.menhir.util.color.Colors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class MenhirBlock {

    public static File getStoragePath(MenhirPlugin plugin, MenhirBlock block) {
        return new File(plugin.getStorageFolder(), block.id + ".mb");
    }

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
    private Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public Runnable onBreak(Player player) {
        health.decrement();
        resetOptions.resetInactive();

        List<Runnable> runnables = new LinkedList<>();

        PlayerData playerData = playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(uuid, player.getName()));
        playerData.incrementBreaks();
        top.update(playerData);
        runnables.add(rewards.giveRewards(playerData));

        if (health.getHealth() <= 0) runnables.add(onLastBreak(player));

        hologram.update();

        return () -> runnables.forEach(Runnable::run);
    }

    private Runnable onLastBreak(Player player) {
        Runnable runnable = rewards.giveLastRewards(player.getUniqueId());
        broadcast(messages.getBreakMessage());
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
        coolDown.deactivate();
    }

    public void destroy() {
        hide();
        hologram.delete();
        resetOptions.cancelInactive();
    }

    public void reset() {
        health.reset();
        playerDataMap.clear();
        resetOptions.cancelInactive();
        coolDown.deactivate();
        top.clear();
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

    public void saveData(DataOutput output) throws IOException {
        output.writeInt(health.getHealth());
        boolean isCoolDownActive = coolDown.isActive();
        output.writeBoolean(isCoolDownActive);
        if (isCoolDownActive) output.writeLong(coolDown.getActive().getEnd().getTime());
        List<PlayerData> playerData = new LinkedList<>(playerDataMap.values());
        output.writeInt(playerData.size());
        for (PlayerData player : playerData) {
            player.serialize(output);
        }
    }

    public void saveData(File file) throws IOException {
        if (!file.exists()) file.createNewFile();
        try (DataOutputStream fos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            saveData(fos);
        }
    }

    public void loadData(DataInput input) throws IOException {
        if (resetOptions.isOnRestart()) return;
        health.setHealth(input.readInt());
        if (input.readBoolean()) {
            coolDown.activate(new Date(input.readLong()));
        } else coolDown.deactivate();
        playerDataMap.clear();
        top.clear();
        int players = input.readInt();
        for (int i = 0; i < players; i++) {
            PlayerData playerData = PlayerData.deserialize(input);
            playerDataMap.put(playerData.getUuid(), playerData);
            top.update(playerData);
        }
        if (health.getHealth() != health.getMaxHealth()) resetOptions.resetInactive();
    }

    public void loadData(File file) throws IOException {
        if (!file.exists()) return;
        try (DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            loadData(fis);
        }
    }

    public void setCoolDown(BlockCoolDown coolDown) {
        if (this.coolDown != null) this.coolDown.deactivate();
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

    /** Effective AFK settings for this block (global settings with the block's overrides applied). */
    public AfkSettings getAfkSettings() {
        return plugin.getConfiguration().getOptionsConfig().getAfkSettings().merge(afkOverride);
    }

}
