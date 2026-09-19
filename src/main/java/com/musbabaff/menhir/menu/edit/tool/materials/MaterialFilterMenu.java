/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.menu.edit.tool.materials;

import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.tool.RequiredTool;
import com.musbabaff.menhir.block.tool.Result;
import com.musbabaff.menhir.block.tool.material.MaterialFilter;
import com.musbabaff.menhir.block.tool.material.SingleMaterialFilter;
import com.musbabaff.menhir.gui.filler.map.MapGuiFiller;
import com.musbabaff.menhir.gui.type.InventoryType;
import com.musbabaff.menhir.menu.PageableBlockMenu;
import com.musbabaff.menhir.menu.edit.tool.ToolEditMenu;
import com.musbabaff.menhir.menu.edit.tool.materials.items.AddMaterialFilter;
import com.musbabaff.menhir.menu.edit.tool.materials.items.MaterialFilterFile;
import com.musbabaff.menhir.menu.general.BackItem;
import com.musbabaff.menhir.menu.general.LearnMoreItem;
import com.musbabaff.menhir.menu.general.NextPageItem;
import com.musbabaff.menhir.menu.general.PreviousPageItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class MaterialFilterMenu extends PageableBlockMenu<MapGuiFiller> {

    public MaterialFilterMenu(MenhirBlock block) {
        super(new MapGuiFiller(
                "aaaaaaaaa",
                "aaaaaaaaa",
                "h  pnf  x"
        ), Component.text(block.getId() + " | Type filters"), InventoryType.CHEST_3, block);

        setPlayerInventoryHandler(itemClickEvent -> {
            int slot = itemClickEvent.getSlot();
            Inventory inventory = itemClickEvent.getPlayer().getInventory();
            if (slot < 0 || slot >= inventory.getSize()) return;
            ItemStack clicked = inventory.getItem(slot);
            if (clicked == null) return;
            Material type = clicked.getType();
            List<MaterialFilter> materialFilters = Optional.of(getBlock().getRequiredTool()).map(RequiredTool::getMaterialFilters).orElseGet(ArrayList::new);
            if (materialFilters.stream().noneMatch(materialFilter -> {
                if (!(materialFilter instanceof SingleMaterialFilter)) return false;
                return ((SingleMaterialFilter) materialFilter).getMaterial().equals(type);
            })) {
                materialFilters.add(new SingleMaterialFilter(
                        type, Result.ALLOWED
                ));
                setPage(getMaxPage());
                update();
                save();
            }
        });

        MapGuiFiller filler = getFiller();

        filler.setItem('a', new MaterialFilterFile(this));

        filler.setItem('p', new PreviousPageItem(this));
        filler.setItem('n', new AddMaterialFilter(this));
        filler.setItem('f', new NextPageItem(this));

        filler.setItem('h', new LearnMoreItem(filler));
        filler.setItem('x', new BackItem(filler, () -> new ToolEditMenu(getBlock())));
    }

    @Override
    public int getPageSize() {
        return 18;
    }
}
