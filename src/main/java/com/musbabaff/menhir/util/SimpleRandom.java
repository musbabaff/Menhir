/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.util;

import java.util.*;

public class SimpleRandom<E> {

    private final NavigableMap<Double, E> map = new TreeMap<>();
    private double total = 0;

    public SimpleRandom<E> add(double weight, E result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public E next(Random random) {
        double value = random.nextDouble() * total;
        return Optional.ofNullable(map.higherEntry(value)).map(Map.Entry::getValue).orElse(null);
    }

    public void clear() {
        map.clear();
    }

}
