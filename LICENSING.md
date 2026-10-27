# Licensing decisions

## Project code: MIT

The project code is offered under the MIT license. It is a short permissive license that allows use, modification, sharing, and commercial use, provided the copyright and license notices are preserved. It does not require recipients to publish their modifications.

A license does not control repository visibility. This repository remains private until its owner explicitly changes that setting. Anyone given a copy under MIT receives the permissions in that license; making a repository private later does not revoke licenses already granted.

The MIT license covers the project code and artwork, not third-party components or signing keys.

## Wearboard

Wearboard inspired the original prototype. No explicit source license was found in the inspected upstream checkout. Public visibility alone is not permission to copy or relicense it.

The current source archive excludes that checkout and earlier prototype archives. The adapted payload was removed in 0.3 in favor of the project's own PRK1 format. THIRD_PARTY.md preserves provenance; it is not a claim to have relicensed Wearboard.

Do not publish the old upstream checkout or prototype source archives without first reviewing any retained upstream material and permissions.

## Android and library notices

Google Play services and transitive libraries keep their own terms. The official Google OSS licenses Gradle plugin collects notices from dependencies and embedded library license metadata.

The release APKs include:
- the project MIT license;
- generated third-party license texts/notices, available in Open-source licenses on phone and watch.

Debug builds may contain the official plugin's debug-information placeholder instead of dependency notices. Use the release-mode APKs for distribution.

The generated notices are useful compliance material, not a replacement for reviewing dependency terms when changing the app or its distribution.

## Downloadable beta APKs

The private beta APKs are release-mode builds signed using the original development certificate, to preserve in-place updates from the user's earlier local builds. The private key is not uploaded to GitHub.

Before wider distribution, decide on a durable production signing identity. Changing that identity normally prevents in-place updates of existing installations.
