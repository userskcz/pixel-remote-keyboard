# Changelog

## 0.3.0-beta.2

- Add an in-app license viewer on phone and watch.
- Bundle the project MIT license and release dependency notices using the official Google OSS licenses plugin.
- Prepare the private userskcz/pixel-remote-keyboard repository and downloadable APK assets.

## 0.3.0-beta.1

- Add native Wear OS crown scrolling to the setup screen and input-method panel.
- Keep editor focus when routing crown events, preserve fractional movement, and show a scroll-position arc.
- Use platform scroll feedback on Android 15+ where supported.
- Translate the UI, accessibility labels, notifications, and documentation to English.
- Move interface copy into Android string/plural resources.
- Replace text-dependent status parsing with an enum.
- Restore setup actions if the keyboard is no longer selected.
- Replace the prototype's adapted payload with the project's own versioned PRK1 transport. Both APKs must be updated.
- Add portable Gradle Wrapper with distribution checksum, centralized versioning, configurable release signing, GitHub CI, draft-release workflow, privacy/security docs, and source packaging exclusions.
- Add packet-validation tests and emulator-only rotary instrumentation coverage.

This is a prerelease. Local build/lint/JVM success does not establish hardware verification or a successful GitHub-hosted workflow run.

## 0.2.0

Redesign the phone editor and watch controls. Add clipboard paste, clear action, character count, connection states, and a compact settings menu.

## 0.1.0

Initial personal phone-to-watch keyboard prototype. User-confirmed text entry on Pixel 7 and Pixel Watch 3.
