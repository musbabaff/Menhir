/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.options;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * How a status message (AFK, timeout, no permission, invalid tool) is shown to a player.
 */
public enum NotificationType {
    ACTIONBAR {
        @Override
        public void send(Player player, Component message) {
            player.sendActionBar(message);
        }
    },
    CHAT {
        @Override
        public void send(Player player, Component message) {
            player.sendMessage(message);
        }
    },
    TITLE {
        private static final Title.Times TIMES = Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(300));

        @Override
        public void send(Player player, Component message) {
            player.showTitle(Title.title(Component.empty(), message, TIMES));
        }
    },
    NONE {
        @Override
        public void send(Player player, Component message) {
        }
    };

    public static Optional<NotificationType> getByName(String name) {
        if (name == null) return Optional.empty();
        try {
            return Optional.of(valueOf(name.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public abstract void send(Player player, Component message);

}
