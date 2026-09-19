/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.item;

import com.musbabaff.menhir.gui.filler.GuiFiller;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.item.state.updater.StateUpdater;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractItem<T> extends GuiItemImpl<T> {

    protected AbstractItem(GuiFiller<?> parent, T state) {
        this(parent, new ItemStack(Material.AIR), state, null);
    }

    protected AbstractItem(GuiFiller<?> parent, ItemStack noStatePlaceholder, T state, StateUpdater<T> stateUpdater) {
        super();
        super.setParent(parent);
        super.setNoStatePlaceholder(noStatePlaceholder);
        if (state != null) setState(state);
        super.setStateUpdater(stateUpdater);
    }

    @Override
    public void onClick(ItemClickEvent<T> itemClickEvent) {
        click(itemClickEvent);
    }

    @Override
    public ItemStack render(int slot) {
        return render(slot, getState());
    }

    public abstract void click(ItemClickEvent<T> itemClickEvent);
    public ItemStack render(int slot, T state) {
        return render(state);
    }
    public ItemStack render(T state) {
        return null;
    }

}
