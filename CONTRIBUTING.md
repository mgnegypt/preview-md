# Contributing

- One task per commit, with Conventional Commits messages (`feat:`, `fix:`,
  `chore:`, `docs:`, `ci:`).
- Every user-facing string lives in `strings.xml` (English in `values/`,
  Arabic in `values-ar/`). No hardcoded strings in Composables.
- Layout uses logical directions (`start`/`end`), never `left`/`right`.
- Only the libraries listed in `docs/VERSIONS.md` are used. A new library
  needs the project owner's approval first.
- `main` must never be left with a failing CI run. After pushing, check the
  run result and its annotations.
