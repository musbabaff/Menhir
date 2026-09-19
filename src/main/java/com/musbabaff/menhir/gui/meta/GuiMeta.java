/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.meta;

import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.filler.GuiFiller;
import com.musbabaff.menhir.gui.type.InventoryType;
import net.kyori.adventure.text.Component;

public class GuiMeta<F extends GuiFiller<F>> {

    private final F filler;
    private final Component title;
    private final InventoryType type;

    public GuiMeta(F filler, Component title, InventoryType type) {
        this.filler = filler;
        this.title = title;
        this.type = type;
    }

    public F getFiller() {
        return filler;
    }

    public Component getTitle() {
        return title;
    }

    public InventoryType getType() {
        return type;
    }

    public GuiMeta<F> withParent(Gui<F> parent) {
        return new GuiMeta<>(filler.withParent(parent, this), title, type);
    }

}
