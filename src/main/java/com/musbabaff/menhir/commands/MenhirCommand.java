/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.cooldown.BlockCoolDown;
import com.musbabaff.menhir.block.health.BlockHealth;
import com.musbabaff.menhir.block.hologram.BlockHologram;
import com.musbabaff.menhir.block.messages.BlockMessages;
import com.musbabaff.menhir.block.reset.ResetOptions;
import com.musbabaff.menhir.block.rewards.BlockRewards;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.block.tool.Result;
import com.musbabaff.menhir.block.type.BlockType;
import com.musbabaff.menhir.config.blocks.BlocksConfig;
import com.musbabaff.menhir.menu.edit.EditMenu;
import com.musbabaff.menhir.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

@CommandAlias("menhir|metin|menhirstone|mb")
@CommandPermission("menhir.admin")
public class MenhirCommand extends BaseCommand {

    public static void showHologram(CommandSender audience, MenhirBlock block) {
        String blockId = block.getId();
        Component message = MineDown.parse("&#2C74B3&Hologram of block " + blockId + ":");

        List<String> lines = block.getHologram().getLines();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            message = message.append(Component.newline())
                    .append(
                            Component.text(" - ", NamedTextColor.DARK_GRAY)
                                    .clickEvent(ClickEvent.suggestCommand("/menhir hologram setline " + blockId + " " + (i + 1) + " " + line))
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to edit!", TextColor.color(32, 82, 149))))
                                    .append(Component.text(line, NamedTextColor.GRAY))
                                    .append(
                                            Component.text(" [Remove]", TextColor.color(223, 46, 56))
                                            .hoverEvent(HoverEvent.showText(Component.text("Click to remove!", TextColor.color(223, 46, 56))))
                                            .clickEvent(ClickEvent.suggestCommand("/menhir hologram removeline " + blockId + " " + (i + 1)))
                                    )
                    );
        }

        message = message.append(Component.newline())
                        .append(
                                Component.text(" Add new line", TextColor.color(44, 116, 179))
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to add line!", TextColor.color(32, 82, 149))))
                                        .clickEvent(ClickEvent.suggestCommand("/menhir hologram addline " + blockId + " "))
                        );

