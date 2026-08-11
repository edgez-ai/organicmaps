# EdgeZ Organic Maps Android libraries

This repository packages the EdgeZ-customized Organic Maps engine as reusable
Android AAR modules. It is the shared native map implementation for the EdgeZ
Android app and Flutter SDK.

## Modules

- `organicmaps-sdk`: renderer, JNI bridge, map storage, download APIs, EdgeZ
  marker colors, and geofence lines.
- `organicmaps-location-core`: Android location integration.
- `organicmaps-maps-world`: bootstrap world-map assets.

The Organic Maps engine is stored at the repository root. Large third-party
dependencies remain pinned Git submodules; do not replace them with vendored
copies.

## Build

Clone with recursive submodules, then install JDK 21, Android SDK 36, NDK
`29.0.14206865`, and CMake 3.22.1. Build with:

```sh
./gradlew \
  :organicmaps-location-core:publishReleasePublicationToBuildRepository \
  :organicmaps-maps-world:publishReleasePublicationToBuildRepository \
  :organicmaps-sdk:publishReleasePublicationToBuildRepository
```

The resulting Maven repository is written to `build/maven-repository`. Its
coordinates default to:

```text
ai.edgez.organicmaps:organicmaps-sdk:0.1.0-SNAPSHOT
ai.edgez.organicmaps:organicmaps-location-core:0.1.0-SNAPSHOT
ai.edgez.organicmaps:organicmaps-maps-world:0.1.0-SNAPSHOT
```

Override the version with `-PVERSION_NAME=1.0.0`. Applications normally depend
on both `organicmaps-sdk` and `organicmaps-maps-world`; the SDK POM brings in
`organicmaps-location-core` transitively.

GitHub Actions checks out the pinned recursive submodules, performs the same
release build, and uploads packaged artifacts for pushes, pull requests, and
manual runs. No dependency-generation step is required after checkout.

When a GitHub Release is published, the workflow removes an optional leading
`v` from its tag for the Maven version. For example, release tag `v1.2.0`
publishes version `1.2.0` and attaches:

- the complete Maven repository ZIP;
- the SDK, location, and world-map AARs;
- the example APK;
- `SHA256SUMS` covering every attached binary.

## Example app

Open the repository root in Android Studio, let Gradle sync, select the
`example-app` run configuration, and run it on an Android device or emulator.
The example centers on Stockholm and renders three sample EdgeZ nodes plus a
green geofence using the customized native APIs. Use **Reset sample view** to
reapply the test camera and overlays.

Build the APK from the command line with:

```sh
./gradlew -Pedgez.organicmaps.abis=arm64-v8a :example-app:assembleDebug
```

The APK is written to
`example-app/build/outputs/apk/debug/example-app-debug.apk` and is also uploaded
by GitHub Actions.

The `edgez.organicmaps.abis` property keeps local smoke builds small. Use
`x86_64` for an Intel emulator, or a comma-separated list when needed. Release
publication omits the property and builds `armeabi-v7a`, `arm64-v8a`, `x86`,
and `x86_64`.

Run `tools/setup-android-dependencies.sh` manually when initially cloning
without `--recurse-submodules`, or when synchronizing the pinned submodules
with their configured upstream repositories. Builds use the submodule commits
recorded by this repository as their source of truth.

## Attribution

The engine is derived from Organic Maps and retains its Apache-2.0 license.
Third-party submodules retain their respective licenses and notices.
