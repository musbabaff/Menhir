/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.filler;

import com.musbabaff.menhir.gui.Gui;
import com.musbabaff.menhir.gui.item.GuiItem;
import com.musbabaff.menhir.gui.meta.GuiMeta;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface GuiFiller<S extends GuiFiller<S>> {

    Gui<S> getParent();
    GuiItem<?> getItem(int slot);
    Collection<GuiItem<?>> getItems();
    Collection<Integer> getSlots(GuiItem<?> item);
    CompletableFuture<Void> stateUpdated(GuiItem<?> item);
    S withParent(Gui<S> parent, GuiMeta<S> meta);
    CompletableFuture<Void> renderAll();

}
