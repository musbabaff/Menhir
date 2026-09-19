/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage;

import com.musbabaff.menhir.api.TopEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The in-memory copy of the stored data that every {@link StorageProvider} works on, plus the
 * bookkeeping of what changed since the last flush. All public methods are synchronized so a
 * background load or flush can touch it while the main thread keeps counting hits; the
 * {@link ChangeSet} handed to background work is an immutable snapshot.
 */
public final class MemoryStore {

    /** Immutable copy of one stone's data, used when writing. */
    public record BlockSnapshot(String blockId, StoneState state, Map<UUID, TopEntry> players) {
    }

    /** A pending hit delta of one player on one stone. */
    public record BreakDelta(String blockId, UUID player, String name, int amount) {
    }

    /** Everything that changed since the previous flush. */
    public record ChangeSet(boolean resetAll,
                            Set<String> resetBlocks,
                            Set<UUID> resetPlayers,
                            Set<String> deletedBlocks,
                            List<BreakDelta> breaks,
                            Map<String, StoneState> states,
                            Map<String, BlockSnapshot> dirtySnapshots) {

        public boolean isEmpty() {
            return !resetAll && resetBlocks.isEmpty() && resetPlayers.isEmpty() && deletedBlocks.isEmpty()
                    && breaks.isEmpty() && states.isEmpty() && dirtySnapshots.isEmpty();
        }
    }

    private static final class BlockRecord {
        StoneState state;
        final Map<UUID, TopEntry> players = new LinkedHashMap<>();
    }

    private final Map<String, BlockRecord> blocks = new HashMap<>();

    // change tracking
    private boolean resetAll;
    private final Set<String> resetBlocks = new java.util.LinkedHashSet<>();
    private final Set<UUID> resetPlayers = new java.util.LinkedHashSet<>();
    private final Set<String> deletedBlocks = new java.util.LinkedHashSet<>();
    private final Map<String, Map<UUID, BreakDelta>> breakDeltas = new LinkedHashMap<>();
    private final Map<String, StoneState> stateChanges = new LinkedHashMap<>();
    private final Set<String> dirtyBlocks = new java.util.LinkedHashSet<>();

    // --- loading (does not mark anything dirty) ---

    public synchronized void putLoaded(String blockId, StoneState state, Collection<TopEntry> players) {
        BlockRecord record = blocks.computeIfAbsent(blockId, k -> new BlockRecord());
        if (state != null) record.state = state;
        for (TopEntry entry : players) record.players.put(entry.uuid(), entry);
    }

    public synchronized void clear() {
        blocks.clear();
    }

    // --- reads ---

    public synchronized int getBreaks(String blockId, UUID player) {
        BlockRecord record = blocks.get(blockId);
        if (record == null) return 0;
        TopEntry entry = record.players.get(player);
        return entry == null ? 0 : entry.breaks();
    }

    public synchronized List<TopEntry> getTop(String blockId, int limit) {
        BlockRecord record = blocks.get(blockId);
        if (record == null) return List.of();
        return sorted(record.players.values(), limit);
    }

    public synchronized List<TopEntry> getGlobalTop(int limit) {
        Map<UUID, TopEntry> totals = new HashMap<>();
        for (BlockRecord record : blocks.values()) {
            for (TopEntry entry : record.players.values()) {
                totals.merge(entry.uuid(), entry, (a, b) -> new TopEntry(a.uuid(), b.name(), a.breaks() + b.breaks()));
            }
        }
        return sorted(totals.values(), limit);
    }

    public synchronized Map<UUID, TopEntry> getPlayers(String blockId) {
        BlockRecord record = blocks.get(blockId);
        return record == null ? Map.of() : Map.copyOf(record.players);
    }

    public synchronized Optional<StoneState> getState(String blockId) {
        BlockRecord record = blocks.get(blockId);
        return record == null ? Optional.empty() : Optional.ofNullable(record.state);
    }

    public synchronized Collection<String> getBlockIds() {
        return List.copyOf(blocks.keySet());
    }

    private static List<TopEntry> sorted(Collection<TopEntry> entries, int limit) {
        List<TopEntry> list = new ArrayList<>(entries);
        list.removeIf(e -> e.breaks() <= 0);
        list.sort(Comparator.comparingInt(TopEntry::breaks).reversed());
        return list.size() > limit ? List.copyOf(list.subList(0, Math.max(0, limit))) : List.copyOf(list);
    }

    // --- writes (tracked) ---

    public synchronized void addBreaks(String blockId, UUID player, String name, int amount) {
        if (amount == 0) return;
        BlockRecord record = blocks.computeIfAbsent(blockId, k -> new BlockRecord());
        TopEntry current = record.players.get(player);
        int total = Math.max(0, (current == null ? 0 : current.breaks()) + amount);
        record.players.put(player, new TopEntry(player, name, total));
        breakDeltas.computeIfAbsent(blockId, k -> new LinkedHashMap<>())
                .merge(player, new BreakDelta(blockId, player, name, amount),
                        (a, b) -> new BreakDelta(blockId, player, name, a.amount() + b.amount()));
        dirtyBlocks.add(blockId);
    }

