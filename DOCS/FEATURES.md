<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->
<!-- PRESERVATION RULE: Keep documentation current. Replace stale details when needed. -->

# Help Helper - Feature Documentation

[AMENDED 2026-05-06]:
- Added UI roadmap document: `DOCS/ROADMAP.md`.
- Roadmap includes 3 phases with 14 checklist items focused on edge cases, UX safety, and QoL.
- Phase 1 targets accidental execution prevention, hover/selection separation, empty-state recovery, action feedback, and category overflow handling.
- This amendment is append-only and does not remove prior feature documentation.

Fabric mod (`helphelper`): searchable command browser GUI plus clickable command execution, minimal server load.

---

## Summary

| Area | Behavior |
|------|----------|
| **Command browser** | Full-window Minecraft-styled GUI replacing `/help` text output |
| **Search** | Case-insensitive filtering across command text, category, aliases, and description |
| **Click actions** | Run command, Copy to clipboard, Fill chat box |
| **Categories** | Top-level command grouping with filter buttons and preferred vanilla ordering |
| **Details panel** | Shows selected command title, command line, category, vanilla/risky label, aliases, and templates |
| **Navigation** | Mouse wheel scroll, scrollbar drag, track paging, arrow keys, Page Up/Down, Home/End |
| **Display modes** | Compact/Roomy row density toggle |
| **Favorites** | Star favorite commands (F key); shows in All scope but boosted in ranking |
| **Recent commands** | Last 12 executed commands tracked; higher in sort order |
| **Quick filters** | Filter by All, Favorites, Recent, Vanilla-only, Modded-only, Safe (non-risky), or Risky commands |
| **Sort modes** | Sort by Top (relevance + recency + favorites), A-Z (alphabetical), Recent (execution order), Vanilla-first |
| **UI persistence** | Favorites, recents, active filter, sort mode, search text, and display density saved to `config/helphelper/ui.json` |
| **Networking** | Server-to-client payload via Fabric networking |

---

## User-facing Behavior

1. **`/help` command** - Server collects usable commands for player's permission level, sends via S2C payload, client opens GUI.
2. **Search** - Type in search box to filter commands.
3. **Click command row** - Executes selected action.
4. **Categories** - Click category buttons to filter by top-level command family.
5. **Keyboard shortcuts**:
   - `Arrow Up/Down` - Navigate selection
   - `Page Up/Down` - Scroll by page
   - `Home/End` - Jump to top/bottom
   - `Enter` (or NumpadEnter) - Execute selected command
   - `C` - Cycle click action mode (Run → Copy → Fill Chat)
   - `D` - Toggle compact/roomy display density
   - `F` - Toggle favorite status of selected command

---

## Command Collection

- Uses Brigadier `CommandDispatcher` tree traversal.
- Respects permission gates via `CommandNode.canUse(source)`.
- Returns sorted list of `/`-prefixed command paths.
- Collected server-side per player permission level.

---

## GUI Components

| Component | Description |
|-----------|-------------|
| **Search box** | EditBox with hint text `Search commands...` |
| **Action button** | Toggles between Run/Copy/Fill modes |
| **Density button** | Toggles Compact/Roomy row spacing |
| **Done button** | Closes GUI (`gui.done` translation) |
| **Category buttons** | Filters by top-level command family |
| **Command rows** | Clickable with hover, selection highlight, and small vanilla/risky badge |
| **Details panel** | Shows command metadata and clickable templates |
| **Scrollbar** | Custom-drawn with drag and paging support |

---

## Vanilla Command Catalog

The client contains a curated catalog for common vanilla commands. It adds:

- Friendly titles and short descriptions.
- Known aliases like `tp`, `xp`, `tell`, `w`, `tm`, `deop`, and `ban-ip`.
- Risk labeling for destructive or high-impact commands.
- Template suggestions for quick editing.

Unknown commands still render with a generic "Server/Modded" entry.

---

## Networking

| Payload | Direction | Content |
|---------|-----------|---------|
| `OpenHelpPayload` | Server -> Client | `List<String>` of usable commands |

- Payload type: `helphelper:open_help`
- Uses `StreamCodec` for serialization
- Client receives and opens `HelpHelperScreen`

---

## Configuration

Currently no config file. Future considerations:

- Default click action preference
- Default display density
- Search debounce delay
- Favorite/bookmark commands
- Vanilla metadata expansion

---

## Composition (for extenders)

- **Entry:** `HelpHelper` (main), `HelpHelperClient` -> registers payload type, `/help` command, network receiver.
- **Commands:** `HelpHelperCommands` - `/help` registration via `CommandRegistrationCallback`.
- **Support:** `HelpCommandsSupport` - Brigadier tree traversal, permission-aware.
- **Network:** `OpenHelpPayload` - S2C payload record with `CustomPacketPayload`.
- **GUI:** `HelpHelperScreen` - Full-screen browser with search, categories, details panel, and scrolling.
- **Catalog:** `CommandCatalog` - Client-side vanilla command metadata and presets.

---

## Known Limitations

- Mod must be on **both client and server**.
- Vanilla servers will not show the GUI.
- Command metadata is curated, not exhaustive.
- No full Brigadier syntax preview for every command.
- Single payload sends all commands at once.

---

## Planned Enhancements (Future)

- Expand vanilla command metadata coverage.
- Group commands by mod/namespace.
- Favorite/bookmark commands.
- Recent commands history.
- Fuzzy search with scoring.
- Show required permission level per command.
- Config file for preferences.
- Keyboard shortcut to open help without typing `/help`.

---

## Explicit Non-features (Current Scope)

- No command execution confirmation dialogs.
- No server-side GUI rendering.
- No datapack integration.
- No permission management beyond Brigadier checks.
- No external config yet.
