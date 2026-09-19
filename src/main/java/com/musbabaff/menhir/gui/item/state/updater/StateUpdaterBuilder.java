/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.item.state.updater;

import com.musbabaff.menhir.gui.item.GuiItem;

import java.time.Duration;

public class StateUpdaterBuilder<T> {

    public static <T> StateUpdaterBuilder<T> sameStateUpdater(Class<T> forClass) {
        return new StateUpdaterBuilder<>(GuiItem::getState);
    }

    private final StateSupplier<T> stateSupplier;
    private Duration repeatInterval;
    private boolean sync = false;

    public StateUpdaterBuilder(StateSupplier<T> stateSupplier) {
        this.stateSupplier = stateSupplier;
    }

    public StateUpdaterBuilder<T> withRefreshRate(Duration repeatInterval) {
        this.repeatInterval = repeatInterval;
        return this;
    }

    public StateUpdaterBuilder<T> sync() {
        return sync(true);
    }

    public StateUpdaterBuilder<T> sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public StateUpdater<T> build() {
        return new StateUpdaterImpl<>(repeatInterval, sync, stateSupplier);
    }

}
