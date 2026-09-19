/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage.mysql;

import com.musbabaff.menhir.storage.MemoryStore.BreakDelta;
import com.musbabaff.menhir.storage.MemoryStore.ChangeSet;
import com.musbabaff.menhir.storage.StoneState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Safety net for MySQL: changes that could not be written at shutdown are stored here and replayed
 * on the next start, so a database outage during a restart does not lose hits.
 */
final class PendingFile {

    private final File file;

    PendingFile(File file) {
        this.file = file;
    }

    File getFile() {
        return file;
    }

    void write(ChangeSet changes, Logger logger) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("reset-all", changes.resetAll());
        yaml.set("reset-blocks", new ArrayList<>(changes.resetBlocks()));
        yaml.set("reset-players", changes.resetPlayers().stream().map(UUID::toString).toList());
        yaml.set("deleted-blocks", new ArrayList<>(changes.deletedBlocks()));
        List<Map<String, Object>> breaks = new ArrayList<>();
        for (BreakDelta delta : changes.breaks()) {
            breaks.add(Map.of("block", delta.blockId(), "uuid", delta.player().toString(), "name", delta.name(), "amount", delta.amount()));
        }
        yaml.set("breaks", breaks);
        for (Map.Entry<String, StoneState> entry : changes.states().entrySet()) {
            String path = "states." + entry.getKey().replace('.', '_');
            yaml.set(path + ".id", entry.getKey());
            yaml.set(path + ".health", entry.getValue().health());
            yaml.set(path + ".broken", entry.getValue().broken());
            yaml.set(path + ".respawn-at", entry.getValue().respawnAt());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not write " + file.getName(), e);
        }
    }

    /** Reads and deletes the file; returns {@code null} when there is nothing pending. */
    ChangeSet readAndDelete(Logger logger) {
        if (!file.isFile()) return null;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        Set<UUID> players = new LinkedHashSet<>();
        for (String raw : yaml.getStringList("reset-players")) {
            try {
                players.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                // skip
            }
        }
        List<BreakDelta> breaks = new ArrayList<>();
        for (Map<?, ?> entry : yaml.getMapList("breaks")) {
            try {
                breaks.add(new BreakDelta(String.valueOf(entry.get("block")), UUID.fromString(String.valueOf(entry.get("uuid"))),
                        String.valueOf(entry.get("name")), Integer.parseInt(String.valueOf(entry.get("amount")))));
            } catch (RuntimeException ignored) {
                // skip malformed rows
            }
        }
        Map<String, StoneState> states = new LinkedHashMap<>();
        ConfigurationSection statesSection = yaml.getConfigurationSection("states");
        if (statesSection != null) {
            for (String key : statesSection.getKeys(false)) {
                ConfigurationSection s = statesSection.getConfigurationSection(key);
                if (s == null) continue;
                states.put(s.getString("id", key), new StoneState(s.getInt("health"), s.getBoolean("broken"), s.getLong("respawn-at")));
            }
        }
        ChangeSet changes = new ChangeSet(yaml.getBoolean("reset-all"),
                new LinkedHashSet<>(yaml.getStringList("reset-blocks")), players,
                new LinkedHashSet<>(yaml.getStringList("deleted-blocks")), breaks, states, Map.of());
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not delete " + file.getName(), e);
        }
        return changes;
    }

}
