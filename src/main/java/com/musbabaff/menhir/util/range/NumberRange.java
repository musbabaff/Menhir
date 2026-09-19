/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.util.range;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface NumberRange extends Predicate<Integer> {

    Pattern RANGE_PATTERN = Pattern.compile("\\d*-\\d*");
    Pattern STATIC_PATTERN = Pattern.compile("\\d*");

    static Optional<NumberRange> parse(String value) {
        if (value == null || value.isBlank()) return Optional.empty();
        if (value.contains(",")) {
            return Optional.of(
                    new MultiNumberRange(
                            Arrays.stream(value.split(","))
                                    .map(NumberRange::parse)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList())
                    )
            );
        }
        if (RANGE_PATTERN.matcher(value).matches()) {
            String[] values = value.split("-");
            int n1 = Integer.parseInt(values[0]);
            int n2 = Integer.parseInt(values[1]);
            return Optional.of(new RangeNumber(n1, n2));
        } else if (STATIC_PATTERN.matcher(value).matches()) {
            return Optional.of(new StaticNumber(Integer.parseInt(value)));
        }
        return Optional.empty();
    }

    int getMin();
    int getMax();

}
