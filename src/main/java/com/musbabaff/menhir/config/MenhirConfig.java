/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config;

import com.musbabaff.menhir.config.blocks.BlocksConfig;
import com.musbabaff.menhir.config.hologram.HologramTemplates;
import com.musbabaff.menhir.config.lang.LangConfig;
import com.musbabaff.menhir.config.options.OptionsConfig;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

/**
 * Root of the parsed {@code config.yml}.
 */
@Getter
public class MenhirConfig {

    private final LangConfig langConfig;
    private final OptionsConfig optionsConfig;
    private final HologramTemplates hologramTemplates;
    private final BlocksConfig blocksConfig;

    public MenhirConfig(ConfigurationSection config, Logger logger) {
        this.langConfig = new LangConfig(config.getConfigurationSection("lang"));
        this.optionsConfig = new OptionsConfig(config.getConfigurationSection("options"), logger);
        this.hologramTemplates = new HologramTemplates(config.getConfigurationSection("hologram-templates"), logger);
        ConfigurationSection blocks = config.getConfigurationSection("blocks");
        if (blocks == null) blocks = config.createSection("blocks");
        this.blocksConfig = new BlocksConfig(blocks, optionsConfig, hologramTemplates);
    }

}
