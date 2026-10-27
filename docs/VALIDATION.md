# Local validation — 0.3.0-beta.2

Completed:
- 8 JVM protocol/session tests, zero failures.
- Debug builds for phone and watch.
- Release builds using the existing development certificate to exercise configurable signing.
- Android Lint for debug and release, zero errors (non-blocking warnings remain).
- Rotary instrumentation test compiled into an Android test APK.
- YAML parsing and shell syntax validation of workflow scripts.
- Source archive contents, Gradle checksum, wrapper executable bit, Git exclusions, APK versions and signatures checked.

Not completed:
- Running rotary instrumentation on a working emulator.
- Physical hardware validation of this beta.
- GitHub-hosted CI execution.
- Dedicated production release-key setup or public release publication.

The personal debug-signed APKs are provided for manual evaluation and compatible updates of earlier local builds. For public release signing and workflow setup, see RELEASING.md.
