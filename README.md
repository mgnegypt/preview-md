# Preview MD

A native Android Markdown reader and editor that renders Markdown like
GitHub does. Open any `.md` file, see it rendered, and edit it in place.

- GitHub-flavoured Markdown preview (headings, code blocks with syntax
  highlighting, task lists, tables, images)
- In-place editor with line numbers and a Markdown toolbar
- Light, dark and system themes; English and Arabic with full RTL
- Self-updates from GitHub Releases (no Google Play)

## Status

Under construction, phase by phase. The build is produced entirely by
GitHub Actions; see the CI workflow for the current state.

## Installing

Releases are published on the GitHub Releases page as
`preview-md-<version>.apk` together with a `.sha256` checksum file.
Android will ask you to allow installs from your browser or file manager
the first time; that is expected for apps distributed outside Google Play.

## Building

Nothing is built on a developer machine. Pushing to `main` runs lint,
unit tests and a debug APK build in CI. Pushing a `v*` tag produces the
signed release. See `docs/VERSIONS.md` for the verified toolchain and
library versions, and `docs/RELEASING.md` (added with the release
pipeline) for the release procedure.

## License

MIT. See `LICENSE`.
