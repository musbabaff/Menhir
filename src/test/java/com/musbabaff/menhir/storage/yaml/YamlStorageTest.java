/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 */
package com.musbabaff.menhir.storage.yaml;

import com.musbabaff.menhir.api.TopEntry;
import com.musbabaff.menhir.storage.StoneState;
import com.musbabaff.menhir.storage.StorageMigration;
import com.musbabaff.menhir.storage.StorageProvider;
import com.musbabaff.menhir.storage.StorageProviderContractTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlStorageTest extends StorageProviderContractTest {

    private static final Logger LOGGER = Logger.getLogger("menhir-test");

    @TempDir
    Path folder;

    @Override
    protected StorageProvider createProvider() {
        YamlStorage yaml = new YamlStorage(folder.toFile(), LOGGER);
        yaml.load().join();
        return yaml;
    }

    @Override
    protected StorageProvider reopen() {
        return createProvider();
    }

    @Test
    @DisplayName("a legacy MineBlocks .mb file is imported once and converted to .yml")
    void importsLegacyFile() throws IOException {
        File legacy = new File(folder.toFile(), "old.mb");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(legacy))) {
            out.writeInt(77);          // health
            out.writeBoolean(true);    // broken
            out.writeLong(4242L);      // respawn at
            out.writeInt(1);           // players
            out.writeLong(ALICE.getMostSignificantBits());
            out.writeLong(ALICE.getLeastSignificantBits());
            out.writeUTF("Alice");
            out.writeInt(9);
        }
        storage.close();
        storage = reopen();
        assertEquals(new StoneState(77, true, 4242L), storage.getState("old").orElseThrow());
        assertEquals(9, storage.getBreaks("old", ALICE));
        assertTrue(new File(folder.toFile(), "old.yml").isFile(), "converted file written");
        assertTrue(legacy.isFile(), "legacy file left in place");
    }

    @Test
    @DisplayName("migration copies data, skips existing rows unless --overwrite, and writes a backup")
    void migration() throws IOException {
        storage.addBreaks("north", ALICE, "Alice", 3);
        storage.addBreaks("north", BOB, "Bob", 5);
        storage.setState("north", 12, false, 0L);

        File targetFolder = new File(folder.toFile(), "target");
        YamlStorage target = new YamlStorage(targetFolder, LOGGER);
        target.load().join();
        target.addBreaks("north", ALICE, "Alice", 100); // pre-existing row, must be skipped
        List<String> progress = new ArrayList<>();
        StorageMigration.Result result = StorageMigration.migrate(storage, target, new File(folder.toFile(), "backups"), false, progress::add);
        assertEquals(1, result.blocksCopied());
        assertEquals(1, result.skipped());
        assertTrue(result.backup().isFile());
        assertTrue(progress.stream().anyMatch(line -> line.contains("north")));

        YamlStorage reopened = new YamlStorage(targetFolder, LOGGER);
        reopened.load().join();
        assertEquals(100, reopened.getBreaks("north", ALICE), "existing row kept");
        assertEquals(5, reopened.getBreaks("north", BOB));
        assertEquals(new StoneState(12, false, 0L), reopened.getState("north").orElseThrow());
        reopened.close();

        YamlStorage overwritten = new YamlStorage(targetFolder, LOGGER);
        overwritten.load().join();
        StorageMigration.migrate(storage, overwritten, new File(folder.toFile(), "backups"), true, progress::add);
        YamlStorage check = new YamlStorage(targetFolder, LOGGER);
        check.load().join();
        assertEquals(3, check.getBreaks("north", ALICE), "--overwrite replaces the row");
        assertEquals(List.of(new TopEntry(BOB, "Bob", 5), new TopEntry(ALICE, "Alice", 3)), check.getTop("north", 10));
        check.close();
    }

    @Test
    @DisplayName("only changed stones are rewritten")
    void writesOnlyDirtyFiles() {
        storage.addBreaks("north", ALICE, "Alice", 1);
        storage.addBreaks("south", BOB, "Bob", 1);
        storage.close();
        File south = new File(folder.toFile(), "south.yml");
        long stamp = south.lastModified();
        storage = reopen();
        storage.addBreaks("north", ALICE, "Alice", 1);
        storage.close();
        assertEquals(stamp, south.lastModified(), "untouched stone must not be rewritten");
        assertEquals(2, reopen().getBreaks("north", ALICE));
        UUID unknown = UUID.randomUUID();
        assertEquals(0, storage.getBreaks("north", unknown));
    }

}
