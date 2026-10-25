# Development

Build with JDK 21 and Android SDK Platform 37. The app compiles against API 37
because AndroidX Core 1.19.1 requires it; it targets API 37 and its minimum SDK
remains 33. The wrapper uses Gradle 9.8.2.

Set `ANDROID_HOME` to the local Android SDK location, or create an ignored
`local.properties` with `sdk.dir` pointing to it. CI uses the runner's SDK
environment; machine-specific SDK paths must not be committed. Gradle can install missing SDK packages
when the SDK licenses have been accepted.

Build, run local unit tests, and check lint with the Gradle wrapper:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The Android Test workflow also runs instrumented tests on an API 34 emulator.
Run `./gradlew :app:connectedDebugAndroidTest` on an API 37 device or emulator
when validating changes to the target SDK. App lint should have no warnings or
errors.

App and widget picker lists use `ListAdapter` with immutable snapshots. Item
identity includes both the component and user profile; changes to displayed
content must be included in the diff comparison. Updating installed apps keeps
the current search query active.
