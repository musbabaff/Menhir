/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.hologram;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The {@code hologram-templates} section: named, reusable hologram settings.
 */
public final class HologramTemplates {

    /** Name of the template that is used when a block does not reference one explicitly. */
    public static final String DEFAULT_NAME = "default";

    private final Map<String, HologramSettings> templates;

    public HologramTemplates(ConfigurationSection section, Logger logger) {
        Map<String, HologramSettings> map = new LinkedHashMap<>();
        if (section != null) {
            for (String name : section.getKeys(false)) {
                ConfigurationSection templateSection = section.getConfigurationSection(name);
                if (templateSection == null) continue;
                try {
                    map.put(name, HologramSettings.parse(templateSection));
                } catch (IllegalArgumentException e) {
                    logger.warning("Hologram template '" + name + "' could not be loaded: " + e.getMessage());
                }
            }
        }
        this.templates = Collections.unmodifiableMap(map);
    }

    public Optional<HologramSettings> get(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(templates.get(name));
    }

    public Set<String> getNames() {
        return templates.keySet();
    }

    public boolean isEmpty() {
        return templates.isEmpty();
    }

}
