# Releasing

## Before the first public release

1. Create the GitHub repository and push the prepared source. No remote repository is created by the local packaging scripts.
2. Enable private vulnerability reporting and review repository Actions permissions.
3. Observe CI, including the emulator rotary test, after the first push.
4. Test install, selection, reconnect, insertion, password opt-in, crown direction/bounds/focus, large font, and switching to Gboard on devices you explicitly own or are authorized to test. Do not use the user's physical watch without new permission.
5. Create a dedicated Android release keystore and keep an offline backup. Public debug artifacts are not a stable update channel.
6. Choose one durable release certificate for both APKs and future versions.

## Signing configuration

Set these environment variables for local signed release builds, or configure the equivalent repository Actions secrets:

- PRK_KEYSTORE_FILE — absolute path to the keystore (local builds only).
- PRK_KEYSTORE_PASSWORD
- PRK_KEY_ALIAS
- PRK_KEY_PASSWORD

For GitHub Actions, add PRK_KEYSTORE_BASE64 containing the base64-encoded keystore instead of PRK_KEYSTORE_FILE. Actions decodes it to a temporary runner location and deletes it after use.

No signing secret is embedded in the project. The release build remains unsigned if no signing configuration is supplied; do not distribute unsigned APKs.

    ./gradlew :protocol:test :phone:lintRelease :watch:lintRelease :phone:assembleRelease :watch:assembleRelease

Verify both signatures with Android SDK apksigner and confirm their certificate SHA-256 digests match.

## Version and draft release

Update version.properties, CHANGELOG.md, and docs/RELEASE_NOTES.md together. Push the source to main and wait for CI.

Run the **Draft release** workflow manually with a tag matching version.properties, such as v0.3.0-beta.2. It validates the version, builds both signed APKs, verifies their signatures, computes SHA-256 checksums, and creates a draft prerelease in the current repository.

The workflow's required signing secrets must be present. The workflow does not publish the draft. Review notes, assets, checksums, signing identity, and test results before publishing through GitHub.

Creating the local source package does not run or validate GitHub-hosted workflows. No claim is made that the build is byte-for-byte reproducible or that hardware tests have passed.
