# Contributing

Use JDK 17, Android SDK 35, and the checked-in Gradle Wrapper. Keep phone and watch protocol changes compatible within one release, and update both APKs together.

Place user-facing copy in Android string/plural resources. Keep text input in memory, avoid logging content, and preserve session/peer validation and explicit password opt-in.

Run JVM tests, Android Lint, and both APK builds. Compile and run instrumentation tests on a disposable emulator when changing rotary behavior. Never automatically install on or test against an end user's physical device.

Use version.properties as the single source of the release version. Document user-visible changes in CHANGELOG.md. Do not commit local SDK paths, credentials, keystores, IDE files, cached tools, APKs, or generated output.
