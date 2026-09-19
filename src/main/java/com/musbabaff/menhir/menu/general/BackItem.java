/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.general;

import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.filler.GuiFiller;
import com.musbabaff.menhir.gui.item.AbstractItem;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

public class BackItem extends AbstractItem<Void> {

    private final Supplier<Gui<?>> guiSupplier;

    public BackItem(GuiFiller<?> parent, Supplier<Gui<?>> guiSupplier) {
        super(parent, null);
        this.guiSupplier = guiSupplier;
    }

    @Override
    public void click(ItemClickEvent<Void> itemClickEvent) {
        guiSupplier.get().open(itemClickEvent.getPlayer());
    }

    @Override
    public ItemStack render(Void state) {
        return ItemStackBuilder.create(Material.ARROW)
                .withName(MineDown.parse("&#DF2E38&Go back"))
                .build();
    }
}
