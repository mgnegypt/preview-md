# Changelog

All notable changes to Preview MD. Versions follow the tag format
`vMAJOR.MINOR.PATCH[-beta.N]`; the newest entry is at the top.

## [1.0.3] - 2026-11-19

- Fixed: the Check for updates button disappeared after an update was
  installed — the installed state showed its status text without the
  button. The button now stays available, as it does after an
  up-to-date check.
- Fixed: the unselected release-channel label (Beta while Stable is
  selected, and the reverse) was invisible in dark mode: cream text on
  the cream track. Channel labels are now ink in both themes.
  Both fixes were reported by the developer from on-device use.

## [1.0.2] - 2026-11-19

- Update-path verification release: no user-facing changes. Published
  to prove the in-app updater end to end on a real device (detect,
  download, checksum verification, install over 1.0.1), after a beta
  with a deliberately wrong checksum was correctly rejected.

## [1.0.1] - 2026-11-19

- Fixed: the bottom navigation bar was missing from the 1.0.0 release
  build. The shell compared the current route against class-name
  strings, which R8 renames in release builds, so the comparison never
  matched; route checks now use NavDestination.hasRoute. (Debug builds
  were unaffected, which is why CI never saw it.)

## [1.0.0] - 2026-11-19

First release.

- GitHub-flavoured Markdown preview: headings, highlighted code blocks,
  task lists with drawn checkboxes, tables, images (including images
  stored next to the file), links between local Markdown files.
- Editor with line numbers that stay in sync on wrapped lines, syntax
  highlighting, a Markdown toolbar, autosaved drafts, verified saves
  and a warning when the file changed outside the app.
- Home with recent files and browsable folders through the system file
  picker (no storage permissions), search, and full Arabic RTL.
- Settings: light/dark/system themes, English/Arabic per-app language,
  and self-updates from GitHub Releases with SHA-256 verification
  (Stable and Beta channels).
- About screen with the developer's photo and social links.
