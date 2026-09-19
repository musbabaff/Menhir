/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage.yaml;

import com.musbabaff.menhir.api.TopEntry;
import com.musbabaff.menhir.storage.MemoryStore.BlockSnapshot;
import com.musbabaff.menhir.storage.StoneState;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reader for the binary {@code storage/<id>.mb} files written by MineBlocks and Menhir 2.0
 * ({@code int health, boolean broken, [long respawnAt], int n, n x (uuid, name, breaks)}).
 * Menhir 2.1 reads them once and continues with {@code <id>.yml}.
 */
public final class LegacyBlockFile {

    public static final String EXTENSION = ".mb";

    private LegacyBlockFile() {
    }

    public static BlockSnapshot read(String blockId, File file) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            int health = in.readInt();
            boolean broken = in.readBoolean();
            long respawnAt = broken ? in.readLong() : 0L;
            int count = in.readInt();
            Map<UUID, TopEntry> players = new LinkedHashMap<>();
            for (int i = 0; i < count; i++) {
                UUID uuid = new UUID(in.readLong(), in.readLong());
                String name = in.readUTF();
                int breaks = in.readInt();
                players.put(uuid, new TopEntry(uuid, name, breaks));
            }
            return new BlockSnapshot(blockId, new StoneState(health, broken, respawnAt), players);
        }
    }

}
