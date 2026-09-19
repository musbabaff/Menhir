# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] - 2026-09-19

First release of Menhir, a fork of [MineBlocks 2.1.7](https://github.com/RAIXOCZ/MineBlocks) by RAIXOCZ.

### Added

- Built-in hologram engine using Paper `TextDisplay` entities — no hologram plugin is needed any more.
  Entities are non-persistent, carry a `menhir_hologram` marker in their persistent data container, are
  re-created when their chunk loads and swept away on reload, shutdown and chunk load (leftover cleanup).
- `#ICON: %type%` / `#ICON: <material>` hologram lines are rendered as a slowly rotating `ItemDisplay`
  above the text. Unknown materials are reported once in the console and skipped.
- Hologram appearance settings, globally under `options.hologram` and per block under `blocks.<id>.hologram`:
  `scale`, `billboard`, `background`, `text-shadow`, `see-through`, `alignment`, `view-distance`,
  `update-interval` (seconds, `-1` = live), `line-width`.
- `hologram-templates` section and `blocks.<id>.hologram.template` to share one hologram definition
  between blocks. A template named `default` is applied automatically; block keys override template keys.
- Text renderer that understands MiniMessage (`<gradient:#FF4500:#FFD700><b>…</b></gradient>`), legacy
  `&` codes, `&#RRGGBB` / `{#RRGGBB}` / bare `#RRGGBB` hex colours, per-character hex gradients and the
  IridiumColorAPI `<#A>…</#B>` / `<RAINBOW1>…</RAINBOW>` syntaxes. MiniMessage is parsed first, legacy
  codes second.
- PlaceholderAPI placeholders (including ItemsAdder font placeholders such as `%img_general_icon_16%`) are
  resolved on every hologram update.
- Built-in AFK protection (`options.afk.{enabled, seconds, notification-type}`, overridable per block under
  `blocks.<id>.afk`). Only position changes count as activity; a hit by an AFK player is ignored and the
  `lang.status.afk` message is shown at most once every two seconds.
- `TITLE` and `NONE` notification types.
- PlaceholderAPI expansion `menhir`: `%menhir_<block>_health%`, `_max_health`, `_percent`, `_status`,
  `_timeout`, `_top_<n>_name`, `_top_<n>_breaks`, `_my_breaks`, `_my_rank`, `%menhir_global_top_<n>_name%`,
  `%menhir_global_top_<n>_breaks%` and `%menhir_next_respawn%`.
- Hologram text is only re-sent when it actually changed, and never while no player is within
  `view-distance`.
- `tool.types` accepts a YAML map (`STONE_.*: ALLOWED`) in addition to the list-of-strings form.
- First-run migration: if `plugins/Menhir/config.yml` does not exist but `plugins/MineBlocks/` does, its
  `config.yml` and `storage/` are copied (the old folder is left untouched).
- Player prefixes (`%player_<n>_prefix%`) through Vault's chat service.
- Unit tests for the text renderer, timeout formatter, AFK threshold logic, hologram settings and legacy
  config compatibility; GitHub Actions workflow building the jar with Java 21.
- `examples/escraft-config.yml`, a two-block MineBlocks-era config used as a compatibility smoke test.
- Documentation: `README.md`, `README.tr.md`, `BUILD.md`, `CONTRIBUTING.md`, `NOTICE.md`.

### Changed

- Plugin renamed to **Menhir**; packages moved from `cz.raixo.blocks` to `com.musbabaff.menhir`; main
  class is `MenhirPlugin`; artifact is `Menhir-<version>.jar`; data folder is `plugins/Menhir/`.
- Command is `/menhir` with aliases `metin`, `menhirstone` and `mb` (kept for muscle memory).
- Permissions are `menhir.admin` (parent) and `menhir.<subcommand>` (`menhir.reload`, `menhir.edit`, …).
- Target platform is Paper 1.21.4 / Java 21 (`api-version: 1.21`). Spigot is no longer supported because
  the hologram engine and messaging use Paper's Adventure API.
- Chat messages, action bars and GUI item names/lore are sent as Adventure components; the
  `adventure-platform-bukkit` shade is gone.
- `options.notification-type` now defaults the AFK notification type as well as the other status messages.
- Item names in `tool.names` are compared by their visible text (formatting is ignored).
- `/menhir version` prints the installed version and the release page instead of querying SpigotMC.
- Legacy config keys are still read but log a one-time deprecation hint:
  `options.afk-integration-enabled` → `options.afk.enabled`,
  `options.notification-type` → `options.afk.notification-type`,
  `options.hologram-update-interval` (ticks) → `options.hologram.update-interval` (seconds).
- Player heads in the GUI editor use the Paper profile API instead of raw NBT.
- Build: Gradle wrapper (9.3.0) committed; JUnit is a test-only dependency (it used to be shaded).

### Removed

- DecentHolograms and CMI/CMILib hologram integrations and the `libs/` API jars.
- Essentials, CMI, AFKPlus and UltraAFK AFK integrations.
- Direct LuckPerms integration (replaced by Vault).
- SpigotMC update checker and `/mb update` self-updater.
- bStats metrics (the plugin id belonged to the original project).
- Unused `util/PAPI` helper.

### Fixed

- `BlockTop` could insert a player above a better one when the list was full; the leaderboard is now
  strictly sorted by breaks.
- `tool.names` / map-form `tool.types` keys containing a dot were split by Bukkit's path separator; they
  are re-joined when read.
- Failed offline-reward writes are logged through the plugin logger instead of `printStackTrace`.
- A block's inactivity-reset task is cancelled when the block is unloaded.

[Unreleased]: https://github.com/musbabaff/Menhir/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/musbabaff/Menhir/releases/tag/v2.0.0
