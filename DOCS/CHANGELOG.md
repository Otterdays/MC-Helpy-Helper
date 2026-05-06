<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Changed

- Updated `HelpHelperScreen` top control layout to use font-aware control height and adaptive top spacing, fixing clipped/cut-off controls on larger UI scales.
- Hardened `HelpHelperScreen` for edge cases by scaling top-row control widths on narrow layouts and reserving more vertical space for top controls/filters.
- Refined `HelpHelperScreen` header styling to be more compact/professional by tightening header height, vertical gaps, and strip/text placement while keeping controls visible.

### Added

- Added `DOCS/ROADMAP.md` with phased UI edge-case and QoL checklist.
- Added baseline status docs: `SUMMARY.md`, `SCRATCHPAD.md`, `SBOM.md`, `STYLE_GUIDE.md`.
