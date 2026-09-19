# Notice

Menhir is a fork of **MineBlocks**.

| | |
|---|---|
| Original project | MineBlocks |
| Original author | RAIXOCZ |
| Original repository | https://github.com/RAIXOCZ/MineBlocks |
| Original license | Mozilla Public License 2.0 (the `LICENSE` file in this repository is that license, unchanged) |
| Fork | Menhir — https://github.com/musbabaff/Menhir |
| Fork author | musbabaff (Mustafa Babayev) |
| Fork license | Mozilla Public License 2.0 |

## Attribution in source files

* Files that originate from MineBlocks keep the original work under its MPL-2.0 terms and carry the header
  `Modifications (c) 2026 musbabaff — see NOTICE.md`.
* Files written from scratch for Menhir carry `Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.`
* `src/main/resources/acf-core_en.properties` and `acf-minecraft_en.properties` are message overrides for
  [Aikar's Command Framework](https://github.com/aikar/commands) and keep their MIT header.

## What this fork changed (summary)

* Renamed the plugin, packages (`cz.raixo.blocks` → `com.musbabaff.menhir`), command (`/menhir`), and
  permissions (`menhir.*`).
* Removed every hard dependency: DecentHolograms, CMI/CMILib, Essentials, AFKPlus, UltraAFK and LuckPerms
  integrations were deleted together with the bundled API jars.
* Added a built-in hologram engine based on Paper's `TextDisplay` / `ItemDisplay` entities.
* Added built-in AFK protection based on player movement.
* Replaced the `mb` PlaceholderAPI expansion with a larger `menhir` expansion.
* Added hologram templates, per-block AFK overrides and a MiniMessage-aware text renderer.
* Removed the SpigotMC update checker / self-updater and the bStats metrics of the original project
  (both were tied to the original author's accounts).
* Player prefixes are now read from Vault's chat service instead of LuckPerms directly.
* Targets Paper 1.21.4 / Java 21.

See `CHANGELOG.md` for the complete list.

## Third-party libraries shaded into the jar

| Library | License |
|---|---|
| [Aikar's Command Framework (ACF)](https://github.com/aikar/commands) | MIT |
| [MineDown (Adventure)](https://github.com/Phoenix616/MineDown) | MIT |
