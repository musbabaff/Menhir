/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.item.click;

import com.musbabaff.menhir.gui.item.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ItemClickEvent<T> {

    private final Player player;
    private final GuiItem<T> guiItem;
    private final ClickType type;
    private final int slot;
    private final ItemStack cursorItem;

    public ItemClickEvent(Player player, GuiItem<T> guiItem, ClickType type, int slot, ItemStack cursorItem) {
        this.player = player;
        this.guiItem = guiItem;
        this.type = type;
        this.slot = slot;
        this.cursorItem = cursorItem;
    }

    public Player getPlayer() {
        return player;
    }

    public GuiItem<T> getGuiItem() {
        return guiItem;
    }

    public ClickType getType() {
        return type;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getCursorItem() {
        return cursorItem;
    }

}
