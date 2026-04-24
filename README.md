# FPS Mod — barebones Fabric template

This repository is intentionally **minimal**: a tiny Fabric mod for **Minecraft 26.1.2** plus a working Gradle setup. Treat it as a **starter you duplicate**, not a feature-complete mod.

**Use it as a template:** copy or fork this repo (or clone and push to a new remote), then rename IDs, packages, and metadata for your own project and build from there. The code only logs on load so you have a clean place to add content.

## Compatibility

These are the versions this template is built and tested against (also declared in `gradle.properties` and `src/main/resources/fabric.mod.json`). Update all of them together when you move to a new Minecraft line.

| Component | Version | Notes |
|-----------|---------|--------|
| **Minecraft** | **26.1.2** | `minecraft_version`; mod metadata uses `~26.1.2` (same release line). |
| **Fabric Loader** | **0.19.2** | `loader_version` for Gradle; `fabric.mod.json` requires `fabricloader` **>= 0.19.2**. |
| **Fabric API** | **0.146.1+26.1.2** | `fabric_api_version` — use this (or a compatible newer API for **26.1.2**) in-game. |
| **Java** | **25+** | Required by Minecraft 26.x; `fabric.mod.json` depends on `java` **>= 25**. |

Build tooling: **Fabric Loom** is set to **1.16-SNAPSHOT** in `gradle.properties` (see [Fabric develop](https://fabricmc.net/develop) when upgrading).

Versions are aligned with the official [Fabric example mod](https://github.com/FabricMC/fabric-example-mod) **26.1.2** template line.

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

## After you duplicate this repo

1. **Mod id** — change `fpsmod` in `src/main/resources/fabric.mod.json` and in code (`FpsMod.MOD_ID`).
2. **Maven group / package** — replace `com.fpsmod` (Java package + `maven_group` in `gradle.properties`).
3. **Project name** — update `rootProject.name` in `settings.gradle` if you want a different Gradle project name.
4. **Display name** — edit `name` / `description` in `fabric.mod.json`.
5. **License** — replace `LICENSE` and the `license` field in `fabric.mod.json` if you are not using CC0.
6. **Compatibility** — when you change Minecraft or Fabric versions, update `gradle.properties`, `fabric.mod.json` `depends`, and this README’s compatibility table so they stay in sync.

## License

See `LICENSE` (CC0-1.0 as shipped; change after forking if you need a different license).