    public synchronized void resetBlock(String blockId) {
        BlockRecord record = blocks.get(blockId);
        if (record != null) record.players.clear();
        breakDeltas.remove(blockId);
        resetBlocks.add(blockId);
        dirtyBlocks.add(blockId);
    }

    public synchronized void resetPlayer(UUID player) {
        for (Map.Entry<String, BlockRecord> entry : blocks.entrySet()) {
            if (entry.getValue().players.remove(player) != null) dirtyBlocks.add(entry.getKey());
            Map<UUID, BreakDelta> deltas = breakDeltas.get(entry.getKey());
            if (deltas != null) deltas.remove(player);
        }
        resetPlayers.add(player);
    }

    public synchronized void resetAll() {
        for (Map.Entry<String, BlockRecord> entry : blocks.entrySet()) {
            entry.getValue().players.clear();
            dirtyBlocks.add(entry.getKey());
        }
        breakDeltas.clear();
        resetBlocks.clear();
        resetPlayers.clear();
        resetAll = true;
    }

    public synchronized void setState(String blockId, StoneState state) {
        BlockRecord record = blocks.computeIfAbsent(blockId, k -> new BlockRecord());
        if (state.equals(record.state)) return;
        record.state = state;
        stateChanges.put(blockId, state);
        dirtyBlocks.add(blockId);
    }

    public synchronized void deleteBlock(String blockId) {
        blocks.remove(blockId);
        breakDeltas.remove(blockId);
        stateChanges.remove(blockId);
        resetBlocks.remove(blockId);
        dirtyBlocks.remove(blockId);
        deletedBlocks.add(blockId);
    }

    public synchronized void renameBlock(String oldId, String newId) {
        BlockRecord record = blocks.remove(oldId);
        if (record == null) return;
        deleteBlock(oldId);
        blocks.put(newId, record);
        if (record.state != null) stateChanges.put(newId, record.state);
        for (TopEntry entry : record.players.values()) {
            breakDeltas.computeIfAbsent(newId, k -> new LinkedHashMap<>())
                    .put(entry.uuid(), new BreakDelta(newId, entry.uuid(), entry.name(), entry.breaks()));
        }
        resetBlocks.add(newId);
        dirtyBlocks.add(newId);
    }

    // --- flushing ---

    public synchronized boolean hasChanges() {
        return resetAll || !resetBlocks.isEmpty() || !resetPlayers.isEmpty() || !deletedBlocks.isEmpty()
                || !breakDeltas.isEmpty() || !stateChanges.isEmpty() || !dirtyBlocks.isEmpty();
    }

    /** Takes and clears the pending changes; the snapshots are immutable copies safe for other threads. */
    public synchronized ChangeSet drain() {
        List<BreakDelta> deltas = new ArrayList<>();
        for (Map<UUID, BreakDelta> perBlock : breakDeltas.values()) deltas.addAll(perBlock.values());
        Map<String, BlockSnapshot> snapshots = new LinkedHashMap<>();
        for (String blockId : dirtyBlocks) {
            BlockRecord record = blocks.get(blockId);
            if (record != null) snapshots.put(blockId, new BlockSnapshot(blockId, record.state, Map.copyOf(record.players)));
        }
        ChangeSet set = new ChangeSet(resetAll, Set.copyOf(resetBlocks), Set.copyOf(resetPlayers), Set.copyOf(deletedBlocks),
                List.copyOf(deltas), Map.copyOf(stateChanges), Map.copyOf(snapshots));
        resetAll = false;
        resetBlocks.clear();
        resetPlayers.clear();
        deletedBlocks.clear();
        breakDeltas.clear();
        stateChanges.clear();
        dirtyBlocks.clear();
        return set;
    }

    /** Puts a change set back (after a failed flush) so it is retried next time. */
    public synchronized void requeue(ChangeSet set) {
        if (set.resetAll()) resetAll = true;
        resetBlocks.addAll(set.resetBlocks());
        resetPlayers.addAll(set.resetPlayers());
        deletedBlocks.addAll(set.deletedBlocks());
        for (BreakDelta delta : set.breaks()) {
            breakDeltas.computeIfAbsent(delta.blockId(), k -> new LinkedHashMap<>())
                    .merge(delta.player(), delta, (a, b) -> new BreakDelta(a.blockId(), a.player(), b.name(), a.amount() + b.amount()));
        }
        for (Map.Entry<String, StoneState> entry : set.states().entrySet()) stateChanges.putIfAbsent(entry.getKey(), entry.getValue());
        dirtyBlocks.addAll(set.dirtySnapshots().keySet());
    }

}
