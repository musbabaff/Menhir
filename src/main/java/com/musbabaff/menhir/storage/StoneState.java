/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage;

/**
 * Persisted state of one stone.
 *
 * @param health    current health
 * @param broken    whether the stone is in its respawn cooldown
 * @param respawnAt respawn time in epoch milliseconds, {@code 0} when not broken
 */
public record StoneState(int health, boolean broken, long respawnAt) {

    public static final StoneState NONE = new StoneState(-1, false, 0L);

}
