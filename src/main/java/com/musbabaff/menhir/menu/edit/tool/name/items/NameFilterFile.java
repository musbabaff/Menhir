/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool.name.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.block.tool.name.NameFilter;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.tool.name.NameFilterMenu;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NameFilterFile extends BlockMenuItem {
    private final NameFilterMenu menu;

    public NameFilterFile(NameFilterMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> event) {
        int index = event.getSlot() + (menu.getPage() * menu.getPage());
        if (index < 0) return;
        List<NameFilter> nameFilters = Optional.of(getState().getRequiredTool()).map(RequiredTool::getNameFilters).orElseGet(ArrayList::new);
        if (index < nameFilters.size()) {
            if (event.getType() == ClickType.LEFT) {
                nameFilters.remove(index);
            } else if (event.getType() == ClickType.RIGHT) {
                NameFilter nameFilter = nameFilters.get(index);
                nameFilter.setResult(nameFilter.getResult().getOther());
            }
            menu.saveAndUpdate();
        }
    }

    @Override
    public ItemStack render(int slot, MenhirBlock state) {
        List<NameFilter> nameFilters = Optional.of(state.getRequiredTool()).map(RequiredTool::getNameFilters).orElseGet(ArrayList::new);

        if (nameFilters.isEmpty() && slot == 13) {
            return ItemStackBuilder.create(Material.STRUCTURE_VOID)
                    .withName(MineDown.parse("&#DF2E38&There are no name filters"))
                    .withItemFlags(ItemFlag.values())
                    .build();
        }

        if (nameFilters.isEmpty()) return null;

        int pageStart = menu.getPage() * menu.getPage();
        if (pageStart >= nameFilters.size()) {
            menu.setPage(menu.getMaxPage());
            return render(slot, state);
        }

        List<NameFilter> pageList = nameFilters.subList(pageStart, nameFilters.size());

        if (slot < pageList.size()) {
            NameFilter nameFilter = pageList.get(slot);
            return ItemStackBuilder.create(nameFilter.getResult().getBooleanValue() ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA)
                    .withName(MineDown.parse("&#205295&&lName filter"))
                    .withLore(
                            MineDown.parse("&7" + nameFilter.getName()),
                            Component.empty(),
                            MineDown.parse("&7Value: " +
                                    (nameFilter.getResult().getBooleanValue() ? "&#539165&Allowed" : "&#DF2E38&Denied")
                            ),
                            Component.empty(),
                            MineDown.parse("&7Left click to &#DF2E38&delete"),
                            MineDown.parse("&7Right click to change value")
                    )
                    .build();
        }
        return null;
    }

    @Override
    public boolean requiresPerSlotRendering() {
        return true;
    }

}
