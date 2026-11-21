# Safety and recovery policy

## Important facts

- KernelSU's official documentation says KernelSU requires an unlocked bootloader. A reported temporary root path (including an exploit-based route) must not be assumed to be KernelSU-compatible or persistent. The app must test the actual `su` provider and its behavior.
- Root access does not bypass Android's overlay restrictions, signature policies, SELinux, verified boot, or Samsung-specific implementation details.
- Android Runtime Resource Overlays (RRO) can change only existing resources and are subject to overlayable declarations and policy restrictions. An ordinary third-party APK cannot freely replace arbitrary SystemUI resources.
- A runtime root session is not equivalent to bootloader unlock and does not guarantee that systemless mounts or modules can be installed.

## Forbidden actions

The app must never:
- unlock/relock the bootloader;
- flash or patch boot images;
- write to raw block devices or firmware partitions;
- alter vbmeta, boot, init_boot, vendor_boot, super, modem or recovery;
- disable verified boot or SELinux;
- install a KernelSU module without a separate explicit confirmation;
- overwrite original files without a verified backup;
- claim full rollback when an operation has no tested inverse.

## Root execution policy

Root is optional and disabled by default. Each privileged operation must:
1. explain exactly what it will change;
2. declare target paths and expected effects;
3. verify prerequisites and available recovery path;
4. create and verify a backup;
5. request explicit confirmation;
6. execute only allowlisted commands;
7. verify the result;
8. rollback automatically on failure where safe.

No generic terminal or arbitrary command input is part of the end-user app.

## Rollback levels

- **Component rollback**: undo one operation.
- **Profile rollback**: undo all operations belonging to one profile in reverse order.
- **Emergency recovery**: disable the project's own systemless module or integration and restore backed-up user-facing settings.

A feature is not considered reversible until rollback has been tested on the target firmware.
