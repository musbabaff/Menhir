/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.util.color;

import com.musbabaff.menhir.text.TextRenderer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thin convenience layer over {@link TextRenderer} for sending coloured messages.
 * Kept under its historical name so the command and menu classes stay readable.
 */
public final class Colors {

    private Colors() {
    }

    /** Renders a config/message string into a component (MiniMessage + legacy + hex). */
    public static Component colorize(String string) {
        return TextRenderer.render(string);
    }

    /** Renders a list of strings. */
    public static List<Component> colorize(List<String> list) {
        return list.stream().map(Colors::colorize).collect(Collectors.toList());
    }

    /** Renders and serializes to a legacy {@code §} string for APIs that only accept strings. */
    public static String legacy(String string) {
        return TextRenderer.toLegacy(string);
    }

    /** Sends each line as a separate rendered message. */
    public static void send(CommandSender sender, String... message) {
        for (String line : message) {
            sender.sendMessage(colorize(line));
        }
    }

}
