# Repository Guidelines

## Project Structure & Module Organization

Legado is an Android ebook reader with an embedded web interface.

- `app/src/main/java/io/legado/app/`: Kotlin/Java application code, organized into `ui`, `data`, `model`, `service`, and API packages.
- `app/src/main/res/` and `assets/`: Android resources, bundled content, and web assets.
- `app/src/test/` and `app/src/androidTest/`: host unit tests and device instrumentation tests. `app/schemas/` stores Room database schemas.
- `modules/book/`: ebook parsing library; `modules/rhino/`: JavaScript engine integration.
- `modules/web/src/`: Vue 3/TypeScript bookshelf and source editor. Edit these sources; GitHub Actions synchronizes builds into `app/src/main/assets/web/vue/`.

## Build, Test, and Development Commands

Use JDK 17, Android SDK 36, and the bundled Gradle wrapper from the repository root:

- `./gradlew :app:assembleAppDebug`: build a debug APK.
- `./gradlew :app:installAppDebug`: install on a connected device or emulator; launch the app there.
- `./gradlew :app:testAppDebugUnitTest`: run host unit tests.
- `./gradlew :app:connectedAppDebugAndroidTest`: run instrumentation tests on a device or emulator.
- `./gradlew :app:lintAppDebug`: run Android Lint.

Inside `modules/web/`, use Node.js >=20 and pnpm >=9:

- `pnpm install`: install dependencies.
- `pnpm dev`: start Vite at `http://localhost:8080`; enable the Android app's web service for backend access.
- `pnpm build`: type-check and build `dist/`; asset synchronization runs only in GitHub Actions.

## Coding Style & Naming Conventions

Follow Kotlin's official style with four-space indentation for Android code. Use `PascalCase` types, `camelCase` members, and `snake_case` Android resources. Match surrounding Java/XML conventions.

Web code uses two spaces, single quotes, and no semicolons. Run `pnpm lint:fix` (ESLint) and `pnpm format` (Prettier) within `modules/web/`; keep formatting changes scoped.

## Testing Guidelines

Use JUnit 4 for host tests and AndroidX/Espresso for instrumentation. Name classes `<Feature>Test` and annotate test methods with `@Test`. Add regression coverage for behavior changes; database changes should update schemas and migration tests. No coverage threshold is configured. Web changes require type-checking and manual verification of affected flows; no web test runner is configured.

## Commit & Pull Request Guidelines

History uses brief Chinese/English summaries, issue references such as `优化 #5784`, and automated `Bump web ...` commits. Write specific summaries and reference related issues. Commitizen supports Conventional Commits, but history does not enforce them.

PRs should explain the problem and resulting behavior, link issues, list validation commands/results, and include screenshots for UI changes. Keep SDK paths in ignored `local.properties` and personal signing credentials outside tracked files.
