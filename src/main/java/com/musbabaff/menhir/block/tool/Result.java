/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.tool;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public enum Result {
    ALLOWED(true) {
        @Override
        public Result getOther() {
            return DENIED;
        }
    },
    DENIED(false) {
        @Override
        public Result getOther() {
            return ALLOWED;
        }
    };

    private static final Map<String, Result> TYPES = new HashMap<>();

    static {
        for (Result value : values()) {
            TYPES.put(value.name(), value);
        }
    }

    public static Result fromBoolean(boolean b) {
        return b ? ALLOWED : DENIED;
    }

    public static Optional<Result> getByName(String name) {
        return Optional.ofNullable(TYPES.get(name.toUpperCase()));
    }

    private final boolean booleanValue;

    public abstract Result getOther();

    public boolean getBooleanValue() {
        return booleanValue;
    }

}
