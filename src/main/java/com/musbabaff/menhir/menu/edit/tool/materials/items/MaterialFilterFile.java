/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool.materials.items;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.block.tool.material.ContainsMaterialFilter;
import com.musbabaff.menhir.block.tool.material.SingleMaterialFilter;
import com.musbabaff.menhir.gui.item.click.ItemClickEvent;
import com.musbabaff.menhir.gui.itemstack.ItemStackBuilder;
import com.musbabaff.menhir.menu.BlockMenuItem;
import com.musbabaff.menhir.menu.edit.tool.materials.MaterialFilterMenu;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MaterialFilterFile extends BlockMenuItem {

    private final MaterialFilterMenu menu;

    public MaterialFilterFile(MaterialFilterMenu menu) {
        super(menu);
        this.menu = menu;
    }

    @Override
    public void click(ItemClickEvent<MenhirBlock> event) {
        int index = event.getSlot() + (menu.getPage() * menu.getPage());
        if (index < 0) return;
        List<com.musbabaff.menhir.block.tool.material.MaterialFilter> materialFilters = Optional.of(getState().getRequiredTool()).map(RequiredTool::getMaterialFilters).orElseGet(ArrayList::new);
        if (index < materialFilters.size()) {
            if (event.getType() == ClickType.LEFT) {
                materialFilters.remove(index);
            } else if (event.getType() == ClickType.RIGHT) {
                com.musbabaff.menhir.block.tool.material.MaterialFilter materialFilter = materialFilters.get(index);
                materialFilter.setResult(materialFilter.getResult().getOther());
            }
            menu.saveAndUpdate();
        }
    }

    @Override
    public ItemStack render(int slot, MenhirBlock state) {
        List<com.musbabaff.menhir.block.tool.material.MaterialFilter> materialFilters = Optional.of(state.getRequiredTool()).map(RequiredTool::getMaterialFilters).orElseGet(ArrayList::new);

        if (materialFilters.isEmpty() && slot == 13) {
            return ItemStackBuilder.create(Material.STRUCTURE_VOID)
                    .withName(MineDown.parse("&#DF2E38&There are no type filters"))
                    .withItemFlags(ItemFlag.values())
                    .build();
        }

        if (materialFilters.isEmpty()) return null;

        int pageStart = menu.getPage() * menu.getPage();
        if (pageStart >= materialFilters.size()) {
            menu.setPage(menu.getMaxPage());
            return render(slot, state);
        }

        List<com.musbabaff.menhir.block.tool.material.MaterialFilter> pageList = materialFilters.subList(pageStart, materialFilters.size());

        if (slot < pageList.size()) {
            com.musbabaff.menhir.block.tool.material.MaterialFilter materialFilter = pageList.get(slot);
            List<Component> lore = new LinkedList<>();
            Material type;

            if (materialFilter instanceof SingleMaterialFilter) {
                type = ((SingleMaterialFilter) materialFilter).getMaterial();
            } else if (materialFilter instanceof ContainsMaterialFilter) {
                type = Material.PAPER;
                lore.add(MineDown.parse("&7" + ((ContainsMaterialFilter) materialFilter).getStr()));
            } else type = Material.STRUCTURE_VOID;

            lore.addAll(List.of(
                    Component.empty(),
                    MineDown.parse("&7Value: " +
                            (materialFilter.getResult().getBooleanValue() ? "&#539165&Allowed" : "&#DF2E38&Denied")
                    ),
                    Component.empty(),
                    MineDown.parse("&7Left click to &#DF2E38&delete"),
                    MineDown.parse("&7Right click to change value")
            ));

            return ItemStackBuilder.create(type)
                    .withName(MineDown.parse("&#205295&&lType filter"))
                    .withLore(lore)
                    .build();
        }
        return null;
    }

    @Override
    public boolean requiresPerSlotRendering() {
        return true;
    }

}
