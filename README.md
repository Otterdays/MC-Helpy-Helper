# FPS Mod — barebones Fabric template

This repository is intentionally **minimal**: a tiny Fabric mod for **Minecraft 26.1.2** plus a working Gradle setup. Treat it as a **starter you duplicate**, not a feature-complete mod.

**Use it as a template:** copy or fork this repo (or clone and push to a new remote), then rename IDs, packages, and metadata for your own project and build from there. The code only logs on load so you have a clean place to add content.

## Requirements

- **JDK 25** — Minecraft 26.1.2 targets Java 25. This project uses a Gradle Java toolchain (with automatic JDK provisioning when possible).
- **Fabric Loader** and **Fabric API** matching the game version (see `gradle.properties`).

Versions are pinned to match the official [Fabric example mod](https://github.com/FabricMC/fabric-example-mod) line for **26.1.2**.

## Build

From the repo root:

```bat
build.bat
```

Or:

```bat
gradlew.bat build
```

The mod JAR for your `mods` folder is **`build\libs\fps-mod-1.0.0.jar`** (name follows `mod_version` in `gradle.properties`). Run the game with Fabric Loader + Fabric API for **26.1.2**.

Optional dev client:

```bat
gradlew.bat runClient
```

## After you duplicate this repo

1. **Mod id** — change `fpsmod` in `src/main/resources/fabric.mod.json` and in code (`FpsMod.MOD_ID`).
2. **Maven group / package** — replace `com.fpsmod` (Java package + `maven_group` in `gradle.properties`).
3. **Project name** — update `rootProject.name` in `settings.gradle` if you want a different Gradle project name.
4. **Display name** — edit `name` / `description` in `fabric.mod.json`.
5. **License** — replace `LICENSE` and the `license` field in `fabric.mod.json` if you are not using CC0.

## License

See `LICENSE` (CC0-1.0 as shipped; change after forking if you need a different license).
