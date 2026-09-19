/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.config.blocks;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.cooldown.BlockCoolDown;
import com.musbabaff.menhir.block.health.BlockHealth;
import com.musbabaff.menhir.block.hologram.BlockHologram;
import com.musbabaff.menhir.block.messages.BlockMessages;
import com.musbabaff.menhir.block.reset.ResetOptions;
import com.musbabaff.menhir.block.rewards.BlockRewards;
import com.musbabaff.menhir.block.rewards.Reward;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.block.tool.Result;
import com.musbabaff.menhir.block.tool.enchantment.ToolEnchantment;
import com.musbabaff.menhir.block.tool.material.MaterialFilter;
import com.musbabaff.menhir.block.tool.name.NameFilter;
import com.musbabaff.menhir.block.type.BlockType;
import com.musbabaff.menhir.config.afk.AfkSettings;
import com.musbabaff.menhir.config.hologram.HologramSettings;
import com.musbabaff.menhir.config.hologram.HologramTemplates;
import com.musbabaff.menhir.config.options.OptionsConfig;
import com.musbabaff.menhir.util.ConfigUtil;
import org.bukkit.Location;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

/**
 * The {@code blocks} section: parses every block and writes edited blocks back.
 */
public class BlocksConfig {

    /** Keys of a block's hologram section that Menhir interprets; everything else is preserved as-is. */
    private static final Set<String> HOLOGRAM_KEYS = Set.of(
            "offset", "lines", "template", "scale", "billboard", "background", "text-shadow",
            "see-through", "alignment", "view-distance", "update-interval", "line-width");

    private final ConfigurationSection section;
    private final OptionsConfig options;
    private final HologramTemplates templates;

    public BlocksConfig(ConfigurationSection section, OptionsConfig options, HologramTemplates templates) {
        this.section = section;
        this.options = options;
        this.templates = templates;
    }

    public List<MenhirBlock> getBlocks(MenhirPlugin plugin) {
        List<MenhirBlock> blocks = new LinkedList<>();

        for (String id : section.getKeys(false)) {
            try {
                ConfigurationSection blockSection = section.getConfigurationSection(id);
                if (blockSection == null) continue;
                MenhirBlock block = new MenhirBlock(plugin);
                block.setId(id);
                block.setType(new BlockType(block, ConfigUtil.getMaterial(blockSection.getString("type"))));
                block.setHealth(new BlockHealth(block, blockSection.getInt("health")));
                block.setPermission(blockSection.getString("permission"));
                block.setBreakLimit(blockSection.getInt("break-limit", -1));
                block.setLocation(getLocation(block, Objects.requireNonNull(blockSection.getConfigurationSection("location"), "Block " + id + " does not have a location set")));
                block.setHologram(getHologram(block, blockSection.getConfigurationSection("hologram")));
                block.setAfkOverride(AfkSettings.parse(blockSection.getConfigurationSection("afk")));

                ConfigurationSection coolDownSection = blockSection.getConfigurationSection("timeout");
                if (coolDownSection != null) {
                    block.setCoolDown(getCoolDown(block, coolDownSection));
                } else block.setCoolDown(new BlockCoolDown(block, -1, null, ""));

                ConfigurationSection resetOptSection = blockSection.getConfigurationSection("reset");
                if (resetOptSection != null) {
                    block.setResetOptions(getResetOptions(block, resetOptSection));
                } else block.setResetOptions(new ResetOptions(block, false, -1, ""));

                ConfigurationSection messagesSection = blockSection.getConfigurationSection("messages");
                if (messagesSection != null) {
                    block.setMessages(getMessages(messagesSection));
                } else block.setMessages(new BlockMessages(""));

                ConfigurationSection toolSection = blockSection.getConfigurationSection("tool");
                if (toolSection != null) {
                    block.setRequiredTool(getRequiredTool(toolSection));
                } else block.setRequiredTool(new RequiredTool(
                        new LinkedList<>(),
                        Result.ALLOWED,
                        new HashMap<>(),
                        Result.ALLOWED,
                        new LinkedList<>(),
                        Result.ALLOWED
                ));

                ConfigurationSection rewardsSection = blockSection.getConfigurationSection("rewards");
                if (rewardsSection != null) {
                    block.setRewards(getRewards(block, rewardsSection));
                } else block.setRewards(new BlockRewards(block, new LinkedList<>(), new LinkedList<>()));

                block.loadData(MenhirBlock.getStoragePath(plugin, block));

                blocks.add(block);
            } catch (Exception e) {
                plugin.getLogger().warning("Block " + id + " could not be loaded: " + e.getMessage());
            }
        }

        return blocks;
    }

