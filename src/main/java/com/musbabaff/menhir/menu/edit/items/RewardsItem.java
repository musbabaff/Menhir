/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.rewards.BlockRewards;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenu;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.rewards.RewardsEditMenu;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RewardsItem  extends BlockMenuItem {
    public RewardsItem(BlockMenu<?> editMenu) {
        super(editMenu);
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        new RewardsEditMenu(getState()).open(itemClickEvent.getPlayer());
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        BlockRewards rewards = getState().getRewards();
        int totalRewards = rewards.getRewards().size() + rewards.getLastRewards().size();
        return ItemStackBuilder.create(Material.GOLD_NUGGET)
                .withName(MineDown.parse("&#205295&&lRewards"))
                .withLore(List.of(
                        Component.empty(),
                        MineDown.parse("&7There are &#2C74B3&" + totalRewards + " &7rewards"),
                        MineDown.parse("&7in total"),
                        Component.empty(),
                        MineDown.parse("&7Click to edit")
                ))
                .addItemFlags(ItemFlag.values())
                .build();
    }
}
