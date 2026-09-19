/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.resetopt;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.gui.filler.map.MapGuiFiller;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.edit.EditMenu;
import com.musbabaff.menhir.menu.edit.resetopt.items.InactiveMessageItem;
import com.musbabaff.menhir.menu.edit.resetopt.items.InactiveTimeItem;
import com.musbabaff.menhir.menu.edit.resetopt.items.OnRestartItem;
import com.musbabaff.menhir.menu.general.BackItem;
import com.musbabaff.menhir.menu.general.LearnMoreItem;
import net.kyori.adventure.text.Component;

public class ResetOptionsMenu extends BlockMenu<MapGuiFiller> {

    public ResetOptionsMenu(MenhirBlock block) {
        super(new MapGuiFiller(
                "         ",
                "  a b c  ",
                "h       x"
        ), Component.text(block.getId() + " | Reset options"), InventoryType.CHEST_3, block);

        MapGuiFiller filler = getFiller();

        filler.setItem('a', new InactiveTimeItem(this));
        filler.setItem('b', new InactiveMessageItem(this));
        filler.setItem('c', new OnRestartItem(this));

        filler.setItem('h', new LearnMoreItem(filler));
        filler.setItem('x', new BackItem(filler, () -> new EditMenu(getBlock())));
    }

}
