# Build Release APK for Buck Manager

The goal is to generate a release-ready APK so you can test the app on a physical device. This involves syncing the native Android project (to include the newly added Fund Goal Widget) and then running the Gradle build process.

## User Review Required

> [!IMPORTANT]
> **Native Sync**: I noticed that `FundGoalWidget` was added to `app.json` but is missing from the `android/` directory (AndroidManifest.xml and widget provider XMLs). I will run `npx expo prebuild` to sync these changes. If you have made manual changes to files in the `android/` directory that were not via Expo config plugins, they might be affected.

## Proposed Changes

### Native Project Sync

#### [MODIFY] Native Android Files
I will run `npx expo prebuild --platform android` to ensure the `android/` directory is in sync with `app.json`. This will specifically:
- Add the `FundGoalWidget` receiver to `AndroidManifest.xml`.
- Generate `widgetprovider_fundgoalwidget.xml`.
- Ensure the native code for the widget is correctly linked.

### Build Process

#### [RUN] Gradle Build
I will run the following command in the `android/` directory:
```bash
./gradlew assembleRelease
```
This will produce a release APK using the `debug` signing key (as configured in `build.gradle` for testing purposes).

## Verification Plan

### Manual Verification
- After the build finishes, I will provide the path to the generated APK: `android/app/build/outputs/apk/release/app-release.apk`.
- You can then install this APK on your Android device to test the app and the new Fund Goal Widget.
