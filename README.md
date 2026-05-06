# Help Helper — `/help` as a searchable command browser (Fabric)

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

**Help Helper** is a small [Fabric](https://fabricmc.net/) mod for **Minecraft Java 26.1.2**: when you run **`/help`**, the server sends your client a snapshot of commands you may use (from Brigadier usage + permission gates), and the client opens a **large, Minecraft-styled UI**—search box, clickable rows that run each command via the normal **`sendUnattendedCommand`** path, and scrolling.

Upstream home for packaging and releases: [Otterdays/MC-Helpy-Helper](https://github.com/Otterdays/MC-Helpy-Helper).

## Requirements

- Minecraft **26.1.2**, Fabric Loader pinned in [`gradle.properties`](gradle.properties).
- [**Fabric API**](https://modrinth.com/mod/fabric-api) on both **client** and **server** (the mod declares `fabric-api`).
- Java **25+** per `fabric.mod.json`.

## Behavior (MVP)

- **`/help`** (logical server command): if the issuer is a **player**, the server aggregates usable command paths for that source’s permissions and sends them to **that player’s client** in an S2C payload; the client opens `Help Helper` screen listing `/…` strings.
- **Search** filters rows (substring, case-insensitive).
- **Click** a row: runs the slash command (`Commands.trimOptionalPrefix`), then closes the screen.
- **Scroll** wheel over the list area scrolls the list.

Non-players requesting `/help` get a chat error (GUI is intentionally player-focused).

### Limitations / expectations

- The mod must load on **the server your client is executing commands against**. Vanilla servers will not react to `/help` with this GUI.
- Dedicated servers hosting only this mod jar will advertise the custom payload Fabric networking expects clients to understand; vanilla clients connecting to those servers are unsupported (use a Fabric client with Help Helper/Fabric networking stack).

## Building

```powershell
.\gradlew.bat clean build -q
```

Artifacts land under **`BUILT/libs/`** (see [`build.gradle`](build.gradle): `layout.buildDirectory`).

Developers can use `.\gradlew.bat runClient` / `runServer` once [Fabric runs](https://docs.fabricmc.net/develop/getting-started/setup) are wired as usual—this repo keeps Fabric Loom pinned for 26.1.2.

## Project layout

- Main entry + payload registration + `/help`: `src/main/java/com/otterdays/helphelper/`
- Payload type: [`OpenHelpPayload`](src/main/java/com/otterdays/helphelper/network/OpenHelpPayload.java)
- Screen + receiver: `src/client/java/com/otterdays/helphelper/client/`
- Metadata: [`src/main/resources/fabric.mod.json`](src/main/resources/fabric.mod.json)

## License

See [LICENSE](LICENSE) (currently All Rights Reserved in template metadata—you may publish your own SPDX ID when releasing).
