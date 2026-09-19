/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.tool.material;

import com.musbabaff.menhir.block.tool.Result;
import com.musbabaff.menhir.util.ConfigUtil;
import org.bukkit.Material;

import java.util.List;
import java.util.function.Predicate;

public interface MaterialFilter extends Predicate<Material> {

    static MaterialFilter parse(String value, Result result) {
        return ConfigUtil.getMaterialOpt(value)
                .map(s -> new SingleMaterialFilter(s, result))
                .map(p -> (MaterialFilter) p)
                .orElseGet(() -> new ContainsMaterialFilter(
                        value, result
                ));
    }

    static Result matches(Material type, List<MaterialFilter> filters, Result defaultResult) {
        Result result = defaultResult;

        for (MaterialFilter filter : filters) {
            if (filter.test(type)) {
                result = filter.getResult();
            }
        }

        return result;
    }

    Result getResult();
    void setResult(Result result);

}
