# Preview MD — Verified versions (Phase 0)

> Verified on 2026-11-19 from the official sources named in each row.
> Rule: latest **stable** only. A version counts as verified for this project
> once the CI build is green with the full set (see the CI workflow).


Method: Google Maven and Maven Central `maven-metadata.xml`, the GitHub releases API, developer.android.com, kotlinlang.org, services.gradle.org and docs.github.com. Where a metadata `latest` field pointed at a pre-release, the newest stable below it was taken and the pre-release is named in the row.

## Toolchain and platform

| Item | Plan baseline | Verified latest stable | Source |
|---|---|---|---|
| Android platform | latest stable | Android 17, API 37 (stable 2026-06-16); minor SDK 37.1 exists and is required by Compose BOM 2026.11.01 | developer.android.com |
| AGP (com.android.tools.build:gradle) | 8.11.x | 9.5.1 (metadata latest field is 9.6.0-alpha04, excluded). AGP 9.5: Gradle min 9.6.0, JDK 17, max API 37, Build Tools 36.0.0 default | dl.google.com maven-metadata + AGP 9.5 release notes |
| Gradle wrapper | 8.14.x | Current release 9.9.1 (final). AGP 9.5 requires >= 9.6.0 and defaults to 9.6.0. Candidate: 9.9.1; if CI proves an AGP/Gradle incompatibility, the documented compatible fallback is 9.6.0 (AGP's own default), chosen for compatibility, not to silence a warning | services.gradle.org/versions/current |
| JDK | 17 | 17 (temurin via setup-java) | AGP 9.5 release notes |
| Kotlin | 2.2.x | 2.4.21 (2026-10-08). 2.5.0-RC excluded (not stable) | github.com/JetBrains/kotlin/releases + kotlinlang.org |
| Compose compiler plugin | with Kotlin | org.jetbrains.kotlin.plugin.compose at the Kotlin version (2.4.21), subject to AGP 9 built-in-Kotlin wiring (see impact note) | kotlinlang.org / AGP 9 docs |
| KSP | 2.2.x-2.0.x coupled | 2.3.14 (2026-11-13). Since 2.3.0 KSP versions are standalone. Official KSP overview: the 2.3.x line supports KGP 2.3.20-2.4.x, AGP 8.13.0+, Gradle 9.x for AGP 9, JDK 17+ | github.com/google/ksp/releases + kotlinlang.org/docs/ksp-overview.html |

## Libraries (exact coordinates, verified)

| Purpose | Coordinate | Verified stable | Source |
|---|---|---|---|
| Compose BOM | androidx.compose:compose-bom | 2026.11.01 (Compose 1.13, Material 3 1.5.0, published 2026-11-18; requires Kotlin 2.2+ and compileSdk 37.1). Verified fallback if 37.1 tooling blocks CI: 2026.08.00 (Compose 1.12, compileSdk 37, AGP >= 9.1.2) | dl.google.com maven-metadata |
| Activity Compose | androidx.activity:activity-compose | 1.13.0 (metadata latest is 1.14.0-alpha03, excluded) | dl.google.com |
| AppCompat | androidx.appcompat:appcompat | 1.8.0 | dl.google.com |
| Core KTX | androidx.core:core-ktx | 1.19.1 (metadata latest is 1.20.0-alpha01, excluded). Note: 1.19.x already demanded compileSdk 37 + AGP 9.1+ in the wild, consistent with this toolchain | dl.google.com |
| Lifecycle | androidx.lifecycle:lifecycle-viewmodel-compose, lifecycle-runtime-compose | 2.11.1 (metadata latest is 2.12.0-alpha06, excluded) | dl.google.com |
| Navigation | androidx.navigation:navigation-compose | 2.10.2 | dl.google.com |
| Room | androidx.room:room-runtime, room-ktx, room-compiler (KSP) | 2.8.5 | dl.google.com |
| DataStore | androidx.datastore:datastore-preferences | 1.3.0 | dl.google.com |
| DocumentFile | androidx.documentfile:documentfile | 1.1.0 | dl.google.com |
| Markdown renderer | com.mikepenz:multiplatform-markdown-renderer-android, -m3, -coil3, -code | 0.46.0 (2026-10-13) — all four modules at the same version | repo1.maven.org + github.com/mikepenz releases |
| Code highlighting | dev.snipme:highlights | 1.2.0 (2026-11-03). Note: the GitHub org is SnipMeDev (the plan text says SnipMeow — the Maven metadata and the releases page settle it as dev.snipme / SnipMeDev) | repo1.maven.org + github.com/SnipMeDev/Highlights |
| Images | io.coil-kt.coil3:coil-compose, coil-network-okhttp, coil-svg | 3.6.3 (2026-09-18) | repo1.maven.org + github.com/coil-kt/coil |
| HTTP | com.squareup.okhttp3:okhttp | 5.5.1. Plan rule: align with the version Coil pulls transitively — Phase 0 must print the resolved dependency tree in CI/local notes and, if Coil 3.6.3 resolves a different OkHttp, record the resolved version here instead of forcing 5.5.1 | repo1.maven.org |
| JSON | org.jetbrains.kotlinx:kotlinx-serialization-json | 1.13.0 | repo1.maven.org |
| Coroutines | org.jetbrains.kotlinx:kotlinx-coroutines-android (+ coroutines-test for tests) | 1.11.0 | repo1.maven.org |
| Tests | junit:junit | 4.13.2 | repo1.maven.org |
| Tests | io.mockk:mockk | 1.14.12 | repo1.maven.org |
| Tests | app.cash.turbine:turbine | 1.2.1 | repo1.maven.org |
| Tests | Compose UI test (ui-test-junit4 via BOM) | via BOM 2026.11.01 | dl.google.com |

## GitHub Actions and runner (verified from each repo's releases)

| Action | Plan skeleton | Verified latest | Use |
|---|---|---|---|
| Runner | ubuntu-latest | ubuntu-26.04 GA (2026-09-17); ubuntu-latest migration to 26.04 completes 2026-11-19. Pin the explicit label ubuntu-26.04 | actions/runner-images |
| actions/checkout | v4 | v7.0.1 (2026-07-20) | actions/checkout@v7 |
| actions/setup-java | v4 | v6.3.0 (2026-11-14) | actions/setup-java@v6 |
| gradle/actions/setup-gradle | v4 | v6.4.1 (2026-10-28) | gradle/actions/setup-gradle@v6 |
| actions/upload-artifact | v4 | v7.0.2 (2026-10-07) | actions/upload-artifact@v7 |

## Platform decisions recorded for Phase 0 (to prove in CI, not assumed)
- compileSdk: 37.1 if BOM 2026.11.01 is adopted, else 37. targetSdk: candidate 37; Google's own nowinandroid currently pairs compileSdk 37 with targetSdk 36 on AGP 9.3 — the Phase 0/1 work must pick deliberately and record the reason. Targeting 37 removes the large-screen orientation/resizability opt-out (sw > 600dp); Phase 7 covers the tablet check either way. minSdk stays 26 per plan.
- AGP 9 impact (from official release notes and KSP docs): built-in Kotlin changes plugin wiring (the standalone kotlin-android plugin is not applied the old way), kotlinOptions block is removed, KSP 2.3.x carries the AGP 9 built-in-Kotlin fixes. Build files must be written to the AGP 9 shape from the start — never the AGP 8 shape with flags on top.
- GitHub REST API version for Phase 6: newest is 2026-03-10; 2022-11-28 supported until 2028-03-10 (docs.github.com). Phase 6 reviews breaking changes for the releases endpoints before choosing.

## Decisions taken with these versions
- applicationId: `com.mgnegypt.previewmd` (decided by the project owner on 2026-11-19; permanent from the first public release).
- The version catalog is `gradle/libs.versions.toml`; this file and the catalog must always agree.
