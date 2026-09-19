/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.item.state.multi;

import com.musbabaff.menhir.gui.item.GuiItem;
import com.musbabaff.menhir.gui.item.state.StateHandler;

import java.util.LinkedHashSet;
import java.util.Set;

public class BiState<A, B> implements StateHandler {

    private final Set<GuiItem<?>> items = new LinkedHashSet<>();
    private A a;
    private B b;

    public BiState(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public BiState() {
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
        updated();
    }

    public BiState<A, B> withA(A a) {
        this.a = a;
        return this;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
        updated();
    }

    public BiState<A, B> withB(B b) {
        this.b = b;
        return this;
    }

    protected void updated() {
        for (GuiItem<?> item : items) {
            item.stateUpdated();
        }
    }

    @Override
    public void onAdd(GuiItem<?> item) {
        items.add(item);
    }

    @Override
    public void onRemove(GuiItem<?> item) {
        items.remove(item);
    }

}