    /**
     * Resolves the hologram settings: built-in defaults ← {@code options.hologram} ← template ← block.
     * The template is the one named in {@code hologram.template}, or {@code default} if that exists.
     */
    private BlockHologram getHologram(MenhirBlock block, ConfigurationSection section) {
        HologramSettings blockSettings = HologramSettings.parse(section);
        String templateName = blockSettings.getTemplate().orElse(HologramTemplates.DEFAULT_NAME);
        HologramSettings template = templates.get(templateName).orElse(null);
        if (template == null && blockSettings.getTemplate().isPresent()) {
            throw new IllegalArgumentException("Hologram template '" + templateName + "' does not exist");
        }
        HologramSettings resolved = options.getHologramDefaults().merge(template).merge(blockSettings);
        if (resolved.getLines().isEmpty()) {
            block.getPlugin().logWarn("Block {0} has no hologram lines (neither in the block nor in a template)", block.getId());
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (!HOLOGRAM_KEYS.contains(key)) extra.put(key, section.get(key));
            }
        }
        return new BlockHologram(block, resolved, blockSettings.hasLines(), extra);
    }

    private Location getLocation(MenhirBlock block, ConfigurationSection section) {
        String worldName = section.getString("world", "");
        return new Location(
                Optional.ofNullable(
                        block.getPlugin().getServer().getWorld(worldName)
                ).orElseThrow(() -> new IllegalArgumentException("Invalid world " + worldName + " configured for block " + block.getId())),
                section.getInt("x"),
                section.getInt("y"),
                section.getInt("z")
        );
    }

    private BlockCoolDown getCoolDown(MenhirBlock block, ConfigurationSection section) {
        return new BlockCoolDown(
                block,
                section.getInt("time", -1),
                ConfigUtil.getMaterial(section.getString("type", "")),
                ConfigUtil.getMultiLine(section, "respawn")
        );
    }

    private ResetOptions getResetOptions(MenhirBlock block, ConfigurationSection section) {
        return new ResetOptions(
                block,
                section.getBoolean("onrestart", false),
                section.getInt("inactive.time", -1),
                ConfigUtil.getMultiLine(section, "inactive.message")
        );
    }

    private BlockMessages getMessages(ConfigurationSection section) {
        return new BlockMessages(
                ConfigUtil.getMultiLine(section, "break")
        );
    }

    private Result parseDefaultResult(String name) {
        if (name == null) return Result.ALLOWED;
        return Result.getByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid result type: "+ name));
    }

    private RequiredTool getRequiredTool(ConfigurationSection section) {
        List<MaterialFilter> materialFilters;
        Result defMaterial = Result.DENIED;
        materialFilters = new LinkedList<>();
        Map<String, String> typeEntries = readTypeEntries(section);
        if (typeEntries != null) {
            for (Map.Entry<String, String> entry : typeEntries.entrySet()) {
                String key = entry.getKey().trim();
                String value = entry.getValue().replace(" ", "");
                if (key.equalsIgnoreCase("default")) {
                    defMaterial = Result.getByName(value)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid result for type default"));
                    continue;
                }
                materialFilters.add(
                        MaterialFilter.parse(key, Result.getByName(value)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid result type for material " + key))
                        )
                );
            }
        } else {
            defMaterial = Result.ALLOWED;
        }

        ConfigurationSection enchantments = section.getConfigurationSection("enchantments");

        Map<Enchantment, ToolEnchantment> enchantmentFilters;
        Result defEnchantment;
        enchantmentFilters = new HashMap<>();
        if (enchantments != null) {
            defEnchantment = parseDefaultResult(enchantments.getString("default"));
            for (String key : enchantments.getKeys(false)) {
                if (key.equalsIgnoreCase("default")) continue;
                Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(key));
                ConfigurationSection enchantSection = enchantments.getConfigurationSection(key);
                if (enchantment == null || enchantSection == null) continue;
                ToolEnchantment.parse(enchantSection).ifPresent(toolEnchantment -> enchantmentFilters.put(
                        enchantment,
                        toolEnchantment
                ));
            }
        } else {
            defEnchantment = Result.ALLOWED;
        }

        ConfigurationSection names = section.getConfigurationSection("names");
        List<NameFilter> nameFilters;
        Result defName;
        nameFilters = new LinkedList<>();
        if (names != null) {
            defName = parseDefaultResult(names.getString("default"));
            for (Map.Entry<String, Object> entry : names.getValues(true).entrySet()) {
                String key = entry.getKey();
                if (key.equalsIgnoreCase("default") || entry.getValue() instanceof ConfigurationSection) continue;

                nameFilters.add(new NameFilter(
                        key,
                        Result.getByName(entry.getValue().toString())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid result type for name " + key))
                ));
            }
        } else {
            defName = Result.ALLOWED;
        }

        return new RequiredTool(
                materialFilters,
                defMaterial,
                enchantmentFilters,
                defEnchantment,
                nameFilters,
                defName
        );
    }

    /**
     * {@code tool.types} may be a list of {@code "NAME: RESULT"} strings (the MineBlocks format) or a map
     * {@code NAME: RESULT}. Returns {@code null} when the key is absent.
     */
    public static Map<String, String> readTypeEntries(ConfigurationSection section) {
        Map<String, String> entries = new LinkedHashMap<>();
        if (section.isList("types")) {
            for (String type : section.getStringList("types")) {
                int colon = type.indexOf(':');
                if (colon < 0) continue;
                entries.put(type.substring(0, colon), type.substring(colon + 1));
            }
            return entries;
        }
        if (section.isConfigurationSection("types")) {
            // Deep values re-join keys that Bukkit split on '.', e.g. the regex "STONE_.*".
            for (Map.Entry<String, Object> entry : section.getConfigurationSection("types").getValues(true).entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection) continue;
                entries.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            return entries;
        }
        return null;
    }

    private BlockRewards getRewards(MenhirBlock block, ConfigurationSection section) {
        List<Reward> breakRewards = new LinkedList<>();
        List<Reward> lastRewards = new LinkedList<>();
        for (String name : section.getKeys(false)) {
            if (section.isConfigurationSection(name)) {
                //noinspection DataFlowIssue
                Reward reward = Reward.parse(section.getConfigurationSection(name));
                if (reward.isLast()) {
                    lastRewards.add(reward);
                } else breakRewards.add(reward);
            }
        }
        return new BlockRewards(
                block,
                breakRewards,
                lastRewards
        );
    }

    private void setHologram(ConfigurationSection section, BlockHologram hologram) {
        HologramSettings settings = hologram.getSettings();
        settings.getTemplate().ifPresent(t -> section.set("template", t));
        if (settings.getOffsetX() != 0 || settings.getOffsetY() != 0 || settings.getOffsetZ() != 0) {
            ConfigurationSection offsetSection = section.createSection("offset");
            if (settings.getOffsetX() != 0) offsetSection.set("x", settings.getOffsetX());
            if (settings.getOffsetY() != 0) offsetSection.set("y", settings.getOffsetY());
            if (settings.getOffsetZ() != 0) offsetSection.set("z", settings.getOffsetZ());
        }
        if (hologram.isOwnLines()) section.set("lines", hologram.getLines());
        for (Map.Entry<String, Object> entry : hologram.getExtraConfig().entrySet()) {
            section.set(entry.getKey(), entry.getValue());
        }
    }

    private void setAfk(ConfigurationSection section, AfkSettings afk) {
        if (afk != null) afk.write(section);
    }

    private void setLocation(ConfigurationSection section, Location location) {
        section.set("world", Optional.ofNullable(location.getWorld()).map(World::getName).orElse(null));
        section.set("x", location.getBlockX());
        section.set("y", location.getBlockY());
        section.set("z", location.getBlockZ());
    }

    private void setCoolDown(ConfigurationSection section, BlockCoolDown coolDown) {
        if (coolDown.getTime() > 0) section.set("time", coolDown.getTime());
        if (coolDown.getTypeOverride() != null) section.set("type", coolDown.getTypeOverride().name());
        if (coolDown.getRespawnMessage() != null && !coolDown.getRespawnMessage().isEmpty())
            ConfigUtil.setMultiLine(section, "respawn", coolDown.getRespawnMessage());
    }

    private void setResetOptions(ConfigurationSection section, ResetOptions resetOptions) {
        if (resetOptions.isOnRestart()) section.set("onrestart", true);
        if (resetOptions.getInactiveTime() > 0) section.set("inactive.time", resetOptions.getInactiveTime());
        if (resetOptions.getInactiveMessage() != null && !resetOptions.getInactiveMessage().isEmpty())
            ConfigUtil.setMultiLine(section, "inactive.message", resetOptions.getInactiveMessage());
    }

    private void setMessages(ConfigurationSection section, BlockMessages messages) {
        if (messages.getBreakMessage() != null && !messages.getBreakMessage().isEmpty())
            ConfigUtil.setMultiLine(section, "break", messages.getBreakMessage());
    }

    private void setRewards(ConfigurationSection section, BlockRewards rewards) {
        List<Reward> rewardList = new LinkedList<>();
        rewardList.addAll(rewards.getRewards());
        rewardList.addAll(rewards.getLastRewards());
        for (Reward reward : rewardList) {
            Reward.save(
                    section.createSection(reward.getName()),
                    reward
            );
        }
    }

    private void setRequiredTool(ConfigurationSection section, RequiredTool tool) {
        if (tool == null) return;
        Optional.ofNullable(tool.getEnchantmentDefault()).ifPresent(r -> section.set("enchantments.default", r.name()));
        Optional.ofNullable(tool.getNameDefault()).ifPresent(r -> section.set("names.default", r.name()));

        List<MaterialFilter> materialFilters = tool.getMaterialFilters();
        List<String> materialValues = new LinkedList<>();
        Result defaultMaterial = tool.getMaterialDefault();
        if (defaultMaterial != null) {
            materialValues.add("default:" + defaultMaterial.name());
        }
        for (MaterialFilter filter : materialFilters) {
            materialValues.add(filter.toString() + ":" + filter.getResult().name());
        }

        section.set("types", materialValues);

        Map<Enchantment, ToolEnchantment> toolEnchantments = tool.getEnchantmentFilters();
        if (!toolEnchantments.isEmpty()) {
            ConfigurationSection enchantments = section.createSection("enchantments");
            for (Map.Entry<Enchantment, ToolEnchantment> entry : toolEnchantments.entrySet()) {
                ConfigurationSection enchSection = enchantments.createSection(entry.getKey().getKey().getKey());
                enchSection.set("type", entry.getValue().getResult().name());
                enchSection.set("level", Optional.ofNullable(entry.getValue().getRange()).map(Object::toString).orElse(null));
            }
        }
        List<NameFilter> nameFilters = tool.getNameFilters();
        if (!nameFilters.isEmpty()) {
            ConfigurationSection names = section.createSection("names");
            for (NameFilter nameFilter : nameFilters) {
                names.set(nameFilter.getName(), nameFilter.getResult().name());
            }
        }
    }

    public void setBlocks(List<MenhirBlock> blocks) {
        for (String key : section.getKeys(false)) section.set(key, null);
        for (MenhirBlock block : blocks) {
            setBlock(block);
        }
    }

    public void setBlock(MenhirBlock block) {
        ConfigurationSection blockSection = section.createSection(block.getId());

        setLocation(blockSection.createSection("location"), block.getLocation());
        blockSection.set("type", block.getType().getType().name());
        setHologram(blockSection.createSection("hologram"), block.getHologram());
        blockSection.set("health", block.getHealth().getMaxHealth());
        if (block.getPermission() != null) blockSection.set("permission", block.getPermission());

        setCoolDown(blockSection.createSection("timeout"), block.getCoolDown());
        setMessages(blockSection.createSection("messages"), block.getMessages());
        setResetOptions(blockSection.createSection("reset"), block.getResetOptions());
        setRequiredTool(blockSection.createSection("tool"), block.getRequiredTool());
        setRewards(blockSection.createSection("rewards"), block.getRewards());
        setAfk(blockSection.createSection("afk"), block.getAfkOverride());

        for (String s : List.of("timeout", "reset", "messages", "tool", "rewards", "afk")) {
            ConfigurationSection sec = blockSection.getConfigurationSection(s);
            if (sec == null || sec.getKeys(false).isEmpty()) blockSection.set(s, null);
        }
    }

    public void removeBlock(String name) {
        section.set(name, null);
    }

}
