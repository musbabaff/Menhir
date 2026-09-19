# Menhir

Breakable leaderboard stones for Paper — with built-in holograms, AFK protection and PlaceholderAPI
support. No hologram plugin required.

[![Build](https://github.com/musbabaff/Menhir/actions/workflows/build.yml/badge.svg)](https://github.com/musbabaff/Menhir/actions/workflows/build.yml)
[![License: MPL-2.0](https://img.shields.io/badge/license-MPL--2.0-blue.svg)](LICENSE)
[![Paper 1.21.4](https://img.shields.io/badge/Paper-1.21.4-9cf.svg)](https://papermc.io/)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)

🇹🇷 [Türkçe README](README.tr.md)

<p align="center">
  <img src="docs/images/hologram.png" alt="A Menhir stone with its hologram (screenshot placeholder)" width="600">
</p>

## What is it

Menhir lets you place special blocks ("stones") anywhere in your world. Each stone has a health value;
every time a player hits it, the health goes down by one and the player's hit count goes up. When the
health reaches zero the stone "breaks": the players who hit it most get rewards, the block turns into a
placeholder material (bedrock, for example) for a cooldown, and then it comes back. A live hologram above
the stone shows the top players, the remaining health and the respawn countdown.

The plugin is a fork of [MineBlocks](https://github.com/RAIXOCZ/MineBlocks). It keeps the configuration
format and the in-game editor, but removes every external dependency: holograms are rendered with Paper's
own `TextDisplay` entities, AFK detection is built in, and everything runs on Paper 1.21.4 / Java 21.
An existing MineBlocks `config.yml` keeps working without changes.

## Features

* **Breakable stones** — health, per-player hit counts, top-10 leaderboard, cooldown with a placeholder
  material, automatic reset after inactivity or on restart, per-block permission.
* **Live holograms without an external plugin** — `TextDisplay`-based, updated only when the text changes
  and only while a player is nearby; per-block scale, billboard mode, background, shadow, alignment, view
  distance and update interval; `#ICON: <material>` lines become a rotating item model.
* **Every colour syntax** — MiniMessage gradients, legacy `&` codes, `&#RRGGBB` / `#RRGGBB` hex,
  per-character gradients, PlaceholderAPI placeholders (ItemsAdder font icons included).
* **Built-in AFK protection** — players who have not moved for *n* seconds cannot hit stones; only real
  position changes count, looking around does not.
* **Rewards per place** — commands for the 1st, 1st–3rd, … players, for every *n*-th hit, for the last hit,
  for a hit-count range; random ("chance;command") or all-commands mode; offline rewards are stored and
  delivered on login.
* **PlaceholderAPI expansion** `menhir` — health, percent, status, countdown, per-block and global tops,
  the viewer's own hits and rank, next respawn.
* **Hologram templates** — define the hologram once, reuse it on every stone.
* **In-game editor** — `/menhir edit <stone>` opens a GUI for location, type, health, hologram, timeout,
  rewards, tool filters and reset options.

## Requirements

| | |
|---|---|
| Server | [Paper](https://papermc.io/) 1.21.4 or newer (Paper API is required; Spigot is not supported) |
| Java | 21 |
| Optional | [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) (placeholders in holograms/commands and the `menhir` expansion), [ItemsAdder](https://itemsadder.devs.beer/) (font icons through PlaceholderAPI), [Vault](https://www.spigotmc.org/resources/34315/) (player prefixes in the top list) |

## Installation

1. Download `Menhir-<version>.jar` from the [releases page](https://github.com/musbabaff/Menhir/releases)
   and put it into `plugins/`.
2. Start the server once. `plugins/Menhir/config.yml` is created (if a `plugins/MineBlocks/` folder exists,
   its `config.yml` and `storage/` are copied over automatically).
3. Stand where the stone should be and run `/menhir create <name>`, then `/menhir edit <name>` — or edit
   `config.yml` and run `/menhir reload`.

## Configuration

The full default file is [`src/main/resources/config.yml`](src/main/resources/config.yml); a compact
legacy-style example is [`examples/escraft-config.yml`](examples/escraft-config.yml).

### `lang`

| Key | Description | Example |
|---|---|---|
| `top.nobody` | Name shown for an empty leaderboard slot | `"&cNobody"` |
| `top.nobody-breaks` | Hit count shown for an empty slot | `"0"` |
| `top.not_ranked` | `%menhir_<block>_my_rank%` for a player outside the top 10 | `"Not ranked"` |
| `timeout.message` | Countdown text; `%time%` is replaced by the remaining time | `"&cRespawn in %time%"` |
| `timeout.max-units` | How many units are shown (`1 hour 5 minutes` → 2) | `2` |
| `timeout.units.{hour,hours,minute,minutes,second,seconds}` | Unit suffixes | `" min"` |
| `status.timeout` | Shown when hitting a broken stone | `"&cYou can't destroy the block now!"` |
| `status.afk` | Shown when an AFK player hits a stone | `"&cYou are AFK!"` |
| `status.no-permission` | Shown when the player lacks the stone's permission | |
| `status.invalid-tool` | Shown when the tool is not allowed | |

### `options`

| Key | Description | Default |
|---|---|---|
| `afk.enabled` | Turn AFK protection on | `true` |
| `afk.seconds` | Seconds without movement after which a player counts as AFK | `15` |
| `afk.notification-type` | `ACTIONBAR`, `CHAT`, `TITLE` or `NONE` for the AFK message | `ACTIONBAR` |
| `notification-type` | Same choices, for the other status messages | `ACTIONBAR` |
| `block-break-limit` | Minimum milliseconds between two hits of one player (per-block `break-limit` overrides) | `20` |
| `offline-rewards` | Store rewards for offline players and run them on login | `true` |
| `hologram.*` | Global hologram defaults, see below | |
| `afk-integration-enabled`, `hologram-update-interval` | **Legacy** MineBlocks keys — still honoured, a deprecation hint is logged once | |

### Hologram settings (`options.hologram`, `hologram-templates.<name>`, `blocks.<id>.hologram`)

Settings are layered: built-in defaults ← `options.hologram` ← template ← block. A key set on the block
wins over the template, which wins over the global defaults.

| Key | Description | Default |
|---|---|---|
| `offset.{x,y,z}` | Offset of the hologram's **top** from the block centre + 1.5 blocks (same meaning as in MineBlocks) | `0` |
| `lines` | List of lines; a line whose text is empty after placeholders is not shown at all | |
| `template` | Name of a `hologram-templates` entry (block only). `default` is used automatically | |
| `scale` | Text size multiplier | `1.0` |
| `billboard` | `CENTER`, `VERTICAL`, `HORIZONTAL`, `FIXED` | `CENTER` |
| `background` | `transparent`, `default`, `#RRGGBB` or `#AARRGGBB` | `transparent` |
| `text-shadow` | Drop shadow behind the text | `false` |
| `see-through` | Visible through blocks | `false` |
| `alignment` | `CENTER`, `LEFT`, `RIGHT` | `CENTER` |
| `view-distance` | Blocks; no updates are sent while nobody is this close | `48` |
| `update-interval` | Seconds between re-renders; `-1` = every tick (text is only re-sent when it changed) | `-1` |
| `line-width` | Pixels before a line wraps | `1000` |

Special line: `#ICON: %type%` (or `#ICON: DIAMOND_BLOCK`) shows a slowly rotating model of that material
above the text. Unknown materials are reported once in the console and skipped.

Text formats, all usable in the same line:

| Syntax | Example |
|---|---|
| Legacy codes | `&7Top &8- &a%health%` |
| Hex | `&#2C74B3Text`, `{#2C74B3}Text`, `#2C74B3Text` |
| Per-character gradient | `&#FF0000M&#FF1100e&#FF2200n` |
| MiniMessage | `<gradient:#FF4500:#FFD700><b>MENHIR</b></gradient>` |
| MineBlocks gradient | `<#FF4500>MENHIR</#FFD700>` |
| PlaceholderAPI | `%img_general_icon_16%`, `%server_online%` |

Processing order is MiniMessage first, then legacy/hex codes.

```yaml
hologram-templates:
  default:
    scale: 1.1
    lines:
      - '#ICON: %type%'
      - '<gradient:#FF4500:#FFD700><b>MENHIR</b></gradient>'
      - '&7%player_1% &8- &e%player_1_breaks%'
      - '&7%player_2% &8- &e%player_2_breaks%'
      - '&a%health%&7/&a%max_health%'
      - '&c%timeout%'

blocks:
  north:
    hologram:
      template: default   # optional, "default" is picked automatically
      offset: {y: 1.2}
      billboard: VERTICAL # overrides the template
```

### `blocks.<id>`

| Key | Description |
|---|---|
| `location.{world,x,y,z}` | Block position |
| `type` | Material of the stone (`GOLD_BLOCK`, `DEEPSLATE_BRICKS`, …) |
| `health` | Hits needed to break it |
| `permission` | Permission required to hit it (`""` = none) |
| `break-limit` | Milliseconds between two hits of one player; `-1` = use `options.block-break-limit` |
| `hologram.*` | See above |
| `afk.{enabled,seconds,notification-type}` | Per-block AFK override (optional) |
| `timeout.time` | Cooldown after breaking, in seconds (`-1` = none) |
| `timeout.type` | Material shown during the cooldown (e.g. `BEDROCK`) |
| `timeout.respawn` | Message broadcast when the stone comes back (string or list) |
| `messages.break` | Message broadcast when the stone breaks (string or list) |
| `reset.inactive.time` | Seconds without a hit after which health and counters reset (`-1` = never) |
| `reset.inactive.message` | Broadcast on that reset |
| `reset.onrestart` | Reset health and counters when the server (re)starts |
| `tool.types` | List of `"<material regex>: ALLOWED\|DENIED"` strings **or** a map; `default` sets the fallback |
| `tool.enchantments.default` | Fallback for enchantments |
| `tool.enchantments.<name>.{level,type}` | `level` is a number or range (`4-5`), `type` `ALLOWED`/`DENIED` |
| `tool.names.default`, `tool.names.<display name>` | Item display name filter (visible text is compared) |
| `rewards.<name>.*` | See below |

### Rewards

```yaml
rewards:
  first:
    type: top
    place: 1            # or a range: 1-3
    mode: random        # random = pick by chance (default) | all = run every command
    commands:
      - "10;give %player% diamond"      # chance;command
      - "100;give %player% iron_ingot"
  every_fifth:
    type: break
    interval: 5         # every 5th hit of a player
    commands: ["100;give %player% iron_nugget"]
  final_hit:
    type: break
    condition: last     # also: "less than 5", "more than 10", "equal to 3", "5-10"
    commands: ["100;give %player% netherite_scrap"]
  grinder:
    type: break_count   # player's total hits on this stone when it breaks
    from: 10
    to: 50
    commands: ["100;give %player% emerald"]
```

Commands are run by the console. Placeholders inside commands and messages: `%player%`, `%uuid%`,
`%breaks%`, plus any PlaceholderAPI placeholder.

## Placeholders

### Inside hologram lines

| Placeholder | Value |
|---|---|
| `%health%` / `%max_health%` | Current / maximum health |
| `%type%` | Material name of the stone |
| `%timeout%` | Formatted remaining cooldown; empty (and the line is hidden) while the stone is alive |
| `%player_1%` … `%player_10%` | Name of the *n*-th player (or `lang.top.nobody`) |
| `%player_1_breaks%` … `%player_10_breaks%` | Hits of the *n*-th player (or `lang.top.nobody-breaks`) |
| `%player_1_prefix%` … | Vault chat prefix of the *n*-th player (empty without Vault) |
| any `%papi_placeholder%` | Resolved through PlaceholderAPI on every update |

### PlaceholderAPI expansion `menhir`

| Placeholder | Value |
|---|---|
| `%menhir_<block>_health%` | Current health |
| `%menhir_<block>_max_health%` | Maximum health |
| `%menhir_<block>_percent%` | Health in percent (0–100) |
| `%menhir_<block>_status%` | `alive` or `broken` |
| `%menhir_<block>_timeout%` | Formatted remaining cooldown, empty when alive |
| `%menhir_<block>_top_<n>_name%` | Name of the *n*-th player |
| `%menhir_<block>_top_<n>_breaks%` | Hits of the *n*-th player |
| `%menhir_<block>_my_breaks%` | The viewer's hits on this stone |
| `%menhir_<block>_my_rank%` | The viewer's rank (or `lang.top.not_ranked`) |
| `%menhir_global_top_<n>_name%` | Name of the *n*-th player by hits summed over all stones |
| `%menhir_global_top_<n>_breaks%` | Their summed hits |
| `%menhir_next_respawn%` | Formatted time until the nearest respawn, empty if no stone is broken |

The MineBlocks names (`%mb_<block>_hp%`, `_max_hp`, `_breaks`, `_rank`, `_top_<n>`, `_top_breaks_<n>`)
are accepted with the `menhir` identifier as well, e.g. `%menhir_north_hp%`.

## Commands & permissions

Base command: `/menhir` (aliases `/metin`, `/menhirstone`, `/mb`).

| Command | Description | Permission |
|---|---|---|
| `/menhir help` | Command overview | `menhir.help` |
| `/menhir reload` | Reload `config.yml` and every stone | `menhir.reload` |
| `/menhir list` | List stones and their locations | `menhir.list` |
| `/menhir create <id>` | Create a stone at your position | `menhir.create` |
| `/menhir edit <id>` | Open the GUI editor | `menhir.edit` |
| `/menhir remove <id>` | Delete a stone (console only; in game use the editor) | `menhir.remove` |
| `/menhir teleport <id>` | Teleport to a stone | `menhir.teleport` |
| `/menhir reset <id>` | Reset health and counters | `menhir.reset` |
| `/menhir sethealth <id> <n>` | Set the current health | `menhir.sethealth` |
| `/menhir hologram show <id>` | Show the hologram lines with edit links | `menhir.hologram` |
| `/menhir hologram addline <id> <text>` | Append a line | `menhir.hologram` |
| `/menhir hologram setline <id> <n> <text>` | Replace line *n* | `menhir.hologram` |
| `/menhir hologram removeline <id> <n>` | Remove line *n* | `menhir.hologram` |
| `/menhir version` | Installed version | `menhir.version` |
| `/menhir wiki` | Link to this page | — |

`menhir.admin` (default: OP) is required for every subcommand and grants all `menhir.*` permissions.

## Migrating from MineBlocks

* Drop `Menhir-<version>.jar` in, remove `MineBlocks-*.jar`. On first start Menhir **copies**
  `plugins/MineBlocks/config.yml` and `storage/` to `plugins/Menhir/`; the old folder is not touched.
* Your `config.yml` works unchanged: all `lang`, `options`, `blocks.<id>.*`, `tool`, `rewards` keys are read
  with the same meaning. `options.afk-integration-enabled`, `options.notification-type` and
  `options.hologram-update-interval` are still read (a one-time hint suggests the new keys).
* DecentHolograms, CMI, Essentials, AFKPlus, UltraAFK and LuckPerms are no longer needed. Holograms are
  drawn by Menhir itself; the old DecentHolograms holograms named `mineblock-<id>` can be deleted with
  `/dh delete mineblock-<id>` (or by removing the DecentHolograms plugin).
* `#ICON: %type%` lines keep working (rendered as a rotating item model).
* Command `/mb` still works as an alias; permissions changed from `mb.admin.*` to `menhir.*`
  (`mb.admin` → `menhir.admin`).
* PlaceholderAPI placeholders changed identifier: `%mb_…%` → `%menhir_…%` (old parameter names still work).
* Player prefixes come from Vault now: install Vault if you use `%player_<n>_prefix%`.
* Stone data (`storage/<id>.mb`) has the same format; nothing is lost.
* Requires Paper 1.21.4 and Java 21 — Spigot and older versions are not supported.

## FAQ

**Do I need DecentHolograms / CMI?**
No. Menhir renders holograms with Paper's `TextDisplay` entities. No other plugin is involved.

**The hologram is not visible / floats at the wrong height.**
Adjust `hologram.offset.y`. The offset is measured from the *top* of the hologram, like in MineBlocks, so
existing values keep their meaning. Make sure the chunk is loaded and you are within `view-distance`.

**Leftover hologram entities after a crash?**
Menhir marks its entities with a `menhir_hologram` key and removes unknown ones whenever a chunk loads,
on `/menhir reload` and on shutdown. Entities are non-persistent, so they are never written to the world.

**Why is a player "AFK" although they are looking around?**
Only position changes count. Turning the camera is not movement. Lower `options.afk.seconds` or set
`options.afk.enabled: false` if you do not want the check.

**Can I use ItemsAdder icons in hologram lines?**
Yes — install PlaceholderAPI and use the ItemsAdder font placeholders (`%img_…%`). They are resolved on
every hologram update.

**Does it work on Spigot / 1.20?**
No. The hologram engine needs Paper's Adventure and Display entity APIs (1.21.4+).

**How do I share one hologram layout between stones?**
Put it into `hologram-templates.default.lines` and delete the `lines` key from the blocks; keep only the
`offset` per block. A block that still has its own `lines` uses those instead.

**Can blocks have different AFK thresholds?**
Yes: `blocks.<id>.afk.seconds: 30` (and `enabled`, `notification-type`).

## Credits

Menhir is a fork of [MineBlocks](https://github.com/RAIXOCZ/MineBlocks) by **RAIXOCZ**, licensed under
the MPL-2.0. The block model, reward system, tool filters and the in-game editor come from that project;
see [NOTICE.md](NOTICE.md) for the attribution details and the full list of changes.

Shaded libraries: [ACF](https://github.com/aikar/commands) (MIT), [MineDown](https://github.com/Phoenix616/MineDown) (MIT).

## License

[Mozilla Public License 2.0](LICENSE). Modifications © 2026 musbabaff.
