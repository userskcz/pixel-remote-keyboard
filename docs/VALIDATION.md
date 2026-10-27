# Local validation — 0.3.0-beta.2

Completed:
- 8 JVM protocol/session tests, zero failures.
- Debug builds for phone and watch.
- Release builds using the existing development certificate to exercise configurable signing.
- Android Lint for debug and release, zero errors (non-blocking warnings remain).
- Rotary instrumentation test compiled into an Android test APK.
- YAML parsing and shell syntax validation of workflow scripts.
- Source archive contents, Gradle checksum, wrapper executable bit, Git exclusions, APK versions and signatures checked.

GitHub validation completed on 2026-10-27:
- Build, JVM tests, and Android Lint passed.
- One rotary instrumentation test executed successfully on an Android 15 disposable emulator.
- Run: https://github.com/userskcz/pixel-remote-keyboard/actions/runs/40479297012
- No physical user watch was used.

Not completed:
- Physical hardware validation of this beta.
- Dedicated production release-key setup or public release publication.

The personal debug-signed APKs are provided for manual evaluation and compatible updates of earlier local builds. For public release signing and workflow setup, see RELEASING.md.
