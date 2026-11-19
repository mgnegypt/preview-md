# Preview MD

A native Android Markdown reader and editor that renders Markdown like
GitHub does. Open any `.md` file, see it rendered, and edit it in place.

- GitHub-flavoured Markdown preview (headings, code blocks with syntax
  highlighting, task lists, tables, images)
- In-place editor with line numbers and a Markdown toolbar
- Light, dark and system themes; English and Arabic with full RTL
- Self-updates from GitHub Releases (no Google Play)

## Status

Current release: see the GitHub Releases page. The build is produced
entirely by GitHub Actions; pushing to `main` runs the CI workflow.

## Updates

The app updates itself from GitHub Releases: Settings > Updates can
check manually, and with automatic updates on it checks at most once a
day. Downloads are verified against the release's SHA-256 checksum
before anything is installed, and Android always shows its own install
confirmation. The Stable channel follows regular releases; the Beta
channel also sees prereleases.

## Installing

Releases are published on the GitHub Releases page as
`preview-md-<version>.apk` together with a `.sha256` checksum file.
Android will ask you to allow installs from your browser or file manager
the first time; that is expected for apps distributed outside Google Play.

## Building

Nothing is built on a developer machine. Pushing to `main` runs lint,
unit tests and a debug APK build in CI. Pushing a `v*` tag produces the
signed release. See `docs/VERSIONS.md` for the verified toolchain and
library versions, and `docs/RELEASING.md` for the release procedure,
including the signing-key warning.

## License

MIT. See `LICENSE`.
