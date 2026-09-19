/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.acf;

import co.aikar.commands.BukkitMessageFormatter;
import com.musbabaff.menhir.util.color.Colors;

/**
 * Lets ACF messages use the same colour syntax as the rest of the plugin.
 */
public class ColorsFormatter extends BukkitMessageFormatter {

    @Override
    public String format(int index, String message) {
        return format(message);
    }

    @Override
    public String format(String message) {
        return Colors.legacy(message);
    }

}
