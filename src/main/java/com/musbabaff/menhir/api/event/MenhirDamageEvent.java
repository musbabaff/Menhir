/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.api.event;

import com.musbabaff.menhir.api.MenhirStone;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A player is about to hit a stone. Fired after the permission, AFK, cooldown and tool checks passed.
 *
 * <p>Cancelling the event discards the hit: the health does not change and the hit is not counted.
 * {@link #setDamage(int)} changes how much health the hit removes.</p>
 */
public class MenhirDamageEvent extends MenhirStoneEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int currentHealth;
    private int damage;
    private boolean cancelled;

    /**
     * Creates a new {@code MenhirDamageEvent}.
     *
     * @param stone         the stone
     * @param player        the hitting player
     * @param damage        health removed by the hit (normally {@code 1})
     * @param currentHealth health before the hit
     */
    public MenhirDamageEvent(@NotNull MenhirStone stone, @NotNull Player player, int damage, int currentHealth) {
        super(stone);
        this.player = player;
        this.damage = damage;
        this.currentHealth = currentHealth;
    }

    /**
     * Returns the hitting player.
     *
     * @return the hitting player
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Returns health removed by this hit.
     *
     * @return health removed by this hit
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Sets {@code damage}.
     *
     * @param damage health to remove; values below {@code 1} are raised to {@code 1}
     */
    public void setDamage(int damage) {
        this.damage = Math.max(1, damage);
    }

    /**
     * Returns health before the hit.
     *
     * @return health before the hit
     */
    public int getCurrentHealth() {
        return currentHealth;
    }

    /**
     * Returns health after the hit (never below zero); the stone breaks when this is zero.
     *
     * @return health after the hit (never below zero); the stone breaks when this is zero
     */
    public int getNewHealth() {
        return Math.max(0, currentHealth - damage);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the handler list of this event type.
     *
     * @return the handler list of this event type
     */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

}
