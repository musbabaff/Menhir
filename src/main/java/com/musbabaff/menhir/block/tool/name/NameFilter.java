/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.tool.name;

import com.musbabaff.menhir.block.tool.Result;
import com.musbabaff.menhir.text.TextRenderer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Matches an item display name against a configured name. Formatting is ignored:
 * only the visible text is compared, so colour differences do not affect the result.
 */
@Getter
@Setter
@AllArgsConstructor
public class NameFilter implements Predicate<Component> {

    public static Result matches(Component displayName, List<NameFilter> filters, Result defaultResult) {
        Result result = defaultResult;

        for (NameFilter filter : filters) {
            if (filter.test(displayName)) {
                result = filter.getResult();
            }
        }

        return result;
    }

    private final String name;
    private Result result;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean test(Component displayName) {
        String actual = displayName == null ? "" : TextRenderer.plain(displayName);
        return TextRenderer.strip(name).equals(actual);
    }

}
