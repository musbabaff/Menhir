/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigUtil {

    private static final Map<String, Material> MATERIALS = new HashMap<>();

    static {
        for (Material value : Material.values()) {
            MATERIALS.put(value.name(), value);
        }
    }

    public static Material getMaterial(String name) {
        return MATERIALS.get(name);
    }

    public static Optional<Material> getMaterialOpt(String name) {
        return Optional.ofNullable(MATERIALS.get(name));
    }

    public static String getMultiLine(ConfigurationSection section, String key) {
        if (section.isList(key)) return String.join("\n", section.getStringList(key));
        return section.getString(key);
    }

    public static void setMultiLine(ConfigurationSection section, String key, String value) {
        String[] lines = value.split("\n");
        if (lines.length <= 1) section.set(key, value);
        else section.set(key, List.of(lines));
    }

    private ConfigUtil() {}

}
