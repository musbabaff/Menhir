# Contributing to Menhir

Thanks for helping! This page explains how to report problems and how to get a change merged.

## Reporting bugs

Open a [bug report](https://github.com/musbabaff/Menhir/issues/new?template=bug_report.yml). Please include:

* the Menhir version (`/menhir version`), Paper build and Java version,
* the relevant part of `config.yml`,
* the server log (a stack trace if there is one),
* the exact steps to reproduce.

If the problem only appears with another plugin (PlaceholderAPI, ItemsAdder, Vault, a permissions
plugin), say which one and which version.

## Suggesting features

Open a [feature request](https://github.com/musbabaff/Menhir/issues/new?template=feature_request.yml).
Describe the problem you want to solve first; the proposed solution second.

## Pull requests

1. Fork the repository and create a branch from `main`: `git checkout -b feat/my-change`.
2. Make your change. Keep it focused — one topic per pull request.
3. Run `./gradlew build`. It must compile **without warnings** and all tests must pass.
4. Test on a Paper 1.21.4 server. If you touched config parsing, load `examples/escraft-config.yml`
   too — existing configs must keep working, keys are never removed or renamed without a fallback.
5. Add an entry to `CHANGELOG.md` under **Unreleased**.
6. Open the pull request; the template will ask for a short summary and test notes.

### Code style

* Java 21, 4-space indentation, UTF-8, LF line endings.
* Match the surrounding code; prefer small classes and methods. Immutable settings objects
  (`HologramSettings`, `AfkSettings`) return copies instead of mutating.
* Everything that touches Bukkit entities runs on the main thread. Anything that can be called from
  another thread (GUI callbacks, async tasks) must only flip a flag or schedule a task.
* Log through `plugin.getLogger()`; no `System.out`, no `printStackTrace()`.
* User-facing strings go through `TextRenderer` / `Colors` so every colour syntax works everywhere.
* Comments and documentation are written in English.

### License headers

* New files: `Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.` plus the MPL notice block
  (copy it from any new file, e.g. `text/TextRenderer.java`).
* Files that came from MineBlocks keep their attribution header
  (`Modifications (c) 2026 musbabaff — see NOTICE.md`). Do not remove it.

### Commit messages

[Conventional Commits](https://www.conventionalcommits.org/):

```
<type>: <short description>

<optional body: what and why, not how>
```

Types: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`, `ci`.

Examples:

```
feat: add %menhir_<block>_percent% placeholder
fix: keep hologram hidden while its chunk is unloaded
docs: describe hologram templates in README.tr.md
```

## Tests

Unit tests live in `src/test/java` and use JUnit 5. Logic that does not need a running server
(text rendering, timeout formatting, AFK thresholds, config parsing) should be covered by a test.
Behaviour that needs a server (entities, events) is verified manually — describe what you tested in
the pull request.

## License

By contributing you agree that your contribution is licensed under the
[Mozilla Public License 2.0](LICENSE), the same license as the project.
