# Device validation: Galaxy A26 5G

This guide is for the first on-device smoke test of the debug build. The current app is diagnostic-only: it does not apply themes, change system settings, install modules, or write to system partitions.

## 1. Download and install

1. Open the successful **Android CI** run in GitHub Actions.
2. Download the artifact named `ios-transformation-debug`.
3. Extract `app-debug.apk`.
4. Install it on the Galaxy A26 5G:

```bash
adb install -r app-debug.apk
```

Enable USB debugging only if you intend to use ADB. APK installation can also be done by transferring the file to the phone and opening it.

## 2. Capture the exact device baseline

Run these read-only commands and save their output with the test notes:

```bash
adb shell getprop ro.product.manufacturer
adb shell getprop ro.product.model
adb shell getprop ro.product.device
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell getprop ro.build.display.id
adb shell getprop ro.build.fingerprint
adb shell getprop ro.build.version.security_patch
```

The intended baseline is Samsung Galaxy A26 5G, model **SM-A266B**, build family **A266BXXUCDZJ6**, Android 17 / One UI 9. Do not assume the device matches this baseline merely because its marketing name is Galaxy A26.

## 3. Smoke-test the app

- Launch **iOS Transformation** and confirm the app opens without crashing.
- Confirm manufacturer, model, Android release and API level are displayed.
- Use **Run device diagnostics** only when ready to test the optional `su` probe. A root manager may display its own authorization prompt.
- Record whether the probe reports no provider, denied authorization, timeout, or `uid=0`.
- Do not infer KernelSU module support, systemless mount support, boot persistence, or SystemUI access from `uid=0` alone.

## 4. Report a problem

Capture the exact build fingerprint, app version, the visible diagnostic result, and the relevant logcat excerpt:

```bash
adb logcat -d -t 1500 > ios-transformation-logcat.txt
```

Review the log before sharing it; remove personal data and unrelated application content.

## Acceptance criteria

- App launches on the target phone.
- Device properties match the expected model and firmware baseline.
- Diagnostics complete without an app crash.
- The optional root probe does not run before the user presses the button.
- No device settings, themes, overlays, user data, or firmware partitions are changed.
