/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.util.result;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<R, E> {

    public static <R, E> Result<R, E> success(R result) {
        return new Result<>(result, null);
    }

    public static <R, E> Result<R, E> error(E error) {
        return new Result<>(null, error);
    }

    private final R result;
    private final E error;

    public boolean isSuccessful() {
        return error == null;
    }

    public void ifSuccessfulOrElse(Consumer<R> success, Consumer<E> error) {
        if (isSuccessful()) {
            success.accept(result);
        } else error.accept(this.error);
    }

}
