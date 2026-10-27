# Pixel Remote Keyboard

Type on your Android phone. Insert the text into the focused field on your Wear OS watch.

Pixel Remote Keyboard pairs a full-size phone editor with a system keyboard on your watch. It supports Unicode text, emoji, clipboard paste, password-field opt-in, and rotary crown scrolling.

## Requirements

- Android 13 or newer on the phone and Wear OS based on Android 13 or newer on the watch.
- A paired Android phone and Wear OS watch with Google Play services.
- Both companion APKs from the same release, signed with the same certificate.

The original 0.1.0 prototype was confirmed working by its user on Pixel 7 + Pixel Watch 3. Version 0.3.0-beta.2 is a prerelease: full device testing of the new protocol and crown behavior is still needed. No physical device is used automatically by the build scripts.

## Install

1. Install **pixel-keyboard-phone.apk** on your phone.
2. Install **pixel-keyboard-watch.apk** on your watch. Installing the phone app does not install the watch app automatically. If necessary, use [wireless ADB](https://developer.android.com/training/wearables/get-started/debugging) once for installation.
3. Open Pixel Remote Keyboard on both devices.
4. On the watch, tap **Enable**, enable the keyboard in Android settings, then tap **Select** and choose Pixel Remote Keyboard.
5. On your phone, allow notifications when convenient. Notifications are optional; you can open the editor manually.
6. Tap the test field on the watch, type on your phone, and tap **Insert on watch**.

ADB is not used to type. You can turn debugging off after installation.

### Upgrading from 0.1.x or 0.2.x

Update **both APKs**. Version 0.3 uses a new application protocol and cannot exchange input with older versions. The application ID is unchanged. An in-place update requires the same signing certificate as your installed build; a separately built CI debug APK usually has a different certificate.

The private beta APKs are release-mode builds signed with the existing development certificate to preserve updates from earlier local versions. The signing key is not included in the repository. Public releases should use a dedicated, durable release key; see [RELEASING.md](RELEASING.md).

## Use

Open a normal text field on the watch and keep the keyboard visible. Type or explicitly paste from your phone clipboard, then tap **Insert on watch**. Text replaces the current selection or is inserted at the cursor. The app does not read the field's existing contents.

Insertion does not submit the form or send a message. **Done** on the watch performs the field's normal action, such as Search or Send. The keyboard icon opens the system keyboard picker so you can switch back to Gboard.

In password fields, first tap **Allow** on the watch. The phone editor masks the text and blocks screenshots. Your chosen phone keyboard still processes what you type.

Rotate the watch crown to scroll the keyboard panel or setup screen. Native rotary encoder events use the Android scroll factor; input focus stays in the current field. A scroll-position arc appears when content exceeds the viewport. Platform scroll haptics are enabled where supported on Android 15+.

## Open-source notices

Use **Open-source licenses** in the phone settings menu or on the watch setup screen. Release builds bundle the project MIT license and dependency notices collected by the official Google OSS licenses plugin. Debug builds may show a debug-information placeholder for dependency notices.

## Privacy

- No ads, analytics, accounts, text history, or application-operated servers.
- Input travels through Google Play services' Wearable Data Layer. This project does not guarantee a Bluetooth-only route: platform connectivity may use other available transports.
- Only request metadata is stored: peer/session identifiers, field label, password flag, and request time. Typed input is not persisted or logged.
- Clipboard contents are read only after you tap **Paste**.
- Backup and device transfer of application data are disabled.
- Draft text is intentionally not restored after process death.
- Session and operation identifiers reject stale replies and recognize duplicate deliveries.

See [PRIVACY.md](PRIVACY.md) and [SECURITY.md](SECURITY.md).

## Build

Install JDK 17 and Android SDK Platform 35. Point Android Studio, ANDROID_HOME, or an untracked local.properties at your SDK.

On Linux or macOS:

    ./gradlew :protocol:test :phone:lintDebug :watch:lintDebug :phone:assembleDebug :watch:assembleDebug :watch:assembleDebugAndroidTest

On Windows, use gradlew.bat with the same tasks. The optional build.ps1 helper defaults its generated storage to D:\Codex and accepts -StorageRoot to change it.

Debug APKs are produced under each module's build/outputs/apk/debug directory. Both modules use the same application ID and debug keystore. Never publish a private signing key or commit local.properties.

The checked-in Gradle Wrapper verifies the Gradle distribution checksum.

## Tests

JVM tests cover Unicode, packet bounds and malformed data, stale sessions, peer routing, and duplicate operations. An Android instrumentation test covers crown direction, fractional movement, boundaries, focused text editing, and unrelated input sources.

Run instrumentation tests only on a disposable emulator you intentionally started:

    ./gradlew :watch:connectedDebugAndroidTest

This command targets connected Android devices. Do not run it while a personal device is connected. CI runs the test using a disposable emulator and does not target your physical watch.

The CI workflow builds, runs JVM tests and lint, compiles instrumentation tests, and runs rotary tests in an Android emulator. The GitHub-hosted workflow must be observed after the first push; local preparation is not evidence that a remote CI run has passed.

## Limits

Standard Android InputConnection fields are supported. Lock-screen PIN entry and custom in-app keyboards may not use third-party IMEs. Text is limited to a 60 KB packet; destination fields may impose smaller limits. Both devices must remain connected, and the watch field must remain open until insertion is confirmed.

English is currently the only UI language. Entered text can use any Unicode language supported by the destination app.

## Project

- phone — companion editor and notification listener
- watch — system IME, setup screen, and native crown scrolling
- protocol — versioned UTF-8 transport and session guards
- shared-ui — native Android styling and drawn icons

See [CONTRIBUTING.md](CONTRIBUTING.md), [CHANGELOG.md](CHANGELOG.md), and [THIRD_PARTY.md](THIRD_PARTY.md).

## License

MIT. See [LICENSE](LICENSE) and [LICENSING.md](LICENSING.md). Wearboard inspired the initial prototype; upstream source is not included in this repository or its APKs.
