/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.integration;

import java.util.UUID;

/**
 * Supplies a chat prefix for a player, used by the {@code %player_N_prefix%} hologram placeholders.
 */
public interface PrefixProvider {

    /**
     * @return the prefix or {@code null} if none is known
     */
    String provide(UUID player);

}
