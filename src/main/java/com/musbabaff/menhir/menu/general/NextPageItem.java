/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.general;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.PageableBlockMenu;
import de.themoep.minedown.adventure.MineDown;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class NextPageItem extends BlockMenuItem {

    private PageableBlockMenu<?> menu;

    public NextPageItem(PageableBlockMenu<?> editMenu) {
        super(editMenu);
        this.menu = editMenu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> itemClickEvent) {
        if (menu != null && menu.hasNextPage()) menu.setPage(menu.getPage() + 1);
    }

    @Override
    public ItemStack render(MenhirBlock state) {
        if (menu == null || !menu.hasNextPage()) return new ItemStack(Material.AIR);
        return ItemStackBuilder.create("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDllY2NjNWMxYzc5YWE3ODI2YTE1YTdmNWYxMmZiNDAzMjgxNTdjNTI0MjE2NGJhMmFlZjQ3ZTVkZTlhNWNmYyJ9fX0=")
                .withName(MineDown.parse("&#205295&Next page"))
                .build();
    }

}
