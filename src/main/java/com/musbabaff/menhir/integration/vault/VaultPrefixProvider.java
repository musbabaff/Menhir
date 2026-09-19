/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.integration.vault;

import com.musbabaff.menhir.integration.PrefixProvider;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;
import java.util.UUID;

/**
 * Prefix provider backed by the Vault chat service (LuckPerms, etc. register into it).
 */
public final class VaultPrefixProvider implements PrefixProvider {

    private final Server server;
    private final Chat chat;

    private VaultPrefixProvider(Server server, Chat chat) {
        this.server = server;
        this.chat = chat;
    }

    /**
     * @return a provider if Vault is installed and a chat service is registered
     */
    public static Optional<PrefixProvider> create(Server server) {
        if (!server.getPluginManager().isPluginEnabled("Vault")) return Optional.empty();
        RegisteredServiceProvider<Chat> registration = server.getServicesManager().getRegistration(Chat.class);
        if (registration == null) return Optional.empty();
        return Optional.of(new VaultPrefixProvider(server, registration.getProvider()));
    }

    @Override
    public String provide(UUID player) {
        OfflinePlayer offlinePlayer = server.getOfflinePlayer(player);
        String world = Optional.ofNullable(offlinePlayer.getPlayer())
                .map(p -> p.getWorld().getName())
                .orElse(null);
        try {
            return chat.getPlayerPrefix(world, offlinePlayer);
        } catch (Exception e) {
            return null;
        }
    }

}
