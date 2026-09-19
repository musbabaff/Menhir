/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Static access to the {@link MenhirAPI}.
 *
 * <pre>{@code
 * MenhirAPI api = MenhirProvider.get();
 * api.getStone("north").ifPresent(stone -> ...);
 * }</pre>
 */
public final class MenhirProvider {

    private static MenhirAPI instance;

    private MenhirProvider() {
    }

    /**
     * Returns the API.
     *
     * @return the API
     * @throws IllegalStateException if Menhir is not enabled (yet); depend on it via
     *                               {@code softdepend: [Menhir]} or {@code depend: [Menhir]}
     */
    public static @NotNull MenhirAPI get() {
        MenhirAPI api = instance;
        if (api == null) {
            throw new IllegalStateException("Menhir is not enabled. Add 'Menhir' to the depend/softdepend list of your plugin.yml "
                    + "and call MenhirProvider.get() no earlier than your onEnable().");
        }
        return api;
    }

    /**
     * Called by Menhir when it enables and disables; not part of the public API.
     *
     * @param api the implementation, or {@code null} on disable
     */
    @ApiStatus.Internal
    public static void register(MenhirAPI api) {
        instance = api;
    }

}
