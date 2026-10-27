# Security

## Reporting

Use the repository's Security tab to report a vulnerability privately once private vulnerability reporting is enabled by the maintainer. Do not post passwords, signing keys, pairing codes, private messages, or device identifiers in public issues. If private reporting is unavailable, open a minimal issue requesting a private reporting channel without revealing the vulnerability.

## Scope and controls

This is a prerelease input method. Review source before enabling it for sensitive fields. Like any keyboard, it receives what you ask it to type.

Wearable Data Layer communication is scoped by the matching application ID and signing certificate. Session and peer checks limit delivery to the active watch field; operation IDs recognize repeat deliveries within that session. Both companion APKs must be signed consistently.

Protect release keystores and repository signing secrets. Never put a keystore or its passwords in an issue, commit, release asset, source archive, or screenshot. Debug builds are for development; public releases should use a stable dedicated release key.

Run connected instrumentation tests only against disposable emulators. Do not use an end user's physical watch for development tests without explicit permission.
