/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.migration;

import com.musbabaff.menhir.MenhirPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * First-run helper: if {@code plugins/Menhir/config.yml} does not exist yet but a MineBlocks data
 * folder does, its {@code config.yml} and {@code storage/} are <em>copied</em> over. The original
 * folder is left untouched.
 */
public final class LegacyMigration {

    private static final String LEGACY_FOLDER = "MineBlocks";

    private LegacyMigration() {
    }

    public static void migrate(MenhirPlugin plugin) {
        File dataFolder = plugin.getDataFolder();
        File newConfig = new File(dataFolder, "config.yml");
        if (newConfig.exists()) return;
        File legacyFolder = new File(dataFolder.getParentFile(), LEGACY_FOLDER);
        File legacyConfig = new File(legacyFolder, "config.yml");
        if (!legacyConfig.isFile()) return;
        try {
            Files.createDirectories(dataFolder.toPath());
            Files.copy(legacyConfig.toPath(), newConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File legacyStorage = new File(legacyFolder, "storage");
            if (legacyStorage.isDirectory()) {
                Path target = plugin.getStorageFolder().toPath();
                Files.createDirectories(target);
                try (var files = Files.list(legacyStorage.toPath())) {
                    for (Path file : (Iterable<Path>) files::iterator) {
                        if (Files.isRegularFile(file)) {
                            Files.copy(file, target.resolve(file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
            plugin.getLogger().info("Copied config.yml and storage from plugins/" + LEGACY_FOLDER + "/ — the old folder was not modified.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not copy the MineBlocks data folder", e);
        }
    }

}
