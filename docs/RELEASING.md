# Releasing Preview MD

Releases are distributed ONLY through GitHub Releases. There is no
Google Play listing and no other channel.

## The signing key (read this first)

Every release is signed with the same keystore, forever. Android only
installs an update over an existing app when the signature matches, so
**losing this keystore means updates become impossible** and every user
would have to uninstall and reinstall, losing nothing but convenience
(the app keeps its data in the user's own files, but the app identity
would change).

- Alias: `previewmd`
- Certificate SHA-256:
  `4B:AF:7E:FF:3F:EC:C1:9E:E4:41:ED:27:DF:25:74:9B:40:6B:F2:B7:A2:90:26:DF:38:82:80:1A:55:96:E7:92`
- Created 2026-11-19, valid 10,000 days.
- The keystore file and its password live OUTSIDE this repository with
  the owner, backed up in two places. Never commit them, never paste
  them into issues, chats or logs.
- CI receives the key through four repository secrets:
  `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## Cutting a release

1. Make sure `main` is green and the change is worth a release.
2. Update `CHANGELOG.md`.
3. Tag and push: `git tag v1.2.3 && git push origin v1.2.3`
   (beta: `v1.2.3-beta.1`).
4. The Release workflow validates the tag, computes the versionCode
   (`MAJOR*1_000_000 + MINOR*10_000 + PATCH*100 + PRE`, PRE = 99 for
   stable, N for beta.N), builds the signed APK, verifies the signature
   with apksigner, and publishes `preview-md-<version>.apk` plus
   `preview-md-<version>.apk.sha256` to a new GitHub release
   (marked prerelease for beta tags).
5. Verify on the Releases page that both assets are attached and the
   version reads correctly, then install it on a real device.

## How the in-app updater consumes a release

- Stable channel: the latest non-prerelease release.
- Beta channel: the newest release including prereleases.
- The APK asset is found by the exact pattern `preview-md-<version>.apk`
  and its checksum by `<apk name>.sha256`; the download is installed
  only when its SHA-256 matches the checksum file. A mismatch deletes
  the download and shows an error. The system always shows its own
  install confirmation; there are no silent installs.

## Proving a corrupted download is rejected

Publish a beta whose `.sha256` asset deliberately contains a wrong
hash, switch the app to the Beta channel, and check for updates: the
download must be rejected with the checksum error and nothing is
installed. Replace it with the real release afterwards.
