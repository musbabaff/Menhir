/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.item;

import com.musbabaff.menhir.gui.item.click.ClickHandler;
import com.musbabaff.menhir.gui.item.state.updater.StateUpdater;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface GuiItem<T> extends ClickHandler<T> {

    T getState();
    void setState(T state);
    void stateUpdated();
    ItemStack render(int slot);
    boolean requiresPerSlotRendering();
    Optional<StateUpdater<T>> getStateUpdater();
    void setStateUpdater(StateUpdater<T> updater);
    boolean hasState();

}
