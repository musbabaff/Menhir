/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import com.musbabaff.menhir.acf.ColorsFormatter;
import com.musbabaff.menhir.afk.AfkService;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.rewards.offline.OfflineRewardsStorage;
import com.musbabaff.menhir.commands.MenhirCommand;
import com.musbabaff.menhir.config.MenhirConfig;
import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.hologram.HologramManager;
import com.musbabaff.menhir.integration.PrefixProvider;
import com.musbabaff.menhir.integration.papi.MenhirExpansion;
import com.musbabaff.menhir.integration.papi.PapiHook;
import com.musbabaff.menhir.integration.vault.VaultPrefixProvider;
import com.musbabaff.menhir.listener.BlocksListener;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.listener.EditListener;
import com.musbabaff.menhir.migration.LegacyMigration;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Menhir — breakable leaderboard stones for Paper.
 */
@Getter
public class MenhirPlugin extends JavaPlugin {

    /** Public repository, shown by /menhir version and /menhir wiki. */
    public static final String REPOSITORY_URL = "https://github.com/musbabaff/Menhir";

    private FileConfiguration config;
    private MenhirConfig configuration;
    private final BlockRegistry blockRegistry = new BlockRegistry();
    private BukkitCommandManager commandManager;
    private EditListener editValuesListener;
    private File storageFolder;
    private OfflineRewardsStorage offlineRewards;
    private HologramManager hologramManager;
    private AfkService afkService;
    private PrefixProvider prefixProvider;
    private MenhirExpansion papiExpansion;

    @Override
    public void onEnable() {
        storageFolder = new File(getDataFolder(), "storage");
        LegacyMigration.migrate(this);
        createFolders();
        offlineRewards = new OfflineRewardsStorage(storageFolder);
        Gui.enable(this);
        saveDefaultConfig();
        commandManager = new BukkitCommandManager(this);
        commandManager.usePerIssuerLocale(false);
        for (MessageType messageType : List.of(MessageType.HELP, MessageType.ERROR, MessageType.SYNTAX, MessageType.INFO)) {
            commandManager.setFormat(messageType, new ColorsFormatter());
        }
        commandManager.registerCommand(new MenhirCommand(this));
        getServer().getScheduler().runTaskLater(this, () -> {
            editValuesListener = new EditListener(this);
            getServer().getPluginManager().registerEvents(editValuesListener, this);
            getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
            load();
        }, 1L);
    }

    private void load() {
        configuration = new MenhirConfig(getConfig(), getLogger());
        hologramManager = new HologramManager(this);
        hologramManager.start();
        afkService = new AfkService(this);
        afkService.start();
        prefixProvider = VaultPrefixProvider.create(getServer()).orElse(null);
        if (prefixProvider != null) logInfo("Using Vault chat service for player prefixes");
        registerPlaceholders();
        for (MenhirBlock block : configuration.getBlocksConfig().getBlocks(this)) {
            blockRegistry.register(block);
        }
        logInfo("Loaded blocks from the config: {0}",
                blockRegistry.getBlocks().stream()
                        .map(MenhirBlock::getId)
                        .collect(Collectors.joining(", "))
        );
        logInfo("Menhir {0} enabled successfully!", getPluginMeta().getVersion());
    }

    private void registerPlaceholders() {
        if (!PapiHook.isAvailable()) return;
        try {
            papiExpansion = new MenhirExpansion(this);
            papiExpansion.register();
            logInfo("Registered PlaceholderAPI expansion \"menhir\"");
        } catch (Exception e) {
            papiExpansion = null;
            getLogger().log(Level.WARNING, "Could not register the PlaceholderAPI expansion", e);
        }
    }

    private void unregisterPlaceholders() {
        if (papiExpansion == null) return;
        try {
            papiExpansion.unregister();
        } catch (Exception ignored) {
            // PlaceholderAPI may already be disabled during shutdown
        }
        papiExpansion = null;
    }

    @Override
    public void onDisable() {
        unload();
        Gui.disable();
    }

    private void unload() {
        if (!storageFolder.exists())
            createFolders();
        closeAllGuis();
        blockRegistry.unregisterAll(block -> {
            try {
                block.saveData(MenhirBlock.getStoragePath(this, block));
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not save data of block " + block.getId(), e);
            }
        });
        unregisterPlaceholders();
        if (afkService != null) afkService.stop();
        if (hologramManager != null) hologramManager.stop();
        logInfo("Menhir disabled successfully!");
    }

    public void reload() {
        closeAllGuis();
        unload();
        reloadConfig();
        load();
    }

    public void logInfo(String msg, Object... args) {
        getLogger().log(Level.INFO, msg, args);
    }

    public void logWarn(String msg, Object... args) {
        getLogger().log(Level.WARNING, msg, args);
    }

    public void saveConfiguration() {
        getServer().getScheduler().runTaskAsynchronously(this, this::saveConfig);
    }

    public void closeAllGuis() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getOpenInventory().getTopInventory().getHolder() instanceof BlockMenu<?>) {
                onlinePlayer.closeInventory();
            }
        }
    }

    @NotNull
    @Override
    public FileConfiguration getConfig() {
        if (config == null) reloadConfig();
        return config;
    }

    @Override
    public void reloadConfig() {
        File file = getConfigFile();
        if (!file.exists()) {
            createFolders();
            saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void saveConfig() {
        if (config == null) return;
        try {
            config.save(getConfigFile());
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }

    private File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }

    private void createFolders() {
        //noinspection ResultOfMethodCallIgnored
        getDataFolder().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        storageFolder.mkdirs();
    }

}
