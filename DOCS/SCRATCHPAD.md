<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# SCRATCHPAD

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
