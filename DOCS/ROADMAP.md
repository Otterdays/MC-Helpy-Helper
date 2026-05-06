<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# Help Helper UI Roadmap

## 2026-05-06 - Initial UI Edge Cases + QoL Plan

### Phase 1 - Quick Wins (Ship First)

- [ ] Single-click selects, Enter/double-click executes command
  - **Why:** prevent accidental command execution during browsing.
  - **Done when:** row click only sets selection; `Enter` and double-click execute selected command.
- [ ] Separate hover highlight from selected row state
  - **Why:** preserve keyboard navigation intent even when mouse moves.
  - **Done when:** moving cursor over rows no longer mutates selected row.
- [ ] Add empty-state recovery actions
  - **Why:** zero-result state should have one-click recovery.
  - **Done when:** empty results show `Clear Search` and `Reset Filters` actions.
- [ ] Add action feedback (copy/fill)
  - **Why:** clipboard and fill actions are currently subtle.
  - **Done when:** lightweight confirmation text/overlay appears after action.
- [ ] Make category overflow accessible on small screens
  - **Why:** categories can be truncated when vertical space is tight.
  - **Done when:** every category remains reachable on low resolutions.

### Phase 2 - Safety + Discoverability

- [ ] Add risky-command safety gate
  - **Why:** reduce irreversible user mistakes.
  - **Done when:** risky run actions require explicit user confirmation or modifier key.
- [ ] Add tooltips for quick filters
  - **Why:** short labels (`Fav`, `Safe`, `Modded`) are fast but ambiguous.
  - **Done when:** each filter shows exact matching behavior on hover.
- [ ] Add keyboard shortcut overlay (`?`)
  - **Why:** discoverability for non-obvious shortcuts.
  - **Done when:** overlay lists full key map and action mode behavior.
- [ ] Add full-text preview for truncated templates
  - **Why:** ellipsized templates hide command intent.
  - **Done when:** hover reveals full template text.
- [ ] Surface persistence errors
  - **Why:** state save/load failures are currently silent.
  - **Done when:** user receives non-spammy warning when state read/write fails.

### Phase 3 - Power UX

- [ ] Add fuzzy search scoring
  - **Why:** robust matching for typos/partial recall.
  - **Done when:** ranked results recover intended commands for near matches.
- [ ] Make recents cap configurable
  - **Why:** fixed recents depth may not fit all play styles.
  - **Done when:** recents max size can be configured and persists.
- [ ] Add details panel pinning
  - **Why:** compare list items while keeping one command pinned in details.
  - **Done when:** details panel can lock/unlock selected command.
- [ ] Show better origin hints for unknown commands
  - **Why:** "Server/Modded" is broad for large modpacks.
  - **Done when:** source hints include namespace/root where available.

### QA Checklist (Run Each Milestone)

- [ ] Test low resolution and full screen layouts.
- [ ] Test keyboard-only flow end-to-end.
- [ ] Test empty, short, and large command result sets.
- [ ] Test risky command behavior for Run/Copy/Fill action modes.
- [ ] Restart client and verify persisted UI state.

### Notes

- This roadmap is derived from current behavior in `HelpHelperScreen` and `CommandCatalog`.
- Prioritize reversible, low-risk UX improvements first.
