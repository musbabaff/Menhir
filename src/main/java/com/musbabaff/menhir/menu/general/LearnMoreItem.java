/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.general;

import com.musbabaff.menhir.gui.filler.GuiFiller;
import com.musbabaff.menhir.gui.item.AbstractItem;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LearnMoreItem extends AbstractItem<Void> {
    public LearnMoreItem(GuiFiller<?> parent) {
        super(parent, null);
    }

    @Override
    public void click(ItemClickEvent<Void> itemClickEvent) {
        Player player = itemClickEvent.getPlayer();
        player.closeInventory();
        Colors.send(player, "&7Menhir documentation and issue tracker: #2C74B3" + com.musbabaff.menhir.MenhirPlugin.REPOSITORY_URL);
    }

    @Override
    public ItemStack render(Void state) {
        return ItemStackBuilder.create(Material.KNOWLEDGE_BOOK)
                .withName(MineDown.parse("&#205295&&lAre you lost?"))
                .withLore(List.of(
                        Component.empty(),
                        MineDown.parse("&7Don't worry! You can learn"),
                        MineDown.parse("&7more in the README, or report"),
                        MineDown.parse("&7a problem on GitHub!"),
                        Component.empty(),
                        MineDown.parse("&7Click to get the link")
                ))
                .build();
    }
}
