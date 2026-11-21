# iOS Transformation

An independent Android customization project for Samsung Galaxy A26 5G (SM-A266B), with an iOS 26-inspired Dark / Liquid Glass visual target. It is not Apple software and does not replace Android with iOS.

## Current build: 0.3.0 preview

- A selectable HOME launcher with real installed-app icons, a searchable app grid, clock/date header and an adaptive Samsung/Google dock.
- Read-only device and permission diagnostics for the SM-A266B.
- An explicit root probe for su identity, KernelSU/Magisk CLI presence, SELinux, verified-boot properties, mount namespace identifiers and the /data/adb/modules directory.
- A registered notification-listener service. It intentionally does not read, store or forward notification contents; the user must separately grant access in Android Settings.
- A transaction coordinator and bounded operation journal. No system customization operation is registered yet.

The launcher is an early functional preview. It does not yet provide editable icon placement, folders, widgets, a replacement lock screen, a replacement notification shade or SystemUI modification.

## Build and install

Every push and pull request to main runs unit tests and assembles a debug APK. Successful runs publish ios-transformation-debug for 14 days.

1. Open [GitHub Actions](https://github.com/alexalxandrov/ios-look/actions).
2. Open the latest successful Android CI run.
3. Download ios-transformation-debug and extract app-debug.apk.
4. Install with adb install -r app-debug.apk, or transfer the APK to the phone.

Open iOS Transformation and press Set as default launcher to select the HOME app. Android displays its own confirmation. The notification-listener permission is separate and optional.

## Device compatibility

The project uses compileSdk 36 and is being tested on the Android 17 / API 37 Galaxy A26 firmware A266BXXUCDZJ6. A lower compileSdk does not prevent an app from running on a newer Android release, but it does mean Android 17-only APIs are not used. Device-specific features remain disabled until tested on the exact firmware.

## Safety

All current diagnostics are read-only. Root checks run only after the user presses the full diagnostics button. uid=0 does not establish that a root manager, mount operations, systemless modules or SystemUI overlays are supported. No boot images, firmware partitions, SELinux settings or verified-boot state are modified.

KernelSU's official documentation requires an unlocked bootloader; a reported temporary root path must not be assumed to be KernelSU. See docs/SAFETY.md.

## License

TBD. No Apple proprietary assets, icons, fonts, sounds or source code are included.
