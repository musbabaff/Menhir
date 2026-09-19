/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.countdown;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps enum-style sound names ({@code BLOCK_NOTE_BLOCK_PLING}) to registry keys
 * ({@code minecraft:block.note_block.pling}). The registry is scanned once on first use.
 */
final class SoundNames {

    private static Map<String, Key> byEnumName;

    private SoundNames() {
    }

    static synchronized Key keyOf(String enumName) {
        if (byEnumName == null) {
            Map<String, Key> map = new HashMap<>();
            Registry<Sound> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT);
            for (Sound sound : registry) {
                NamespacedKey namespaced = registry.getKey(sound);
                if (namespaced == null) continue;
                Key key = Key.key(namespaced.getNamespace(), namespaced.getKey());
                map.put(namespaced.getKey().replace('.', '_').toUpperCase(Locale.ROOT), key);
            }
            byEnumName = map;
        }
        return byEnumName.get(enumName.trim().toUpperCase(Locale.ROOT));
    }

}
