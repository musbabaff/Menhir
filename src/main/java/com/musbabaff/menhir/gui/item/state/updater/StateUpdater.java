/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.item.state.updater;

import java.time.Duration;
import java.util.Optional;

public interface StateUpdater<T> extends StateSupplier<T> {

    static <T> StateUpdaterBuilder<T> builder(StateSupplier<T> stateSupplier) {
        return new StateUpdaterBuilder<>(stateSupplier);
    }

    Optional<Duration> getRefreshInterval();
    default boolean isRefreshing() {
        return getRefreshInterval().isPresent();
    }
    boolean isSync();

}
