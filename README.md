# FPS Mod — barebones Fabric template

<!-- Badges: versions in gradle.properties; style=for-the-badge; logos use Shields + Simple Icons only -->
<p align="center">
  <a href="https://fabricmc.net/"><img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-26.1.2-44AF35?style=for-the-badge&logo=minecraft&logoColor=white" /></a>
  <a href="https://fabricmc.net/use/install/"><img alt="Fabric Loader" src="https://img.shields.io/badge/Fabric%20Loader-0.19.2-DB2233?style=for-the-badge&logoColor=white" /></a>
  <a href="https://modrinth.com/mod/fabric-api"><img alt="Fabric API" src="https://img.shields.io/badge/Fabric%20API-0.146.1%2B26.1.2-FFF04D?style=for-the-badge" /></a>
</p>
<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-25%2B-F40404?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img alt="Loom" src="https://img.shields.io/badge/Loom-1.16--SNAPSHOT-7C4DFF?style=for-the-badge" />
  <img alt="Gradle" src="https://img.shields.io/badge/Gradle-9.4.1-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
  <img alt="Mod version" src="https://img.shields.io/badge/Mod-1.0.0-007EC6?style=for-the-badge" />
</p>
<p align="center">
  <img alt="Client only" src="https://img.shields.io/badge/Environment-client-5865F2?style=for-the-badge" />
  <a href="https://github.com/Otterdays/Minecraft-Fabric-Sample-Mod/blob/main/LICENSE"><img alt="License" src="https://img.shields.io/badge/License-CC0%201.0-555555?style=for-the-badge" /></a>
  <a href="https://github.com/FabricMC/fabric-example-mod"><img alt="Template lineage" src="https://img.shields.io/badge/Template-fabric--example--mod-0E0E0E?style=for-the-badge&logo=github&logoColor=white" /></a>
</p>

<p align="center">
  <strong>Version snapshot</strong> (see <code>gradle.properties</code> and <a href="src/main/resources/fabric.mod.json">fabric.mod.json</a> when you fork)
</p>

<!-- Small images: paths work on GitHub; width keeps the readme compact -->
<p align="center">
  <sub>In-repo artwork (for launcher / Mod Menu icon fields)</sub><br />
  <a href="src/main/resources/assets/fpsmod/icon.png"><img src="src/main/resources/assets/fpsmod/icon.png" width="120" height="120" alt="Default mod icon (full)" /></a>
  &nbsp;&nbsp;
  <a href="src/main/resources/assets/fpsmod/icon_modrinth_cropped.png"><img src="src/main/resources/assets/fpsmod/icon_modrinth_cropped.png" width="120" height="120" alt="Cropped icon variant" /></a>
</p>

