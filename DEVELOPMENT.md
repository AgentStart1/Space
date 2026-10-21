# Development

Build with JDK 21 and Android SDK Platform 37. The app compiles against API 37
because AndroidX Core 1.19.1 requires it; its target SDK remains 36 and minimum
SDK remains 33.

Set `sdk.dir` in `local.properties` to the local Android SDK location; do not
commit machine-specific path changes. Gradle can install missing SDK packages
when the SDK licenses have been accepted.

Build and run local unit tests with the Gradle wrapper:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

The Android Test workflow also runs instrumented tests on an API 34 emulator.
