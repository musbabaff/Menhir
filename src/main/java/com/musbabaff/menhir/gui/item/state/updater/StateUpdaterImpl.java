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
import java.util.Optional;

public class StateUpdaterImpl<T> implements StateUpdater<T> {

    private final Duration repeatInterval;
    private final boolean sync;
    private final StateSupplier<T> stateSupplier;

    public StateUpdaterImpl(Duration repeatInterval, boolean sync, StateSupplier<T> stateSupplier) {
        this.repeatInterval = repeatInterval;
        this.sync = sync;
        this.stateSupplier = stateSupplier;
    }

    @Override
    public Optional<Duration> getRefreshInterval() {
        return Optional.ofNullable(repeatInterval);
    }

    @Override
    public boolean isSync() {
        return sync;
    }

    @Override
    public T updateState(GuiItem<T> item) {
        return stateSupplier.updateState(item);
    }

}
