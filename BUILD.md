# Building Menhir

## Requirements

| Tool | Version |
|---|---|
| JDK | 21 (Temurin recommended) |
| Gradle | not needed — the wrapper (`./gradlew`, 9.3.0) downloads it |
| Internet access | for the first build (Paper API, PlaceholderAPI, VaultAPI, ACF, MineDown) |

The build uses a Java toolchain, so Gradle will look for a JDK 21 even if another Java is on your `PATH`.
If none is installed, Gradle can provision one (`org.gradle.java.installations.auto-download=true`).

## Build

```bash
./gradlew build          # compile, run tests, create the shaded jar
./gradlew test           # tests only
./gradlew shadowJar      # jar only
./gradlew apiJavadoc     # Javadoc of the public API (strict doclint) -> build/docs/api
./gradlew publishToMavenLocal   # install com.musbabaff:menhir into ~/.m2 for API consumers
```

On Windows use `gradlew.bat` instead of `./gradlew`.

The plugin jar is written to:

```
build/libs/Menhir-<version>.jar
```

It is self-contained: ACF and MineDown are shaded and relocated under `com.musbabaff.menhir.libs`.
Everything else (Adventure, MiniMessage, Gson, Guava) is provided by Paper.

`-Xlint:deprecation -Xlint:unchecked` are enabled; the build should compile without warnings.

## IDE setup

* **IntelliJ IDEA** — *File → Open* the project folder; IDEA picks up `build.gradle` and the wrapper.
  Enable annotation processing (Lombok) if the IDE does not do it automatically
  (*Settings → Build → Compiler → Annotation Processors*).
* **VS Code** — install *Extension Pack for Java* and *Gradle for Java*, open the folder, let the Java
  language server import the Gradle project. Lombok support is included in the extension pack.
* **Eclipse** — *File → Import → Existing Gradle Project*; install the Lombok agent for Eclipse.

## Running a test server

1. Download a Paper 1.21.4 jar from https://papermc.io/downloads/paper into a folder outside the repo
   (or into `run/`, which is git-ignored).
2. Start it once (`java -jar paper-1.21.4-*.jar --nogui`), accept the EULA, stop it.
3. Optional but useful: drop [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) into `plugins/`.
4. Copy `build/libs/Menhir-<version>.jar` into `plugins/` and start the server again.
5. Edit `plugins/Menhir/config.yml` (or copy `examples/escraft-config.yml` over it), then `/menhir reload`.

For a quick edit–build–test loop:

```bash
./gradlew shadowJar && cp build/libs/Menhir-*.jar /path/to/server/plugins/
```

To attach a debugger, start the server with

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar paper-1.21.4-*.jar --nogui
```

and connect your IDE's remote debugger to port 5005.

## Project layout

```
src/main/java/com/musbabaff/menhir/
  MenhirPlugin        plugin entry point, lifecycle, reload
  BlockRegistry       id/location lookup of loaded blocks
  block/              block model: health, cooldown, rewards, tool filters, top list, hologram binding
  hologram/           TextDisplay/ItemDisplay engine (HologramManager, HologramEntity)
  text/               TextRenderer — MiniMessage + legacy/hex → Adventure components
  afk/                AfkTracker (pure logic) and AfkService (listener)
  bossbar/            health boss bar shown while hitting
  countdown/          respawn countdown announcements (CountdownSchedule is pure logic)
  storage/            StorageProvider, MemoryStore, yaml/ and mysql/ backends, migration
  api/                public API: MenhirAPI, MenhirStone, TopEntry, event/*
  impl/               API implementation and the EventBridge that fires the events
  config/             config.yml parsing: lang, options, hologram, afk, bossbar, countdown, storage, blocks
  integration/        PlaceholderAPI hook + expansion, Vault prefix provider
  commands/           /menhir (ACF)
  gui/, menu/         in-game editor
  migration/          first-run copy of a MineBlocks data folder
src/main/resources/   config.yml, ACF message overrides (plugin.yml is generated from build.gradle)
src/test/java/        JUnit 5 tests
examples/             sample configs
```
