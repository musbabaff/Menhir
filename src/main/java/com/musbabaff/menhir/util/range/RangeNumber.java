/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.util.range;

public class RangeNumber implements NumberRange {

    private int min;
    private int max;

    public RangeNumber(int n1, int n2) {
        this.min = Math.min(n1, n2);
        this.max = Math.max(n1, n2);
    }

    @Override
    public int getMin() {
        return min;
    }

    @Override
    public int getMax() {
        return max;
    }

    @Override
    public boolean test(Integer integer) {
        if (integer == null) return false;
        return integer <= max && integer >= min;
    }

    @Override
    public String toString() {
        return min + "-" + max;
    }

}
