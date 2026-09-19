/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.tool.enchantment;

import com.musbabaff.menhir.block.tool.Result;
import com.musbabaff.menhir.util.range.NumberRange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class ToolEnchantment {

    public static Result matches(Map<Enchantment, Integer> enchantments, Map<Enchantment, ToolEnchantment> toolEnchantments, Result defaultType) {
        boolean defaultValue = defaultType.getBooleanValue();

        return Result.fromBoolean(toolEnchantments.entrySet().stream().noneMatch(entry -> {
            int level = enchantments.getOrDefault(entry.getKey(), -1);
            return level < 0 && !defaultValue || level >= 0 && entry.getValue().result(level).equals(Result.DENIED);
        }));
    }

    public static Optional<ToolEnchantment> parse(ConfigurationSection section) {
        Result type = Result.getByName(section.getString("type", ""))
                .orElseThrow(() -> new IllegalArgumentException("Invalid result type for enchantment " + section.getName()));
        Optional<NumberRange> numberRange = NumberRange.parse(section.getString("level", ""));
        return Optional.of(new ToolEnchantment(
                numberRange.orElse(null),
                type
        ));
    }

    private final NumberRange range;
    private Result result;

    private Result result(int level) {
        if (range == null || range.test(level)) return result;
        return result.getOther();
    }

}
