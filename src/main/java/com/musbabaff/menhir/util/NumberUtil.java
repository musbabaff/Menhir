/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.util;

import java.util.Optional;
import java.util.regex.Pattern;

public class NumberUtil {

    private static final Pattern INT_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[0-9]+?(\\.[0-9]+)");


    public static Optional<Integer> parseInt(String s) {
        if (!INT_PATTERN.matcher(s).matches()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(Integer.parseInt(s));
            } catch (Throwable t) {
                return Optional.empty();
            }
        }
    }

    public static Optional<Float> parseFloat(String s) {
        if (!FLOAT_PATTERN.matcher(s).matches()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(Float.parseFloat(s));
            } catch (Throwable t) {
                return Optional.empty();
            }
        }
    }

}
