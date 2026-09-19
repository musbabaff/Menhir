/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.countdown;

import com.musbabaff.menhir.MenhirPlugin;
import com.musbabaff.menhir.block.MenhirBlock;
import com.musbabaff.menhir.block.placeholder.BlockPlaceholderSet;
import com.musbabaff.menhir.config.countdown.CountdownSettings;
import com.musbabaff.menhir.config.lang.LangConfig;
import com.musbabaff.menhir.text.TextRenderer;
import com.musbabaff.menhir.util.placeholders.PlaceholderSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Announces "the stone respawns in X" at the configured moments before a broken stone comes back.
 *
 * <p>Tasks exist only while a stone is broken. When a cooldown starts (including one restored from
 * storage after a restart) only thresholds that are still in the future are scheduled, and every
 * threshold is announced at most once per cooldown.</p>
 */
public final class RespawnCountdownService {

    private static final class Countdown {
        final List<BukkitTask> tasks = new ArrayList<>();
        final Set<Integer> announced = new HashSet<>();
    }

    private final MenhirPlugin plugin;
    private final Map<String, Countdown> countdowns = new HashMap<>();

    public RespawnCountdownService(MenhirPlugin plugin) {
        this.plugin = plugin;
    }

    /** Called when a stone's cooldown starts (or is restored); {@code respawnAt} in epoch millis. */
    public void onCooldownStart(MenhirBlock block, long respawnAt) {
        onCooldownEnd(block);
        CountdownSettings settings = block.getCountdownSettings();
        if (!settings.isEnabled()) return;
        long now = System.currentTimeMillis();
        List<CountdownSchedule.Warning> warnings = CountdownSchedule.remaining(settings.getWarnAt(), respawnAt, now);
        if (warnings.isEmpty()) return;
        Countdown countdown = new Countdown();
        for (CountdownSchedule.Warning warning : warnings) {
            countdown.tasks.add(plugin.getServer().getScheduler().runTaskLater(plugin,
                    () -> announce(block, warning.secondsLeft()),
                    CountdownSchedule.ticksUntil(warning.fireAt(), now)));
        }
        countdowns.put(block.getId(), countdown);
    }

    /** Called when the stone respawned or was unloaded: cancels pending announcements. */
    public void onCooldownEnd(MenhirBlock block) {
        Countdown countdown = countdowns.remove(block.getId());
        if (countdown == null) return;
        countdown.tasks.forEach(BukkitTask::cancel);
    }

    public void stop() {
        for (Countdown countdown : countdowns.values()) countdown.tasks.forEach(BukkitTask::cancel);
        countdowns.clear();
    }

    private void announce(MenhirBlock block, int secondsLeft) {
        Countdown countdown = countdowns.get(block.getId());
        if (countdown == null || !countdown.announced.add(secondsLeft)) return;
        if (!block.getCoolDown().isActive()) return;
        CountdownSettings settings = block.getCountdownSettings();
        LangConfig lang = plugin.getConfiguration().getLangConfig();

        plugin.getEvents().respawnCountdown(block, secondsLeft);

        PlaceholderSet placeholders = new BlockPlaceholderSet(block);
        String time = lang.formatDuration(secondsLeft);
        Component chat = render(placeholders, lang.getCountdownChat(), time);
        boolean showTitle = settings.getTitle().isEnabled()
                && (!settings.getTitle().isOnlyLast() || secondsLeft == settings.getLastThreshold());
        Title title = showTitle ? Title.title(
                render(placeholders, lang.getCountdownTitle(), time),
                render(placeholders, lang.getCountdownSubtitle(), time),
                Title.Times.times(ticks(settings.getTitle().getFadeIn()), ticks(settings.getTitle().getStay()),
                        ticks(settings.getTitle().getFadeOut()))) : null;
        Sound sound = resolveSound(settings.getSound());

        for (Player player : audience(block, settings)) {
            if (!TextRenderer.plain(chat).isBlank()) player.sendMessage(chat);
            if (title != null) player.showTitle(title);
            if (sound != null) player.playSound(sound);
        }
    }

    private static Component render(PlaceholderSet placeholders, String raw, String time) {
        if (raw == null) return Component.empty();
        return TextRenderer.render(placeholders.parse(raw.replace("%time%", time)));
    }

    private static Duration ticks(int ticks) {
        return Duration.ofMillis(Math.max(0, ticks) * 50L);
    }

    private Collection<Player> audience(MenhirBlock block, CountdownSettings settings) {
        Location location = block.getLocation();
        World world = location.getWorld();
        return switch (settings.getBroadcastTo()) {
            case SERVER -> new ArrayList<>(plugin.getServer().getOnlinePlayers());
            case WORLD -> world == null ? List.of() : new ArrayList<>(world.getPlayers());
            case RADIUS -> {
                if (world == null) yield List.of();
                double rangeSq = settings.getRadius() * settings.getRadius();
                List<Player> players = new ArrayList<>();
                for (Player player : world.getPlayers()) {
                    if (player.getLocation().distanceSquared(location) <= rangeSq) players.add(player);
                }
                yield players;
            }
        };
    }

    /**
     * Accepts a sound key ({@code minecraft:block.note_block.pling}, {@code block.note_block.pling})
     * or the enum-style name ({@code BLOCK_NOTE_BLOCK_PLING}). Returns {@code null} for an empty value.
     */
    static Sound resolveSound(String value) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        Key key;
        if (trimmed.contains(":") || trimmed.contains(".")) {
            key = Key.key(trimmed.toLowerCase(Locale.ROOT));
        } else {
            key = SoundNames.keyOf(trimmed);
            if (key == null) return null;
        }
        return Sound.sound(key, Sound.Source.MASTER, 1f, 1f);
    }

}
