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
import com.musbabaff.menhir.gui.item.click.ClickHandler;
import com.musbabaff.menhir.gui.item.render.Renderer;
import com.musbabaff.menhir.gui.item.state.updater.StateUpdater;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class GuiItemBuilder<T> {

    private final GuiFiller<?> parent;
    private final Renderer<T> renderer;
    private final boolean hasState;
    private T defaultState;
    private StateUpdater<T> stateUpdater;
    private ItemStack noStatePlaceholder;
    private ClickHandler<T> clickHandler;

    public GuiItemBuilder(GuiFiller<?> parent, Renderer<T> renderer) {
        this.parent = parent;
        this.renderer = renderer;
        this.hasState = true;
    }

    public GuiItemBuilder(GuiFiller<?> parent, ItemStack itemStack) {
        this.parent = parent;
        this.renderer = (slot, state) -> itemStack;
        noStatePlaceholder = itemStack;
        this.hasState = false;
    }


    public GuiItemBuilder<T> withDefaultState(T defaultState) {
        if (!hasState) throw new IllegalStateException("This GuiItem does not have a state");
        this.defaultState = defaultState;
        return this;
    }

    public GuiItemBuilder<T> withStateUpdater(StateUpdater<T> stateUpdater) {
        if (!hasState) throw new IllegalStateException("This GuiItem does not have a state");
        this.stateUpdater = stateUpdater;
        return this;
    }

    public GuiItemBuilder<T> withNoStatePlaceholder(ItemStack noStatePlaceholder) {
        if (!hasState) throw new IllegalStateException("This GuiItem does not have a state");
        this.noStatePlaceholder = noStatePlaceholder;
        return this;
    }

    public GuiItemBuilder<T> withClickHandler(ClickHandler<T> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    public GuiItem<T> build() {
        return new GuiItemImpl<>(parent, noStatePlaceholder, clickHandler, renderer, hasState ? Optional.ofNullable(defaultState) : Optional.empty(), stateUpdater);
    }

}
