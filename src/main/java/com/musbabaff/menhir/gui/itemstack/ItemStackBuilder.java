/*
 * This file is part of Menhir, a fork of MineBlocks (https://github.com/RAIXOCZ/MineBlocks, MPL-2.0).
 * Modifications (c) 2026 musbabaff — see NOTICE.md
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.gui.itemstack;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class ItemStackBuilder {

    public static ItemStackBuilder create(Material material) {
        return new ItemStackBuilder(new ItemStack(material));
    }

    /**
     * Creates a player head with a custom texture (base64 profile value).
     */
    public static ItemStackBuilder create(String headValue) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        if (item.getItemMeta() instanceof SkullMeta skullMeta) {
            UUID hashAsId = new UUID(headValue.hashCode(), headValue.hashCode());
            PlayerProfile profile = Bukkit.createProfile(hashAsId, "menhir");
            profile.setProperty(new ProfileProperty("textures", headValue));
            skullMeta.setPlayerProfile(profile);
            item.setItemMeta(skullMeta);
        }
        return new ItemStackBuilder(item);
    }

    private final ItemStack itemStack;

    public ItemStackBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStackBuilder withName(Component name) {
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.displayName(name);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withLore(Component... lore) {
        return withLore(Arrays.asList(lore));
    }

    public ItemStackBuilder withLore(List<Component> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withCount(int count) {
        itemStack.setAmount(count);
        return this;
    }

    public ItemStackBuilder withItemFlags(ItemFlag... itemFlags) {
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.removeItemFlags(meta.getItemFlags().toArray(ItemFlag[]::new));
        meta.addItemFlags(itemFlags);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder addItemFlags(ItemFlag... itemFlags) {
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.addItemFlags(itemFlags);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withEnchantment(Enchantment enchantment, int level) {
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.addEnchant(enchantment, level, true);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withMeta(UnaryOperator<ItemMeta> updater) {
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        itemStack.setItemMeta(updater.apply(meta));
        return this;
    }

    public ItemStackBuilder withItemStack(UnaryOperator<ItemStack> updater) {
        return new ItemStackBuilder(updater.apply(itemStack));
    }

    public ItemStackBuilder shiny() {
        return
                addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        .withEnchantment(Enchantment.UNBREAKING, 1);

    }

    public ItemStack build() {
        return itemStack;
    }

}
