# Architecture

## Product model

The project is a coordinator for independent customization capabilities. It is not a replacement Android framework and does not attempt to boot iOS.

### Layers

1. **Presentation** — Compose UI, progress, diagnostics, per-feature status and rollback controls.
2. **Orchestrator** — plans operations, checks prerequisites, orders dependencies, records outcomes.
3. **Capability registry** — reports supported / partial / unavailable / untested on this exact firmware.
4. **Adapters**
   - Android public APIs (wallpaper, launcher role, notification listener, media session access).
   - Samsung integrations (Good Lock / Theme Park / supported theme mechanisms).
   - Optional root provider (explicit user authorization).
   - Device-specific One UI adapter, gated by verified firmware fingerprints.
5. **Persistence** — Room database for operation journal, profile state and backup manifests.
6. **Recovery** — idempotent undo actions, restore points and post-rollback verification.

## Core interfaces (planned)

- `CapabilityProbe`: inspect prerequisites without mutation.
- `CustomizationOperation`: declare id, risk, prerequisites, apply, verify and rollback.
- `ExecutionPlan`: immutable ordered list of operations and dependency graph.
- `OperationJournal`: durable state transitions (planned, running, applied, verified, failed, rolled_back).
- `BackupStore`: store only data the app is legally and technically allowed to restore.
- `RootExecutor`: narrowly scoped command execution, no arbitrary shell exposed to UI.

## Safety invariants

- Every mutating operation must declare a rollback strategy before it can be registered.
- Backup is completed and verified before mutation.
- No blind overwrite of user settings or theme state.
- Never write boot, vendor_boot, vbmeta, super or raw block devices.
- Never silently install modules, grant permissions, or execute root commands.
- Root operations are opt-in, individually explained, and allowlisted.
- A failed verification triggers rollback of that operation and stops dependent operations.
- Reboot-required operations must be clearly identified and cannot be reported as applied before reboot verification.
- UI progress is derived from journal events, never simulated.

## Capability status

- **Supported**: tested on the exact model, firmware and Android build.
- **Partial**: some behavior works but platform restrictions remain.
- **Unavailable**: blocked by platform/security restrictions.
- **Untested**: not yet validated on the target build.

## Implementation phases

1. Device diagnostics and root-provider probe (current starter).
2. Operation model, journal and backup metadata.
3. Launcher and visual asset pack.
4. Theme integration and user-guided Samsung configuration.
5. Notification/media companion components.
6. Lock-screen and Quick Settings research prototypes.
7. Device-specific root/systemless adapter only after exact firmware research and rollback testing.
