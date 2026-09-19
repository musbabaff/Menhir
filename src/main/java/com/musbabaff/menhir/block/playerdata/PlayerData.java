/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.playerdata;

import com.musbabaff.menhir.util.serializable.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class PlayerData implements Serializable {

    public static PlayerData deserialize(DataInput input) throws IOException {
        return new PlayerData(new UUID(input.readLong(), input.readLong()), input.readUTF(), input.readInt());
    }

    private final UUID uuid;
    private final String displayName;
    private int breaks;

    public void incrementBreaks() {
        breaks++;
    }

    @Override
    public void serialize(DataOutput output) throws IOException {
        output.writeLong(uuid.getMostSignificantBits());
        output.writeLong(uuid.getLeastSignificantBits());
        output.writeUTF(displayName);
        output.writeInt(breaks);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

}
