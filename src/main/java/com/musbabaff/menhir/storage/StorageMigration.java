/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage;

import com.musbabaff.menhir.api.TopEntry;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Copies every stone's state and hits from one provider to another. Free of Bukkit calls so it can
 * run on a background thread; progress lines are handed to a consumer.
 */
public final class StorageMigration {

    /** Outcome of a migration. */
    public record Result(int blocksCopied, int playersCopied, int skipped, File backup) {
    }

    private StorageMigration() {
    }

    /**
     * Migrates {@code source} into {@code target}. Both providers must be loaded; the target is closed
     * (flushed) at the end, the source is left open.
     *
     * @param overwrite whether existing target rows are replaced; otherwise they are skipped and counted
     */
    public static Result migrate(StorageProvider source, StorageProvider target, File backupFolder,
                                 boolean overwrite, Consumer<String> progress) throws IOException {
        File backup = backup(source, backupFolder);
        progress.accept("Backup written to " + backup.getName());
        int blocks = 0;
        int players = 0;
        int skipped = 0;
        for (String blockId : source.getBlockIds()) {
            Optional<StoneState> state = source.getState(blockId);
            if (state.isPresent()) {
                if (target.getState(blockId).isPresent() && !overwrite) {
                    skipped++;
                } else {
                    target.setState(blockId, state.get().health(), state.get().broken(), state.get().respawnAt());
                }
            }
            for (TopEntry entry : source.getPlayers(blockId).values()) {
                int existing = target.getBreaks(blockId, entry.uuid());
                if (existing > 0 && !overwrite) {
                    skipped++;
                    continue;
                }
                int delta = entry.breaks() - existing;
                if (delta != 0) target.addBreaks(blockId, entry.uuid(), entry.name(), delta);
                players++;
            }
            blocks++;
            progress.accept("Copied " + blockId + " (" + source.getPlayers(blockId).size() + " players)");
        }
        target.close();
        return new Result(blocks, players, skipped, backup);
    }

    /** Dumps the source to {@code storage-backup-<timestamp>.yml}. */
    static File backup(StorageProvider source, File folder) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File file = new File(folder, "storage-backup-" + stamp + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("source", source.getType());
        for (String blockId : source.getBlockIds()) {
            String path = "blocks." + blockId.replace('.', '_');
            yaml.set(path + ".id", blockId);
            source.getState(blockId).ifPresent(state -> {
                yaml.set(path + ".health", state.health());
                yaml.set(path + ".broken", state.broken());
                yaml.set(path + ".respawn-at", state.respawnAt());
            });
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Map.Entry<UUID, TopEntry> entry : source.getPlayers(blockId).entrySet()) {
                rows.add(Map.of("uuid", entry.getKey().toString(), "name", entry.getValue().name(), "breaks", entry.getValue().breaks()));
            }
            yaml.set(path + ".players", rows);
        }
        yaml.save(file);
        return file;
    }

}