        audience.sendMessage(message);
    }

    private final MenhirPlugin plugin;

    public MenhirCommand(MenhirPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommandManager().getCommandCompletions()
                .registerCompletion("blocks", c -> plugin.getBlockRegistry().getBlocks().stream().map(MenhirBlock::getId).collect(Collectors.toList()));
    }

    @Default
    @Subcommand("help")
    @CommandPermission("menhir.help")
    public void help(CommandSender sender) {
        String baseCommand = "#2C74B3/menhir ";
        Colors.send(sender,
                "#205295&lMenhir help menu:",
                baseCommand + "reload &7Reloads plugin configuration and all blocks",
                baseCommand + "version &7Shows version of installed plugin",
                baseCommand + "wiki &7Shows link to the documentation",
                baseCommand + "create <block> &7Creates new Menhir block on your position",
                baseCommand + "edit <block> &7Opens menu where you can edit specified block",
                baseCommand + "hologram show <block> &7Shows hologram of specified block",
                baseCommand + "hologram addline <block> <line content> &7Adds line to hologram",
                baseCommand + "hologram removeline <block> <line number> &7Removes line to hologram",
                baseCommand + "hologram setline <block> <line number> <line content> &7Sets line to hologram",
                baseCommand + "list &7Shows all Menhir blocks on server",
                baseCommand + "reset <block> &7Resets specified block's health",
                baseCommand + "teleport <block> &7Teleports you to specified block"
                );
    }

    @Subcommand("reload")
    @CommandPermission("menhir.reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        Colors.send(sender, "#2C74B3Plugin was reloaded!");
    }

    @Subcommand("list")
    @CommandPermission("menhir.list")
    public void list(CommandSender sender) {
        Collection<MenhirBlock> blocks = plugin.getBlockRegistry().getBlocks();
        if (blocks.isEmpty()) {
            Colors.send(sender,
                "#DF2E38There are no blocks!"
            );
        } else {
            sender.sendMessage(Colors.colorize("#2C74B3List of blocks:"));
            for (MenhirBlock block : blocks) {
                Colors.send(sender,
                        "#0A2647 - #2C74B3"+ block.getId() +" #205295" + Optional.ofNullable(block.getLocation())
                                .map(loc ->
                                        Optional.ofNullable(loc.getWorld())
                                                .map(World::getName).orElse("unknown world") + ", " +
                                        loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())
                                .orElse("")
                );
            }
        }
    }

    @Subcommand("teleport|tp")
    @Syntax("/menhir teleport <block>")
    @CommandCompletion("@blocks")
    @CommandPermission("menhir.teleport")
    public void teleport(Player player, @Single String name) {
        Optional.ofNullable(plugin.getBlockRegistry().get(name)).ifPresentOrElse(block -> {
            player.teleport(block.getLocation().clone().add(0.5, 1.5, .5), PlayerTeleportEvent.TeleportCause.COMMAND);
            Colors.send(player,
                    "#2C74B3Teleported to block "+ name +"!"
            );
        }, () -> Colors.send(player,
                "#DF2E38Block with name "+ name +" was not found!"
        ));
    }

    @Subcommand("version")
    @CommandPermission("menhir.version")
    public void version(CommandSender sender) {
        Colors.send(sender, "#2C74B3Menhir version " + plugin.getPluginMeta().getVersion()
                + " &7(" + MenhirPlugin.REPOSITORY_URL + "/releases)");
    }

    @Subcommand("wiki|docs")
    public void wiki(CommandSender sender) {
        Colors.send(sender, "#2C74B3Menhir documentation is available at " + MenhirPlugin.REPOSITORY_URL);
    }

    @Subcommand("reset")
    @Syntax("/menhir reset <block>")
    @CommandCompletion("@blocks")
    @CommandPermission("menhir.reset")
    public void reset(CommandSender sender, @Single String name) {
        Optional.ofNullable(plugin.getBlockRegistry().get(name)).ifPresentOrElse(block -> {
            block.reset();
            Colors.send(sender,
                    "#2C74B3Block "+ name +" was reset!"
            );
        }, () -> Colors.send(sender,
                "#DF2E38Block with name "+ name +" was not found!"
        ));
    }

    @Subcommand("sethealth")
    @Syntax("/menhir sethealth <block> <health>")
    @CommandPermission("menhir.sethealth")
    @CommandCompletion("@blocks @nothing")
    public void setHealth(CommandSender sender, String name, int health) {
        Optional.ofNullable(plugin.getBlockRegistry().get(name)).ifPresentOrElse(block -> {
            BlockHealth blockHealth = block.getHealth();
            blockHealth.setHealth(Math.max(1, Math.min(health, blockHealth.getMaxHealth())));
            Colors.send(sender,
                    "#2C74B3Health of block "+ name +" was set to "+ blockHealth.getHealth() +"!"
            );
            block.getHologram().update();
        }, () -> Colors.send(sender,
                "#DF2E38Block with name "+ name +" was not found!"
        ));
    }

    @Subcommand("edit")
    @Syntax("/menhir edit <block>")
    @CommandPermission("menhir.edit")
    @CommandCompletion("@blocks")
    public void edit(Player player, @Single String name) {
        Optional.ofNullable(plugin.getBlockRegistry().get(name))
                .ifPresentOrElse(
                        block -> new EditMenu(block).open(player),
                        () -> Colors.send(player, "#DF2E38Block with name "+ name +" was not found!")
                );
    }

    @Subcommand("remove")
    @CommandPermission("menhir.remove")
    @Syntax("/menhir remove <block>")
    public void remove(CommandSender sender, @Single String name) {
        if (!(sender instanceof ConsoleCommandSender)) {
            Colors.send(sender, "#DF2E38You can only delete the block from GUI editor. This command can only be used from console!");
        } else {
            Optional.ofNullable(plugin.getBlockRegistry().get(name))
                .ifPresentOrElse(
                        block -> {
                            block.getPlugin().getBlockRegistry().delete(block);
                            File dataFile = MenhirBlock.getStoragePath(block.getPlugin(), block);
                            try {
                                Files.deleteIfExists(dataFile.toPath());
                            } catch (IOException e) {
                                block.getPlugin().getLogger().log(java.util.logging.Level.WARNING, "Could not delete data file of block " + block.getId(), e);
                            }
                            Colors.send(sender, "&7[Menhir] #2C74B3Block " + block.getId() + " was successfully deleted!");
                        },
                        () -> Colors.send(sender, "&7[Menhir] #DF2E38Block with name "+ name +" was not found!")
            );
        }
    }

    @Subcommand("create")
    @Syntax("/menhir create <block>")
    @CommandPermission("menhir.create")
    public void create(Player sender, @Single String name) {
        Optional.ofNullable(plugin.getBlockRegistry().get(name))
                .ifPresentOrElse(
                        block -> Colors.send(sender, "#DF2E38Block with name "+ name +" already exists!"),
                        () -> {
                            MenhirBlock block = new MenhirBlock(plugin);
                            block.setId(name);
                            block.setLocation(sender.getLocation().getBlock().getLocation());
                            block.setType(new BlockType(block, Material.DIAMOND_BLOCK));
                            block.setHealth(new BlockHealth(block, 100));
                            block.setHologram(new BlockHologram(block, List.of(
                                    "#ICON: %type%",
                                    "#2C74B3&lMENHIR",
                                    "#2C74B3&lᴛᴏᴘ",
                                    "&7%player_1% &8- #2C74B3%player_1_breaks%",
                                    "&7%player_2% &8- #2C74B3%player_2_breaks%",
                                    "&7%player_3% &8- #2C74B3%player_3_breaks%",
                                    "#2C74B3%health%/%max_health%",
                                    "&7Break to get reward",
                                    "&c%timeout%"
                            )));
                            block.setCoolDown(new BlockCoolDown(block, -1, null, ""));
                            block.setResetOptions(new ResetOptions(block, false, -1, ""));
                            block.setMessages(new BlockMessages("&7Block was destroyed\n&7Your breaks: %breaks%"));
                            block.setRewards(new BlockRewards(block, new LinkedList<>(), new LinkedList<>()));
                            block.setRequiredTool(new RequiredTool(
                                    new LinkedList<>(),
                                    Result.ALLOWED,
                                    new HashMap<>(),
                                    Result.ALLOWED,
                                    new LinkedList<>(),
                                    Result.ALLOWED
                            ));
                            plugin.getBlockRegistry().register(block);
                            BlocksConfig blocksConfig = plugin.getConfiguration().getBlocksConfig();
                            blocksConfig.setBlock(block);
                            plugin.saveConfiguration();
                            Colors.send(sender,
                                    "#2C74B3Block "+ name +" was created! You can edit by using /menhir edit " + name
                            );
                        }
                );
    }

    @Subcommand("hologram setline")
    @Syntax("/menhir hologram setline <block> <line number> <line content>")
    @CommandPermission("menhir.hologram")
    @CommandCompletion("@blocks @nothing")
    public void setLine(CommandSender sender, String name, int l, String value) {
        int line = l - 1;
        Optional.ofNullable(plugin.getBlockRegistry().get(name))
                .ifPresentOrElse(
                        block -> {
                            BlockHologram blockHologram = block.getHologram();
                            if (line >= 0 && line < blockHologram.getLines().size()) {
                                blockHologram.setLine(line, value);
                                Colors.send(sender,
                                        "#2C74B3Line was updated!"
                                );
                                showHologram(sender, block);
                                saveBlock(block);
                            } else Colors.send(sender, "#DF2E38Line with number "+ line +" is not present!");
                        },
                        () -> Colors.send(sender, "#DF2E38Block with name "+ name +" was not found!")
                );
    }

    @Subcommand("hologram removeline")
    @Syntax("/menhir hologram removeline <block> <line number>")
    @CommandPermission("menhir.hologram")
    @CommandCompletion("@blocks @nothing")
    public void removeLine(CommandSender sender, String name, int l) {
        int line = l - 1;
        Optional.ofNullable(plugin.getBlockRegistry().get(name))
                .ifPresentOrElse(
                        block -> {
                            BlockHologram blockHologram = block.getHologram();
                            if (line >= 0 && line < blockHologram.getLines().size()) {
                                blockHologram.removeLine(line);
                                Colors.send(sender,
                                        "#2C74B3Line was removed!"
                                );
                                showHologram(sender, block);
                                saveBlock(block);
                            } else Colors.send(sender, "#DF2E38Line with number "+ line +" is not present!");
                        },
                        () -> Colors.send(sender, "#DF2E38Block with name "+ name +" was not found!")
                );
    }

    @Subcommand("hologram addline")
    @Syntax("/menhir hologram addline <block> <line content>")
    @CommandPermission("menhir.hologram")
    @CommandCompletion("@blocks @nothing")
    public void addLine(CommandSender sender, String name, String value) {
        Optional.ofNullable(plugin.getBlockRegistry().get(name))
                .ifPresentOrElse(
                        block -> {
                            BlockHologram blockHologram = block.getHologram();
                            blockHologram.addLine(value);
                            Colors.send(sender,
                                    "#2C74B3Line was inserted!"
                            );
                            showHologram(sender, block);
                            saveBlock(block);
                        },
                        () -> Colors.send(sender, "#DF2E38Block with name "+ name +" was not found!")
                );
    }

    @Subcommand("hologram show")
    @Syntax("/menhir hologram show <block>")
    @CommandPermission("menhir.hologram")
    @CommandCompletion("@blocks")
    public void show(CommandSender sender, @Single String name) {
        Optional.ofNullable(plugin.getBlockRegistry().get(name))
                .ifPresentOrElse(
                        block -> showHologram(sender, block),
                        () -> Colors.send(sender, "#DF2E38Block with name "+ name +" was not found!")
                );
    }

    private void saveBlock(MenhirBlock block) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            block.getPlugin().getConfiguration().getBlocksConfig().setBlock(block);
            block.getPlugin().saveConfiguration();
        });
    }

}
