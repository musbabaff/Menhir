/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.listener;

import com.musbabaff.menhir.gui.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.Consumer;

public class GuiListener implements Listener {

    private void ifGui(Inventory inventory, Consumer<Gui<?>> guiConsumer) {
        if (inventory == null) return;
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof Gui) {
            guiConsumer.accept((Gui<?>) holder);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        ifGui(e.getView().getTopInventory(), gui -> {
            e.setCancelled(true);
            if (!(e.getWhoClicked() instanceof Player)) return;
            if (gui.getInventory().equals(e.getClickedInventory())) {
                gui.onClick(e.getSlot(), e.getClick(), (Player) e.getWhoClicked(), e.getCursor());
            } else {
                gui.onPlayerInventoryClick(e.getSlot(), e.getClick(), (Player) e.getWhoClicked(), e.getCursor());
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        ifGui(e.getView().getTopInventory(), gui -> e.setCancelled(true));
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        ifGui(e.getInventory(), gui -> {
            if (e.getPlayer() instanceof Player) {
                gui.addViewer((Player) e.getPlayer());
            }
        });
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        ifGui(e.getInventory(), gui -> {
            if (e.getPlayer() instanceof Player) {
                gui.removeViewer((Player) e.getPlayer());
            }
        });
    }


}
