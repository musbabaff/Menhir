/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.block.rewards;

import com.musbabaff.menhir.block.rewards.breakcount.BreakCountReward;
import com.musbabaff.menhir.block.rewards.breaks.BreakReward;
import com.musbabaff.menhir.block.rewards.breaks.condition.BreakCondition;
import com.musbabaff.menhir.block.rewards.breaks.condition.IntervalCondition;
import com.musbabaff.menhir.block.rewards.commands.RewardCommands;
import com.musbabaff.menhir.block.rewards.top.TopReward;
import com.musbabaff.menhir.util.range.NumberRange;
import com.musbabaff.menhir.util.range.RangeNumber;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public enum RewardType {
    TOP {
        @SneakyThrows
        @Override
        public Reward parse(String name, ConfigurationSection section) {
            return new TopReward(
                    name,
                    NumberRange.parse(section.getString("place", "-1"))
                            .orElseThrow((Supplier<Throwable>) () -> new IllegalStateException("Invalid place in top reward named " + name)),
                    RewardCommands.parse(section.getString("mode"), section.getStringList("commands"))
            );
        }

        @Override
        public void set(ConfigurationSection section, Reward reward) {
            if (reward instanceof TopReward) {
                TopReward topReward = (TopReward) reward;
                section.set("place", topReward.getRange().toString());
                section.set("commands", topReward.getCommands().saveToList());
            }
        }
    },
    BREAK {

        @SneakyThrows
        @Override
        public Reward parse(String name, ConfigurationSection section) {
            Optional<BreakCondition> condition;
            Object intervalVal = section.get("interval");
            Object conditionVal = section.get("condition");
            if (intervalVal != null) {
                condition = Optional.of(new IntervalCondition(String.valueOf(intervalVal)));
            } else if (conditionVal != null) {
                condition = BreakCondition.parse(String.valueOf(conditionVal));
            }
            else {
                condition = Optional.empty();
            }
            return new BreakReward(
                    name,
                    condition
                            .orElseThrow((Supplier<Throwable>) () -> new IllegalStateException("Invalid interval/condition in break reward named " + name)),
                    RewardCommands.parse(section.getString("mode"), section.getStringList("commands"))
            );
        }

        @Override
        public void set(ConfigurationSection section, Reward reward) {
            if (reward instanceof BreakReward) {
                BreakReward breakReward = (BreakReward) reward;
                if (breakReward.getCondition() != null)
                    section.set(
                            breakReward.getCondition() instanceof IntervalCondition ? "interval" : "condition", breakReward.getCondition().toString()
                    );
                section.set("commands", breakReward.getCommands().saveToList());
            }
        }
    },
    BREAK_COUNT {
        @SneakyThrows
        @Override
        public Reward parse(String name, ConfigurationSection section) {
            int from = section.getInt("from", -1);
            if (from < 0) throw new IllegalStateException("Invalid \"from\" value in break count reward named " + name);
            int to = section.getInt("to", -1);
            if (to < 0) throw new IllegalStateException("Invalid \"to\" value in break count reward named " + name);
            return new BreakCountReward(
                    name,
                    new RangeNumber(from, to),
                    RewardCommands.parse(section.getString("mode"), section.getStringList("commands"))
            );
        }

        @Override
        public void set(ConfigurationSection section, Reward reward) {
            if (reward instanceof BreakCountReward) {
                BreakCountReward breakCountReward = (BreakCountReward) reward;
                section.set("from", breakCountReward.getRange().getMin());
                section.set("to", breakCountReward.getRange().getMax());
                section.set("commands", breakCountReward.getCommands().saveToList());
            }
        }
    };

    private static final Map<String, RewardType> TYPES = new HashMap<>();

    static {
        for (RewardType value : values()) {
            TYPES.put(value.name(), value);
        }
    }

    public static Optional<RewardType> getByName(String name) {
        return Optional.ofNullable(TYPES.get(name.toUpperCase()));
    }

    public abstract Reward parse(String name, ConfigurationSection section);
    public abstract void set(ConfigurationSection section, Reward reward);

}
