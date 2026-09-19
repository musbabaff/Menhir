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
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.item.render.Renderer;
import com.musbabaff.menhir.gui.item.state.StateHandler;
import com.musbabaff.menhir.gui.item.state.updater.StateUpdater;
import com.musbabaff.menhir.gui.refresher.GuiRefresher;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class GuiItemImpl<T> implements GuiItem<T> {

    private GuiFiller<?> parent;
    private ItemStack noStatePlaceholder;
    private ClickHandler<T> clickHandler;
    private Renderer<T> renderer;
    private Optional<T> state = Optional.empty();
    private StateUpdater<T> stateUpdater;

    public GuiItemImpl() {
    }

    public GuiItemImpl(GuiFiller<?> parent, ItemStack noStatePlaceholder, ClickHandler<T> clickHandler, Renderer<T> renderer, Optional<T> state, StateUpdater<T> stateUpdater) {
        this.parent = parent;
        this.noStatePlaceholder = noStatePlaceholder;
        this.clickHandler = clickHandler;
        this.renderer = renderer;
        this.state = state;
        this.stateUpdater = stateUpdater;
    }

    @Override
    public T getState() {
        return state.orElse(null);
    }

    @Override
    public void setState(T state) {
        this.state.ifPresent(t -> {
            if (t instanceof StateHandler) {
                ((StateHandler) t).onRemove(GuiItemImpl.this);
            }
        });
        this.state = Optional.of(state);
        if (state instanceof StateHandler) {
            ((StateHandler) state).onAdd(this);
        }
        stateUpdated();
    }

    @Override
    public void stateUpdated() {
        parent.stateUpdated(this);
    }

    @Override
    public ItemStack render(int slot) {
        if (state.isEmpty()) return noStatePlaceholder;
        return renderer.render(slot, state.get());
    }

    @Override
    public void onClick(ItemClickEvent<T> itemClickEvent) {
        if (clickHandler == null) return;
        clickHandler.onClick(itemClickEvent);
    }

    @Override
    public boolean requiresPerSlotRendering() {
        if (renderer == null) return false;
        return renderer.requiresPerSlotRendering();
    }

    @Override
    public Optional<StateUpdater<T>> getStateUpdater() {
        return Optional.ofNullable(stateUpdater);
    }

    @Override
    public void setStateUpdater(StateUpdater<T> updater) {
        this.stateUpdater = updater;
        GuiRefresher refresher = parent.getParent().getRefresher();
        refresher.remove(this);
        refresher.add(this);
    }

    @Override
    public boolean hasState() {
        return state.isPresent();
    }

    void setParent(GuiFiller<?> parent) {
        this.parent = parent;
    }

    void setNoStatePlaceholder(ItemStack noStatePlaceholder) {
        this.noStatePlaceholder = noStatePlaceholder;
    }

    void setClickHandler(ClickHandler<T> clickHandler) {
        this.clickHandler = clickHandler;
    }

    void setRenderer(Renderer<T> renderer) {
        this.renderer = renderer;
    }
}
