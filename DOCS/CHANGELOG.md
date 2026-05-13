<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Changed

- Made the top toolbar height adapt to wrapped quick-filter, sort, and category rows so small GUI scales keep the list pane below the controls instead of overlapping or clipping them.
- Shifted the Help Helper content pane off the decorative left gutter so header counters, scope text, and top controls align with the visible panel on narrow/wide layouts.
- Updated `HelpHelperScreen` top control layout to use font-aware control height and adaptive top spacing, fixing clipped/cut-off controls on larger UI scales.
- Hardened `HelpHelperScreen` for edge cases by scaling top-row control widths on narrow layouts and reserving more vertical space for top controls/filters.
- Refined `HelpHelperScreen` header styling to be more compact/professional by tightening header height, vertical gaps, and strip/text placement while keeping controls visible.
- **Improved UI theming & full-screen compat**: Header gradient height now scales with `font.lineHeight`; counter/filter lines spaced dynamically to prevent overlap at any GUI scale.
- **Enhanced visual design**: Added zebra-striped row backgrounds (even/odd alternation), subtle row separator lines, L-shaped gold corner accents on list box border, left accent bar on selected rows, and category color bars.
- **Badge & detail panel refinements**: Badges now have outline borders matching their type color; detail panel includes top accent gradient, separator line after title/command block, and outlined preset buttons with hover states.
- **Scrollbar polish**: Added 1px inner margin on track and thumb for cleaner appearance.

### Added

- Added a simple `otterdays` text icon source (`icon.svg`) and launcher-friendly `icon.png` metadata path so Prism can display a project-specific mod icon.
- **Favorites system**: Press `F` to favorite/unfavorite selected command; favorites appear first in default sort.
- **Recent commands tracking**: Last 12 executed commands tracked and boosted in ranking.
- **Quick filter buttons**: Filter by All, Favorites, Recent, Vanilla, Modded, Safe, or Risky commands.
- **Sort modes**: Top (by relevance score), A-Z (alphabetical), Recent (by execution history), Vanilla (vanilla-first).
- **UI state persistence**: Search box, active filters, sort mode, favorites, and recents saved to `config/helphelper/ui.json` per session.
- **Magic key shortcuts on display**: Hint text now shows `C mode  D density  F favorite` so users discover keyboard control.
- **Named GLFW key constants**: Key codes (264-269, 257, 335, 67, 68, 70) now use semantic constants to avoid magic numbers.
- Added `DOCS/ROADMAP.md` with phased UI edge-case and QoL checklist.
- Added baseline status docs: `SUMMARY.md`, `SCRATCHPAD.md`, `SBOM.md`, `STYLE_GUIDE.md`.
