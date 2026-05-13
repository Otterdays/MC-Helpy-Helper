<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# SCRATCHPAD

## 2026-05-13 [AMENDED-3]

### Active Tasks

- Harden `HelpHelperScreen` and `HelpHelperConfigScreen` for narrow widths and larger GUI/font scales.

### Blockers

- No blocker; needs in-game validation on the smallest GUI scale and on the config/help/confirm overlays.

### Last 5 Actions

1. Reworked the primary search/action/density/done strip to wrap into extra rows when width is too small for the single-row layout.
2. Made command row badges use font-aware vertical insets instead of fixed badge box padding.
3. Made detail-panel template rows use dynamic heights derived from `minecraft.font.lineHeight`.
4. Scaled the config screen panel/buttons from panel width and font height rather than fixed 180/22-style sizing.
5. Made help/confirm overlay panel heights adapt to larger font sizes and recompiled successfully with `./gradlew.bat compileClientJava`.

### Next Steps

- User re-test the smallest GUI scale and config/help/confirm screens.
- If tiny widths still feel overloaded, add a true small-screen fallback that hides category chips behind a secondary toggle.

## 2026-05-13 [AMENDED-2]

### Active Tasks

- Harden `HelpHelperScreen` top toolbar for small GUI scales so wrapped filter/category rows do not collide with the list pane.

### Blockers

- No blocker; needs in-game verification on the smallest failing GUI scale.

### Last 5 Actions

1. Identified fixed `listTop` row budget as the reason wrapped top-bar controls were clipping/breaking on small GUI sizes.
2. Changed quick-filter, sort, and category button builders to return their actual wrapped bottom row.
3. Added dynamic list-top adjustment so the command pane starts below the real control stack instead of below a guessed height.
4. Added a control-bottom limit to preserve a minimum list viewport on cramped screens.
5. Recompiled successfully with `./gradlew.bat compileClientJava`.

### Next Steps

- User verify the smallest failing GUI scale/resolution.
- If categories still crowd the top area, collapse category chips into fewer rows or move them behind a secondary view.

## 2026-05-13 [AMENDED]

### Active Tasks

- Finish narrow-width UI pass for `HelpHelperScreen`, with focus on top-row alignment against the left decorative gutter.

### Blockers

- No blocker; latest pass needs in-game visual confirmation on the wide/short layout from user screenshot.

### Last 5 Actions

1. Reviewed the screenshot showing the left gutter offsetting the count/scope/action rows.
2. Traced the misalignment to layout math anchoring usable content at the outer frame edge.
3. Added a fixed decorative gutter offset in `HelpHelperLayoutMath` so content starts at the inner panel edge.
4. Recompiled with `./gradlew.bat compileClientJava`.
5. Confirmed the layout math change is lint-clean.

### Next Steps

- User verify the top header/count/search rows now line up cleanly.
- If the header still feels cramped, move only the text rows farther in without shrinking the full list pane.

## 2026-05-13

### Active Tasks

- Replace launcher-facing mod icon metadata with a simple `otterdays` text icon for Prism visibility.

### Blockers

- Prism Launcher icon parsing can be inconsistent for non-PNG assets, so a PNG fallback is required even if the source art is SVG.

### Last 5 Actions

1. Confirmed `fabric.mod.json` did not declare an icon field.
2. Verified Prism Launcher commonly reads Fabric icon metadata from `fabric.mod.json`.
3. Confirmed current bundled icon art was still template branding instead of project-specific art.
4. Added a simple two-line `otterdays` SVG source asset.
5. Updated mod metadata to point at a root-level `icon.png` for launcher compatibility.

### Next Steps

- Generate matching `icon.png` files in the jar root and `assets/helphelper/`.
- Rebuild resources and confirm Prism now shows the custom icon.

## 2026-05-06 [AMENDED-3]

### Active Tasks

- Finalize top-header polish in `HelpHelperScreen` while preserving visibility at high UI scale.

### Blockers

- No blocker; awaiting user visual sign-off after compact header pass.

### Last 5 Actions

1. Analyzed real build errors from terminal output and fixed malformed `CFG.*` declarations.
2. Repaired switch-case key bindings to compile-time constants and restored successful builds.
3. Addressed hidden search/control clipping with additional top layout reserve and overflow scaling.
4. Applied compact/professional header refinements (reduced header height, tighter row spacing, cleaner strip sizing).
5. Verified changes by rebuilding with `build.bat` (`BUILD SUCCESSFUL`).

### Next Steps

- User visual QA on target UI scale.
- If needed, apply fixed-height top panel fallback for guaranteed visibility.

## 2026-05-06 [AMENDED-2]

### Active Tasks

- Edge-case hardening for `HelpHelperScreen` top panel and control-row layout at narrow widths/high UI scale.

### Blockers

- No blocker; awaiting in-game visual confirmation for the latest layout reserve changes.

### Last 5 Actions

1. Fixed compile breaks introduced by malformed `CFG.*` constant declarations.
2. Repaired `keyPressed` switch labels to use compile-time `KEY_*` constants.
3. Increased top reserve rows to reduce clipping of control/filter rows.
4. Added width-scaling for right-side control buttons to prevent overflow on narrow screens.
5. Rebuilt successfully with `build.bat`.

### Next Steps

- Validate with the exact problematic UI scale/resolution.
- If clipping remains, enforce a fixed-height top control panel fallback.

## 2026-05-06 [AMENDED]

### Active Tasks

- Debug and stabilize top control row layout in `HelpHelperScreen` for high UI scale/font sizes.
- Verify no clipped text/buttons in search/action/density/done controls.

### Blockers

- No hard blocker; visual validation still needed in-game across multiple UI scales.

### Last 5 Actions

1. Reviewed current screen layout code and found fixed `controlHeight` usage.
2. Switched control sizing to font-aware `Math.max(CFG.controlHeight, font + padding)`.
3. Reworked list/header spacing to scale with control height instead of fixed top-space magic number.
4. Removed duplicate `ARGB` import in `HelpHelperScreen` while touching the file.
5. Prepared lint pass and doc checkpoint.

### Next Steps

- Run lint/compile check for touched file.
- Validate screen at larger UI scale where top row was clipping.
- Tune spacing constants only if clipping still appears in edge resolutions.

## 2026-05-06

### Active Tasks

- Create `DOCS/ROADMAP.md` from UI review findings.
- Align docs with preservation/header and status-doc workflow rules.

### Blockers

- Existing status docs (`SUMMARY.md`, `SBOM.md`, `STYLE_GUIDE.md`, `CHANGELOG.md`) were missing and had to be bootstrapped.

### Last 5 Actions

1. Audited existing docs and confirmed only `DOCS/FEATURES.md` existed.
2. Built prioritized UI roadmap/checklist from edge-case review.
3. Created `DOCS/ROADMAP.md` with phases + acceptance criteria.
4. Bootstrapped status docs required by project rules.
5. Appended roadmap pointer and rule-compliant amendment to `DOCS/FEATURES.md`.

### Next Steps

- Implement Phase 1 roadmap items in UI code.
- Validate UX behavior with low-res and keyboard-only tests.

### Out-of-Scope Observations

- None logged in this update.
