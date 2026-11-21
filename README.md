# iOS Transformation

A modular Android customization toolkit for Samsung Galaxy devices, starting with the Galaxy A26 5G (SM-A266B), Android 17 / One UI 9.

**Visual target:** iOS 26-inspired Dark appearance with a strong Liquid Glass aesthetic. This is an independent Android implementation, not Apple software and not an iOS port.

## Project status

Early foundation. The starter app builds successfully in GitHub Actions. The current build is diagnostic-only: it does not apply themes or modify device settings.

## Design principles

- **Maximum resemblance, honest capability reporting:** every feature is classified as supported, partial, unavailable, or not yet tested on the exact firmware.
- **Two execution modes:** standard (no root) and optional elevated mode through a user-authorized root provider.
- **Reversible by design:** snapshot before mutation, record each operation, verify after applying, and provide component-level plus full rollback.
- **No boot-chain changes:** the app will not unlock the bootloader, flash partitions, patch boot images, or write to firmware partitions.
- **No fake progress:** UI progress reflects real completed operations and verification results.
- **Device-specific adapters:** Samsung One UI behavior is isolated from generic Android functionality.

## Planned capabilities

1. Device and capability diagnostics.
2. iOS-inspired launcher, icon pack, wallpaper and widgets.
3. Dark palette, typography and compatible theme integrations.
4. Media player and notification presentation.
5. Lock-screen companion/customization where Android permits.
6. Quick Settings / notification shade integration where supported.
7. Optional KernelSU/root adapter for explicitly supported, systemless operations.
8. Backup, operation journal, health checks and rollback.

## Build and install

GitHub Actions builds a debug APK on every push and pull request to `main`. Successful runs publish an artifact named **ios-transformation-debug** for 14 days.

1. Open [GitHub Actions](https://github.com/alexalxandrov/ios-look/actions).
2. Select the successful Android CI run.
3. Download `ios-transformation-debug` and extract `app-debug.apk`.
4. Install with `adb install -r app-debug.apk`, or transfer the APK to the phone and open it.

See [docs/DEVICE-TEST.md](docs/DEVICE-TEST.md) for the Galaxy A26 first-run checklist and exact baseline commands.

## Device compatibility

The initial project targets a conservative Android SDK baseline. Android 17 / One UI 9 compatibility must be tested on the actual A266B build before enabling any system-level operation.

## Safety

The initial app is diagnostic-only. Root availability does not imply that a particular operation is safe or supported. KernelSU officially requires an unlocked bootloader; any reported temporary privilege mechanism must be independently verified and treated as experimental. See [docs/SAFETY.md](docs/SAFETY.md).

## License

TBD. No Apple proprietary assets, icons, fonts, sounds, or source code are included.
