# Target device baseline

Initial target supplied for this project:

- Manufacturer: Samsung
- Model: Galaxy A26 5G (SM-A266B)
- Firmware/build reported: A266BXXUCDZJ6
- Android: 17
- One UI: 9
- Security patch reported: 2026-10-05

Samsung's public update record for this model lists build A266BXXUCDZJ6 with Android 17 / One UI 9 and describes a more customizable Quick Panel, including resizing brightness/volume sliders and the media player.

The app must still collect the live values from the handset. Firmware suffix, CSC/region, security patch, build fingerprint, boot state and root provider may differ from the public update record.

## Validation required on handset

- Confirm exact `Build.FINGERPRINT`, `Build.DISPLAY`, `Build.VERSION.SDK_INT`, model and CSC.
- Confirm whether the installed root method exposes a working `su` interface to ordinary apps.
- Identify Good Lock / Theme Park availability and version.
- Inspect installed SystemUI and framework package versions before any device-specific adapter is enabled.
- Test every operation and rollback independently.

No SystemUI or framework package is currently modified by the project.
