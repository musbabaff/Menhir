/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.util.range;

import java.util.Objects;

public class StaticNumber implements NumberRange {

    private int value;

    public StaticNumber(int value) {
        this.value = value;
    }

    @Override
    public int getMin() {
        return value;
    }

    @Override
    public int getMax() {
        return value;
    }

    @Override
    public boolean test(Integer integer) {
        return Objects.equals(value, integer);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
