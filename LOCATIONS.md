# LOCATIONS.md — quick codebase map

Use this as the first stop for quick discovery.

## First-check workflow (for future agents/builders)

1. Read `LOCATIONS.md` (this file) for fast pathing.
2. Read `README.md` for behavior, compatibility, and run/test commands.
3. Read `build.gradle` and `gradle.properties` before changing versions/deps; read `gradle/wrapper/gradle-wrapper.properties` when changing the Gradle distribution.
4. Read `src/main/resources/fabric.mod.json` before changing IDs/entrypoints.

## Core implementation

- Main mod initializer: `src/main/java/com/otterdays/helphelper/HelpHelper.java`
- `/help` registration + dispatcher usage collection: `src/main/java/com/otterdays/helphelper/HelpHelperCommands.java`, `HelpCommandsSupport.java`
- Networking payload: `src/main/java/com/otterdays/helphelper/network/OpenHelpPayload.java`
- Client initializer + receiver: `src/client/java/com/otterdays/helphelper/client/HelpHelperClient.java`
- Full-window help GUI: `src/client/java/com/otterdays/helphelper/client/HelpHelperScreen.java`
- Unit test: `src/test/java/com/otterdays/helphelper/HelpHelperTest.java`

## Mod metadata + wiring

- Mod metadata and entrypoints: `src/main/resources/fabric.mod.json` (`environment` is `*` so the server can register `/help` and networking; optional `suggests.modmenu` when Mod Menu is installed)
- Readme-only icon extras (legacy template): `readme-assets/icon_modrinth_cropped.png`
- Mod id constant: `HelpHelper.MOD_ID` in `src/main/java/com/otterdays/helphelper/HelpHelper.java`
- Client entrypoint target class: `com.otterdays.helphelper.client.HelpHelperClient` in `fabric.mod.json`

## Build + toolchain

- Gradle build config: `build.gradle`
- Version pins (Minecraft/Fabric/Loader/Loom): `gradle.properties`
- **Gradle wrapper** (exact distribution ZIP): `gradle/wrapper/gradle-wrapper.properties` — this repo uses a **9.6 nightly** from `services.gradle.org/distributions-snapshots/` (not `distributions/`).
- Project name + plugin repos: `settings.gradle`
- Windows build shortcut: `build.bat`
- Gradle wrappers: `gradlew.bat`, `gradlew`, `gradle/wrapper/`

## Feature behavior map (Help Helper)

- S2C payload registration: `HelpHelper.onInitialize()`
- `/help` server registration: `HelpHelperCommands.register()` via `CommandRegistrationCallback`
- Command path collection / permission-aware Brigadier traversal: `HelpCommandsSupport.collectFor(CommandSourceStack)`
- Client GUI open handler: `HelpHelperClient` → `HelpHelperScreen` (search filter, scrolling, clickable rows issuing `sendUnattendedCommand`)

## Logging conventions

- Startup heartbeat: `src/main/java/com/otterdays/helphelper/HelpHelper.java`
- Keep INFO-level lines short; avoid spam on hot paths (`HelpHelperScreen` render/tick-like paths stay quiet).

## Commands you will use most

- Build jar: `gradlew.bat build` (or `build.bat`)
- Run dev client: `gradlew.bat runClient`
- Run tests: `gradlew.bat test`

## Output paths

- Main mod jar: `BUILT/libs/help-helper-1.0.0.jar`
- Sources jar: `BUILT/libs/help-helper-1.0.0-sources.jar`
- Test report: `BUILT/reports/tests/test/index.html`
- Gradle problems report: `BUILT/reports/problems/problems-report.html`

## Safe-edit hotspots

- UI-only changes: `src/client/java/com/otterdays/helphelper/client/`
- Networking + `/help`: `HelpHelperCommands`, `HelpCommandsSupport`, `OpenHelpPayload`
- Cross-cutting metadata changes: `fabric.mod.json` + `gradle.properties` + `README.md`

## Git / upstream

- Product repository: [`https://github.com/Otterdays/MC-Helpy-Helper`](https://github.com/Otterdays/MC-Helpy-Helper). Update Git remotes and `fabric.mod.json` → `contact` when publishing.
