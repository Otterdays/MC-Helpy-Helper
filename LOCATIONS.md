# LOCATIONS.md - Quick Codebase Map

Use this as the first stop for quick discovery.

## First-check Workflow

1. Read `LOCATIONS.md` first.
2. Read `README.md` for behavior, compatibility, and build commands.
3. Read `DOCS/FEATURES.md` for feature details and planned enhancements.
4. Read `build.gradle` and `gradle.properties` before changing versions or deps.
5. Read `src/main/resources/fabric.mod.json` before changing IDs or entrypoints.

## Core Implementation

- **Main mod initializer**: `src/main/java/com/otterdays/helphelper/HelpHelper.java`
  - Registers S2C payload type and `/help` command
  - Defines `MOD_ID = "helphelper"` and logger
- **`/help` registration + dispatcher usage collection**: `src/main/java/com/otterdays/helphelper/HelpHelperCommands.java`, `HelpCommandsSupport.java`
  - `HelpHelperCommands`: registers `/help` via `CommandRegistrationCallback`
  - `HelpCommandsSupport`: traverses Brigadier tree, respects permissions, returns sorted command list
- **Networking payload**: `src/main/java/com/otterdays/helphelper/network/OpenHelpPayload.java`
  - Record implementing `CustomPacketPayload`
  - Uses Fabric networking `StreamCodec`
  - Payload type: `helphelper:open_help`
- **Client initializer + receiver**: `src/client/java/com/otterdays/helphelper/client/HelpHelperClient.java`
  - Registers global receiver for `OpenHelpPayload`
  - Opens `HelpHelperScreen` on client thread
- **Full-window help GUI**: `src/client/java/com/otterdays/helphelper/client/HelpHelperScreen.java`
  - Search box with filtering
  - Category buttons with preferred vanilla ordering
  - Clickable command rows with 3 action modes
  - Details panel with metadata and templates
  - Scrollbar with drag support
  - Keyboard navigation
  - Compact/Roomy display toggle
- **Client command catalog**: `src/client/java/com/otterdays/helphelper/client/CommandCatalog.java`
  - Curated vanilla command metadata
  - Friendly titles, aliases, descriptions, presets, and risk labels
- **Unit tests**: `src/test/java/com/otterdays/helphelper/HelpHelperTest.java`
  - Minimal MOD_ID stability test only

## Mod Metadata + Wiring

- **Mod metadata and entrypoints**: `src/main/resources/fabric.mod.json`
  - `environment` is `*` (both client and server)
  - Entrypoints: `main` -> `HelpHelper`, `client` -> `HelpHelperClient`
  - Optional `suggests.modmenu` when Mod Menu is installed
- **Mod ID constant**: `HelpHelper.MOD_ID` in `src/main/java/com/otterdays/helphelper/HelpHelper.java`
- **Client entrypoint target class**: `com.otterdays.helphelper.client.HelpHelperClient` in `fabric.mod.json`

## Build + Toolchain

- **Gradle build config**: `build.gradle`
  - Uses Fabric Loom with `splitEnvironmentSourceSets()`
  - Custom build directory: `BUILT/`
  - Java 25 toolchain
  - Generates sources JAR
- **Version pins**: `gradle.properties`
  - `minecraft_version=26.1.2`
  - `loader_version=0.19.2`
  - `fabric_api_version=0.146.1+26.1.2`
  - `mod_version=1.0.0`
- **Gradle wrapper**: `gradle/wrapper/gradle-wrapper.properties`
  - Uses Gradle 9.6.0 nightly from services.gradle.org snapshots
- **Project name + plugin repos**: `settings.gradle`
- **Windows build shortcut**: `build.bat`
- **Gradle wrappers**: `gradlew.bat`, `gradlew`, `gradle/wrapper/`

## Feature Behavior Map

- **S2C payload registration**: `HelpHelper.onInitialize()` -> `PayloadTypeRegistry.clientboundPlay().register()`
- **`/help` server registration**: `HelpHelperCommands.register()` via `CommandRegistrationCallback`
- **Command path collection**: `HelpCommandsSupport.collectFor(CommandSourceStack)`
  - Iterates `dispatcher.getRoot().getChildren()`
  - Respects `node.canUse(source)`
  - Returns sorted `List<String>` of `/`-prefixed commands
- **Client GUI open handler**: `HelpHelperClient.onInitializeClient()` -> registers `ClientPlayNetworking.registerGlobalReceiver()`
  - Receives `OpenHelpPayload`, opens `HelpHelperScreen` via `client.execute()`

## Logging Conventions

- **Startup heartbeat**: `HelpHelper.java` -> `LOGGER.info("Help Helper registered /help GUI channel")`
- Keep INFO-level lines short.

## Commands You Will Use Most

- **Build JAR**: `.\gradlew.bat build`
- **Run dev client**: `.\gradlew.bat runClient`
- **Run dev server**: `.\gradlew.bat runServer`
- **Run tests**: `.\gradlew.bat test`
- **Clean build**: `.\gradlew.bat clean build -q`

## Output Paths

- **Main mod JAR**: `BUILT/libs/help-helper-1.0.0.jar`
- **Sources JAR**: `BUILT/libs/help-helper-1.0.0-sources.jar`
- **Test report**: `BUILT/reports/tests/test/index.html`
- **Gradle problems report**: `BUILT/reports/problems/problems-report.html`

## Safe-edit Hotspots

- **UI-only changes**: `src/client/java/com/otterdays/helphelper/client/`
  - `HelpHelperScreen.java` - GUI layout, rendering, interaction
  - `CommandCatalog.java` - vanilla metadata, aliases, presets
- **Networking + `/help`**:
  - `HelpHelperCommands.java` - command registration, permission checks
  - `HelpCommandsSupport.java` - command collection logic
  - `OpenHelpPayload.java` - network payload structure
- **Cross-cutting metadata changes**: `fabric.mod.json` + `gradle.properties` + `README.md`

## Known Issues to Address

1. Leftover FPS Mod artifacts - remove `fpsmod` assets and directories from previous project iteration.
2. Permission check - `HelpHelperCommands.java` uses `PermissionLevel.ALL` which may be too restrictive for `/help`.
3. Test coverage - only 1 test exists; add tests for command collection, permissions, payload serialization.

## Planned Enhancements

- Expand vanilla command metadata coverage.
- Group commands by mod/namespace.
- Add favorite/bookmark commands.
- Recent commands history.
- Fuzzy search instead of just substring.
- Show permission level required for each command.
- Config file for user preferences.
- Keyboard shortcut to open help without typing `/help`.

## Git / Upstream

- **Product repository**: [https://github.com/Otterdays/MC-Helpy-Helper](https://github.com/Otterdays/MC-Helpy-Helper)
- Update Git remotes and `fabric.mod.json` `contact` when publishing.
- Current license: All Rights Reserved (see LICENSE file).