**FPS Mod** is a tiny [Fabric](https://fabricmc.net/) **client** mod: a top-left **FPS readout** (refreshed about once per second) and a **show/hide** control, with clear logs. This repository stays intentionally small: use it as a **template** to fork and extend, or drop the JAR in `mods` as-is.

## First check (before editing)

1. Read `LOCATIONS.md` first for quick file/symbol discovery.
2. Then use this README for behavior, compatibility, and run/test commands.

## What this template does right now

- Registers a Fabric mod entrypoint (`ModInitializer`)
- Registers a **client** entrypoint (`ClientModInitializer`) with a tiny HUD feature
- Logs clear startup status lines with emoji markers
- **In-game FPS overlay (lightweight)** — top-left HUD text, **updates once per second** (no per-frame FPS math)
- **Toggle** — use **Hide FPS / Show FPS** screen button to enable/disable the readout. Setting is saved under `config/fpsmod/hud.properties`
- Builds a valid mod jar for `mods/` using pinned Fabric + Minecraft versions (includes `src/client/java` via Loom split source sets)
- Includes a starter JUnit 5 test harness

### Launcher / mod list display (Prism, MultiMC, Mod Menu)

Fabric reads `fabric.mod.json` for the name, description, authors, license, `contact`, and **`icon`**. This repo ships **`assets/fpsmod/icon.png`** (placeholder) so launchers can show a thumbnail in the mods panel. After forking, replace the PNG with your own icon (same path or update the `icon` field) and update `authors` / all **`contact`** entries (`homepage`, `sources`, `issues`) to match your project. Optional: **`suggests.modmenu`** (this template includes it) soft-recommends Mod Menu when present; the game still runs without it.

**Mod Menu “Client” badge:** set top-level **`"environment": "client"`** (this template does). That tells Fabric Loader the mod is **client-only**; Mod Menu surfaces it as the blue **Client** tag. Use `"*"` only if you intentionally load the same jar on dedicated servers too.

### In-game FPS HUD (behavior)

- Renders as a Fabric [`HudElementRegistry`](https://maven.fabricmc.net/docs/fabric-api-0.146.1+26.1.2/net/fabricmc/fabric/api/client/rendering/v1/hud/HudElementRegistry.html) layer **before** `VanillaHudElements.MISC_OVERLAYS`, so it stacks like normal HUD content and respects **Hide HUD** (`F1`).
- **FPS value** is sampled from the client’s built-in FPS counter **at most once per second** to keep work minimal.
- **Toggle control** is a screen button (top-left) added to UI screens (inventory/pause/chat/pause menu) **only while you are in a world** (`client.level` and `client.player` must exist). The main/title menu does not get this button; load or join a world first.
- The world HUD now renders FPS text only (no clickable world-space button). The log prints `🔁` when state changes.
- Screen button injection uses Fabric Screen API (`ScreenEvents.AFTER_INIT` + `Screens.getWidgets(screen).add(button)`), which is compatible with current access rules.

## Known UX caveats

- Pressing `F1` (Hide HUD) hides the in-world FPS text.
- Pause/inventory/chat can dim or cover world HUD layers depending on screen rendering; use the screen-level top-left button there.
- Toggle input is handled by screen widgets (top-left button on screens), not by world HUD mouse clicks. If no screen is open, open inventory or pause (in a world) to use the button.
- No screen toggle on the title menu until a world is loaded (by design; see `FpsHudScreenButton`).
- FPS text updates once per second by design, so it is intentionally not frame-perfect every render tick.

## Tech stack (at a glance)

- Minecraft: `26.1.2`
- Mod loader: `Fabric Loader 0.19.2`
- API: `Fabric API 0.146.1+26.1.2`
- Language/runtime: `Java 25+`
- Build: `Gradle 9.4.1` + `Fabric Loom 1.16-SNAPSHOT`
- Layout: **split** `main` + `client` source sets (see `build.gradle` `loom { splitEnvironmentSourceSets() ... }`)

## Version compatibility

These versions are declared in `gradle.properties` and `src/main/resources/fabric.mod.json`. Keep them in sync when upgrading.

| Component | Version | Notes |
|-----------|---------|--------|
| **Minecraft** | **26.1.2** | `minecraft_version`; mod metadata uses `~26.1.2` (same release line). |
| **Fabric Loader** | **0.19.2** | `loader_version` for Gradle; `fabric.mod.json` requires `fabricloader` **>= 0.19.2**. |
| **Fabric API** | **0.146.1+26.1.2** | `fabric_api_version` — use this (or a compatible newer API for **26.1.2**) in-game. |
| **Java** | **25+** | Required by Minecraft 26.x; `fabric.mod.json` depends on `java` **>= 25**. |

Build tooling: **Fabric Loom** is set to **1.16-SNAPSHOT** in `gradle.properties` (see [Fabric develop](https://fabricmc.net/develop) when upgrading).

Versions are aligned with the official [Fabric example mod](https://github.com/FabricMC/fabric-example-mod) **26.1.2** template line.

## Purpose

Use this repo as a template:
- copy/fork/duplicate it into a new repository
- rename IDs/packages/metadata for your own mod
- start building features from this known-good baseline

## Requirements

- **JDK 25** — required for Minecraft 26.1.2. This project uses a Gradle Java toolchain (with automatic JDK provisioning when possible).
- **Fabric Loader** and **Fabric API** — install the versions in the compatibility table above (or compatible builds for the same Minecraft version).

## Build

From the repo root:

```bat
build.bat
```

Or:

```bat
gradlew.bat build
```

The mod JAR for your `mods` folder is **`build\libs\fps-mod-1.0.0.jar`** (name follows `mod_version` in `gradle.properties`). Use **Minecraft 26.1.2** with **Fabric Loader 0.19.2+** and a matching **Fabric API** (see compatibility table).

Optional dev client:

```bat
gradlew.bat runClient
```

Run unit tests:

```bat
gradlew.bat test
```

## Debug logging convention

This template uses emoji-prefixed logs to make startup diagnostics easy to scan in the console:

- `🔍` debug/trace lifecycle details
- `✅` successful readiness checkpoints
- `⚠️` warning states that are non-fatal

Current startup debug output includes:
- initialization start
- Java version, detected Minecraft version, mod version
- ready heartbeat on successful load
- environment notice (dev vs non-dev)

If you add new logs, keep the emoji prefix so important lines stay visually obvious.

Example log lines:

```text
🔍 Starting mod initialization...
🔍 Environment: java=25, minecraft=26.1.2, modVersion=1.0.0
✅ fpsmod loaded and ready.
```

When you toggle the HUD control in-game, look for:

```text
fpsmod 🔁 FPS HUD enabled
fpsmod 🔁 FPS HUD disabled
```

## Test strategy (template baseline)

This repo includes a tiny JUnit 5 test setup (`gradlew.bat test`) with a starter test in `src/test/java`.

Current test file:
- `src/test/java/com/fpsmod/FpsModTest.java` (template sanity check for `MOD_ID`)

Recommended growth path:
1. Keep **unit tests** for pure logic/helpers (fast, no game runtime needed).
2. Add **integration checks** for config/resource loading where possible.
3. Use `runClient` smoke checks for gameplay behavior that requires Minecraft runtime (ex: confirm the FPS HUD renders, toggles, and writes `config/fpsmod/hud.properties`).

## After you duplicate this repo

1. **Mod id** — change `fpsmod` in `src/main/resources/fabric.mod.json` and in code (`FpsMod.MOD_ID`).
2. **Client entrypoint** — update `fabric.mod.json` → `entrypoints.client` if you rename `FpsModClient`.
3. **Maven group / package** — replace `com.fpsmod` (Java package + `maven_group` in `gradle.properties`).
4. **Project name** — update `rootProject.name` in `settings.gradle` if you want a different Gradle project name.
5. **Display name** — edit `name` / `description` in `fabric.mod.json`.
6. **Mod panel metadata** — update `authors`, all `contact` fields (`homepage`, `sources`, `issues`), and `icon` in `fabric.mod.json` for Prism/MultiMC/Mod Menu display. Adjust or remove `suggests.modmenu` if you prefer a minimal `fabric.mod.json`.
7. **License** — replace `LICENSE` and the `license` field in `fabric.mod.json` if you are not using CC0.
8. **Compatibility** — when you change Minecraft or Fabric versions, update `gradle.properties`, `fabric.mod.json` `depends`, and this README’s compatibility table so they stay in sync.

## License

See `LICENSE` (CC0-1.0 as shipped; change after forking if you need a different license).
