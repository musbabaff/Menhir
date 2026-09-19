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
import com.musbabaff.menhir.bossbar.BossBarService;
import com.musbabaff.menhir.countdown.RespawnCountdownService;
import com.musbabaff.menhir.api.MenhirAPI;
import com.musbabaff.menhir.api.MenhirProvider;
import com.musbabaff.menhir.impl.EventBridge;
import com.musbabaff.menhir.impl.MenhirAPIImpl;
import org.bukkit.plugin.ServicePriority;
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
import com.musbabaff.menhir.storage.StorageFactory;
import com.musbabaff.menhir.storage.StorageProvider;
import org.bukkit.scheduler.BukkitTask;
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
import java.util.concurrent.CompletableFuture;
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
    private BossBarService bossBarService;
    private RespawnCountdownService countdownService;
    private final EventBridge events = new EventBridge(this);
    private MenhirAPI api;
    private StorageProvider storage;
    private BukkitTask storageFlushTask;
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
        api = new MenhirAPIImpl(this);
        getServer().getServicesManager().register(MenhirAPI.class, api, this, ServicePriority.Normal);
        MenhirProvider.register(api);
        getServer().getScheduler().runTaskLater(this, () -> {
            editValuesListener = new EditListener(this);
            getServer().getPluginManager().registerEvents(editValuesListener, this);
            getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
            load();
        }, 1L);
    }

    private void load() {
        configuration = new MenhirConfig(getConfig(), getLogger());
        storage = StorageFactory.create(configuration.getStorageSettings(), storageFolder, getLogger());
        logInfo("Using {0} storage", storage.getType());
        CompletableFuture<Void> storageLoad = storage.load();
        hologramManager = new HologramManager(this);
        hologramManager.start();
        afkService = new AfkService(this);
        afkService.start();
        bossBarService = new BossBarService(this);
        bossBarService.start();
        countdownService = new RespawnCountdownService(this);
        prefixProvider = VaultPrefixProvider.create(getServer()).orElse(null);
        if (prefixProvider != null) logInfo("Using Vault chat service for player prefixes");
        registerPlaceholders();
        for (MenhirBlock block : configuration.getBlocksConfig().getBlocks(this)) {
            blockRegistry.register(block);
        }
        StorageProvider loadingStorage = storage;
        storageLoad.whenComplete((ignored, error) -> getServer().getScheduler().runTask(this, () -> {
            if (storage != loadingStorage) return; // reloaded in the meantime
            if (error != null) {
                getLogger().log(Level.SEVERE, "Storage could not be loaded; stones start with fresh data until the database is back", error);
                return;
            }
            for (MenhirBlock block : blockRegistry.getBlocks()) block.restoreFromStorage();
        }));
        long flushTicks = configuration.getStorageSettings().getFlushInterval() * 20L;
        storageFlushTask = getServer().getScheduler().runTaskTimer(this, () -> storage.save(), flushTicks, flushTicks);
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
        MenhirProvider.register(null);
        getServer().getServicesManager().unregisterAll(this);
        Gui.disable();
    }

    private void unload() {
        if (!storageFolder.exists())
            createFolders();
        closeAllGuis();
        blockRegistry.unregisterAll(block -> {
        });
        if (storageFlushTask != null) {
            storageFlushTask.cancel();
            storageFlushTask = null;
        }
        if (storage != null) {
            storage.close();
        }
        unregisterPlaceholders();
        if (bossBarService != null) bossBarService.stop();
        if (countdownService != null) countdownService.stop();
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
