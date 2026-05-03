<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# FPS Mod — Feature doc

Client-only Fabric mod (`fpsmod`): **FPS HUD** + **persisted toggle**, minimal CPU use.

---

## Summary

| Area | Behavior |
|------|----------|
| **FPS readout** | Top-left HUD text (`FPS: N`), color ~light green + shadow |
| **Refresh rate** | At most **once per second** (client tick + 1000 ms throttle; uses Minecraft’s FPS value) |
| **Visibility toggle** | **Hide FPS** / **Show FPS** button on screens (inventory, pause, chat, etc.) |
| **When it runs** | World + player present only; respects **Hide GUI (F1)** |
| **Persistence** | `config/fpsmod/hud.properties` — key `showFpsHud` |
| **HUD ordering** | `HudElementRegistry` layer **before** `MISC_OVERLAYS` |

---

## User-facing behavior

1. **In world** — FPS line appears top-left unless hidden via toggle or F1.
2. **Toggle** — Open a GUI screen **in a world**; top-left **70×20** button flips HUD and relabels. No title-menu button (no level/player).
3. **Startup** — Common mod init logs (`FpsMod`); toggle changes log with `🔁` prefix.

---

## Configuration

| File | Purpose |
|------|---------|
| `config/fpsmod/hud.properties` | `showFpsHud=true|false` |

Read failures → default **show** (`true`). Write failures logged; in-memory toggle still applies for the session.

---

## Composition (for extenders)

- **Entry:** `FpsMod` (main), `FpsModClient` → registers `FpsHudOverlay`, `FpsHudScreenButton`.
- **HUD:** `FpsHudOverlay` — sampling on `ClientTickEvents.END_CLIENT_TICK`, render via `HudElementRegistry`.
- **UI:** `FpsHudScreenButton` — `ScreenEvents.AFTER_INIT`, `Screens.getWidgets(screen).add(...)`.
- **Config:** `FpsHudConfig` — Properties I/O under config dir.

---

## Explicit non-features (current scope)

- No keybinding for toggle (screen button only).
- No frame-by-frame FPS; not a profiler.
- No dedicated server / datapack logic — **`environment`: `client`** in `fabric.mod.json`.
