/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.item.state.multi;

import com.musbabaff.menhir.gui.item.state.StateHandler;

public class TriState<A, B, C> extends BiState<A, B> implements StateHandler {

    private C c;

    public TriState(A a, B b, C c) {
        super(a, b);
        this.c = c;
    }

    public TriState() {
    }

    @Override
    public TriState<A, B, C> withA(A a) {
        super.withA(a);
        return this;
    }

    @Override
    public TriState<A, B, C> withB(B b) {
        super.withB(b);
        return this;
    }

    public C getC() {
        return c;
    }

    public void setC(C c) {
        this.c = c;
        updated();
    }

    public TriState<A, B, C> withC(C c) {
        this.c = c;
        return this;
    }

}
