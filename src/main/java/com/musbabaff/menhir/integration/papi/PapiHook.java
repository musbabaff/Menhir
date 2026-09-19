/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.integration.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * Safe access to PlaceholderAPI. Every call checks whether the plugin is present, so the rest of
 * Menhir never touches PlaceholderAPI classes when it is not installed.
 */
public final class PapiHook {

    private static final String PLUGIN_NAME = "PlaceholderAPI";

    private PapiHook() {
    }

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
    }

    /**
     * Replaces PlaceholderAPI placeholders. Returns the input unchanged when PlaceholderAPI is not
     * installed or the text contains no {@code %}.
     *
     * @param player the player context, may be {@code null} for server-wide placeholders
     */
    public static String setPlaceholders(OfflinePlayer player, String text) {
        if (text == null || text.indexOf('%') < 0 || !isAvailable()) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

}
