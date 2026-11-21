# Architecture

## Product model

The project coordinates independent customization capabilities. It is not a replacement Android framework and does not attempt to boot iOS.

## Layers

1. **Presentation** — Compose diagnostics, launcher UI and per-feature status.
2. **Orchestrator** — serializes operations and enforces preflight, backup, apply, verify and rollback.
3. **Capability registry** — classifies features as supported / partial / unavailable / untested on the exact firmware.
4. **Adapters**
   - Android public APIs (launcher role, wallpaper, notification listener, media session access).
   - Samsung integrations (supported theme mechanisms and user-guided configuration).
   - Optional root provider (explicit user authorization and read-only capability probes).
   - Device-specific One UI adapter, gated by verified firmware fingerprints.
5. **Persistence** — bounded app-private operation journal. The current implementation uses SharedPreferences on an I/O dispatcher; Room may replace it as profiles and backup manifests grow.
6. **Recovery** — operation-specific undo actions and post-rollback verification.

## Current launcher preview

HomeLauncherActivity is a selectable HOME activity. It enumerates exported launcher activities through PackageManager, displays their actual icons, supports label search and builds a best-effort dock from Samsung/Google package identifiers. App placement, folders, widgets, multiple editable pages and package-change refresh are not implemented yet.

NotificationAccessService is registered but intentionally does not process notification content. Android permission must be granted manually. The diagnostic UI only checks whether the listener is enabled.

## Core interfaces

- CustomizationOperation: declares id, risk, prerequisite checks, backup, apply, verify and rollback.
- CustomizationCoordinator: serializes operations, requires a verified backup before applying, verifies the result and invokes rollback on failure.
- OperationJournal: persists state transitions (planned, checking, backing_up, applying, verifying, applied, failed, rolling_back, rolled_back).
- PreferencesOperationJournal: retains the latest 250 journal events locally.
- BackupStore: future abstraction for structured backup manifests and data the app is legally and technically allowed to restore.
- RootExecutor: future narrowly scoped command execution; no arbitrary shell is exposed to the UI.

## Safety invariants

- Every mutating operation must declare a rollback strategy before it can be registered.
- Backup must be completed and verified before mutation.
- No blind overwrite of user settings or theme state.
- Never write boot, vendor_boot, vbmeta, super or raw block devices.
- Never silently install modules, grant permissions, or execute root commands.
- Root operations are opt-in, individually explained, and allowlisted.
- Failed verification triggers rollback of that operation and stops dependent operations.
- Reboot-required operations cannot be reported as applied before post-reboot verification.
- UI progress is derived from journal events, never simulated.

## Capability status

- **Supported**: tested on the exact model, firmware and Android build.
- **Partial**: some behavior works but platform restrictions remain.
- **Unavailable**: blocked by platform/security restrictions.
- **Untested**: not yet validated on the target build.

## Implementation phases

1. Device diagnostics and explicit root-provider probe (basic probes implemented; results still require device interpretation).
2. Transaction coordinator and durable operation journal (implemented; no mutating operation is registered yet).
3. Basic launcher preview (implemented); editing, folders and widgets remain.
4. Theme integration and user-guided Samsung configuration.
5. Notification/media companion components.
6. Lock-screen and Quick Settings research prototypes.
7. Device-specific root/systemless adapter only after exact firmware research and rollback testing.
