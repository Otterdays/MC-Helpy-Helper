# Help Helper - `/help` as a searchable command browser (Fabric)

<!-- Badges: versions in gradle.properties and fabric.mod.json -->
<p align="center">
  <a href="https://fabricmc.net/"><img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-26.1.2-44AF35?style=for-the-badge&logo=minecraft&logoColor=white" /></a>
  <a href="https://fabricmc.net/use/install/"><img alt="Fabric Loader" src="https://img.shields.io/badge/Fabric%20Loader-0.19.2-DB2233?style=for-the-badge&logoColor=white" /></a>
  <a href="https://modrinth.com/mod/fabric-api"><img alt="Fabric API" src="https://img.shields.io/badge/Fabric%20API-0.146.1%2B26.1.2-FFF04D?style=for-the-badge" /></a>
</p>
<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-25%2B-F40404?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img alt="Environment" src="https://img.shields.io/badge/Environment-*%20(client%20%26%20server)-5865F2?style=for-the-badge" />
</p>
<p align="center">
  <a href="https://github.com/Otterdays/MC-Helpy-Helper"><img alt="Repository" src="https://img.shields.io/badge/Source-MC--Helpy--Helper-24292f?style=for-the-badge&logo=github&logoColor=white" /></a>
</p>

**Help Helper** is a small [Fabric](https://fabricmc.net/) mod for **Minecraft Java 26.1.2**. When you run **`/help`**, the server sends your client a snapshot of commands you may use, and the client opens a large in-game browser with search, categories, action modes, templates, and scrolling.

Upstream home for packaging and releases: [Otterdays/MC-Helpy-Helper](https://github.com/Otterdays/MC-Helpy-Helper).

## Requirements

- Minecraft **26.1.2**, Fabric Loader pinned in [`gradle.properties`](gradle.properties).
- [**Fabric API**](https://modrinth.com/mod/fabric-api) on both **client** and **server**.
- Java **25+** per `fabric.mod.json`.

## Features

- **Searchable GUI** - Full-window command browser with real-time search and relevance scoring
- **Command Details** - Selected command shows category, aliases, risk label, templates, and description
- **Click Actions** - Run commands, copy to clipboard, or fill chat box (default, copy, edit modes)
- **Categories** - Filter by command family and vanilla command groups with colored accent bars
- **Display Modes** - Toggle compact or roomy row spacing; persistent across sessions
- **Keyboard Navigation** - Arrow keys, Page Up/Down, Home/End, plus dedicated keys for mode/density/favorite
- **Scrollbar** - Mouse wheel, drag scrollbar, or click track for page scrolling
- **Vanilla Catalog** - Known Minecraft commands get descriptions, aliases, templates, and risk labeling
- **Permission-Aware** - Only shows commands the player can use based on Brigadier permission checks
- **Favorites** - Star commands with `F` key; boosted in sort order and filterable by "Fav" quick filter
- **Recent Commands** - Last 12 executed commands tracked and ranked higher in default sort
- **Quick Filters** - Filter by All, Favorites, Recent, Vanilla, Modded, Safe, or Risky commands
- **Sort Modes** - Sort by Relevance (Top), A-Z, Recent execution, or Vanilla-first priority
- **UI State Persistence** - Favorites, recents, active filters, sort mode, and display settings saved locally
- **Refined Visual Design** - Zebra-striped rows, category color bars, subtle separators, outlined badges, and scaled-aware header

## Behavior

- **`/help`** (logical server command): if the issuer is a **player**, the server aggregates usable command paths for that source's permissions and sends them to that player's client in an S2C payload; the client opens `Help Helper`.
- **Search** filters rows across command text, category, aliases, and description.
- **Click** a row: runs the command, copies it, or fills chat depending on selected mode.
- **Templates** on the right panel open in chat for editing.
- **Risky commands** default to fill-chat when run mode is selected.
- **Scroll** wheel over the list area scrolls the list; track click pages; thumb drag works.

Non-players requesting `/help` get a chat error.

### Limitations / Expectations

- The mod must load on **the server your client is executing commands against**. Vanilla servers will not react to `/help` with this GUI.
- Dedicated servers hosting only this mod jar will advertise the custom payload Fabric networking expects clients to understand; vanilla clients connecting to those servers are unsupported.
- Command metadata is curated for common vanilla commands. Unknown server/mod commands still fall back to a generic entry.
- The UI shows command paths plus selected metadata, not full Brigadier syntax trees for every command.

## Building

```powershell
.\gradlew.bat clean build -q
```

Artifacts land under **`BUILT/libs/`** (see [`build.gradle`](build.gradle): `layout.buildDirectory`).

Developers can use `.\gradlew.bat runClient` / `runServer` once [Fabric runs](https://docs.fabricmc.net/develop/getting-started/setup) are wired as usual. This repo keeps Fabric Loom pinned for 26.1.2.

## Project Layout

```
src/
|- main/java/com/otterdays/helphelper/
|  |- HelpHelper.java              # Main mod initializer
|  |- HelpHelperCommands.java      # /help command registration
|  |- HelpCommandsSupport.java     # Brigadier tree traversal
|  `- network/
|     `- OpenHelpPayload.java      # S2C network payload
|- client/java/com/otterdays/helphelper/client/
|  |- CommandCatalog.java          # Client-side vanilla command metadata
|  |- HelpHelperClient.java        # Client initializer + receiver
|  `- HelpHelperScreen.java        # Full-window GUI screen
|- test/java/com/otterdays/helphelper/
|  `- HelpHelperTest.java          # Unit tests
`- main/resources/
   |- fabric.mod.json              # Mod metadata
   `- assets/helphelper/           # Mod assets (if any)
```

## Documentation

- **Feature details**: [`DOCS/FEATURES.md`](DOCS/FEATURES.md)
- **Codebase map**: [`LOCATIONS.md`](LOCATIONS.md)
- **Build configuration**: [`build.gradle`](build.gradle), [`gradle.properties`](gradle.properties)

## Recent Improvements

- ✅ **Font-aware layout** - Header height and text spacing scale properly at any GUI scale (fixes clipping on large fonts)
- ✅ **Favorites system** - Star commands with `F` key; stored in `config/helphelper/ui.json`
- ✅ **Recent history** - Last 12 executed commands tracked and ranked by recency
- ✅ **Quick filters** - Filter by All, Favorites, Recent, Vanilla, Modded, Safe, or Risky
- ✅ **Sort modes** - Top (relevance-based), A-Z, Recent, Vanilla-first
- ✅ **Visual polish** - Zebra-striped rows, row separators, category color bars, corner accents, outlined badges, panel gradients

## Planned Enhancements

See [`DOCS/FEATURES.md`](DOCS/FEATURES.md) for additional planned improvements:

- Expand vanilla command metadata coverage
- Group commands by mod/namespace
- Fuzzy search with advanced scoring algorithms
- Show required permission level per command
- Keyboard shortcut to open help GUI without typing `/help`
- Config file GUI for theme customization
- Command history with timestamps

## License

See [LICENSE](LICENSE) (currently All Rights Reserved in template metadata; you may publish your own SPDX ID when releasing).
