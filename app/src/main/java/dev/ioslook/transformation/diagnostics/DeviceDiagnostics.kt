package dev.ioslook.transformation.diagnostics

import android.content.Context
import android.os.Build
import android.app.role.RoleManager
import android.provider.Settings
import java.util.concurrent.TimeUnit

enum class DiagnosticState {
    READY,
    NEEDS_USER_ACTION,
    NOT_TARGET,
    UNTESTED
}

data class DiagnosticItem(
    val id: String,
    val title: String,
    val state: DiagnosticState,
    val detail: String
)

data class DeviceReport(
    val deviceName: String,
    val platform: String,
    val fingerprint: String,
    val securityPatch: String,
    val rootSummary: String,
    val capabilities: List<DiagnosticItem>,
    val notes: List<String>
)

object DeviceDiagnostics {
    fun readBasic(context: Context): DeviceReport {
        val targetMatch = Build.MANUFACTURER.equals("samsung", ignoreCase = true) &&
            Build.MODEL.equals("SM-A266B", ignoreCase = true)

        val overlayAllowed = Settings.canDrawOverlays(context)
        val writeSettingsAllowed = Settings.System.canWrite(context)
        val isHomeRoleHeld = context.getSystemService(RoleManager::class.java)
            ?.isRoleHeld(RoleManager.ROLE_HOME) == true

        val capabilities = listOf(
            DiagnosticItem(
                id = "target_device",
                title = "Galaxy A26 target match",
                state = if (targetMatch) DiagnosticState.READY else DiagnosticState.NOT_TARGET,
                detail = if (targetMatch) {
                    "Samsung SM-A266B detected. Firmware-specific features still require validation."
                } else {
                    "Expected Samsung SM-A266B; detected ${Build.MANUFACTURER} ${Build.MODEL}."
                }
            ),
            DiagnosticItem(
                id = "overlay_permission",
                title = "Display over other apps",
                state = if (overlayAllowed) DiagnosticState.READY else DiagnosticState.NEEDS_USER_ACTION,
                detail = if (overlayAllowed) {
                    "The app currently has overlay permission."
                } else {
                    "Not granted. No permission request was opened; only overlay-based features would need it."
                }
            ),
            DiagnosticItem(
                id = "write_settings",
                title = "Modify system settings",
                state = if (writeSettingsAllowed) DiagnosticState.READY else DiagnosticState.NEEDS_USER_ACTION,
                detail = if (writeSettingsAllowed) {
                    "The app currently has permission to modify supported system settings."
                } else {
                    "Not granted. This permission is optional and is not requested automatically."
                }
            ),
            DiagnosticItem(
                id = "home_role",
                title = "Default launcher role",
                state = if (isHomeRoleHeld) DiagnosticState.READY else DiagnosticState.NEEDS_USER_ACTION,
                detail = if (isHomeRoleHeld) {
                    "iOS-inspired home launcher is available and currently selected."
                } else {
                    "The launcher is implemented. Select it as the default HOME app to use it."
                }
            ),
            DiagnosticItem(
                id = "systemui",
                title = "One UI / SystemUI integration",
                state = DiagnosticState.UNTESTED,
                detail = "Not probed. Model detection does not prove that SystemUI resources can be changed."
            ),
            DiagnosticItem(
                id = "notification_listener",
                title = "Notification access",
                state = DiagnosticState.UNTESTED,
                detail = "No notification-listener service is registered in this build."
            )
        )

        return DeviceReport(
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            platform = "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",
            fingerprint = Build.FINGERPRINT,
            securityPatch = Build.VERSION.SECURITY_PATCH,
            rootSummary = "Root status: not checked",
            capabilities = capabilities,
            notes = listOf("Read-only Android build and permission checks completed.")
        )
    }

    /**
     * Explicit, read-only su probe. It runs only after the user presses the
     * full diagnostics button; a root provider may show its own prompt.
     */
    fun runFullCheck(context: Context): DeviceReport {
        val base = readBasic(context)
        val root = probeSu()
        val notes = buildList {
            add("Manufacturer: ${Build.MANUFACTURER}")
            add("Model: ${Build.MODEL}")
            add("Device codename: ${Build.DEVICE}")
            add("Build display: ${Build.DISPLAY}")
            add("SDK: ${Build.VERSION.SDK_INT}")
            add("Security patch: ${Build.VERSION.SECURITY_PATCH}")
            add("Fingerprint: ${Build.FINGERPRINT}")
            add("Root probe: $root")
            add("No files, settings, overlays or partitions were modified.")
            add("A uid=0 result does not prove module, mount, persistence or SystemUI capabilities.")
        }
        return base.copy(rootSummary = root, notes = notes)
    }

    private fun probeSu(): String {
        return try {
            val process = ProcessBuilder("su", "-c", "id")
                .redirectErrorStream(true)
                .start()
            val completed = process.waitFor(3, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                "Root status: probe timed out"
            } else {
                val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
                if (process.exitValue() == 0 && output.contains("uid=0")) {
                    "Root status: su returned uid=0 (provider confirmed)"
                } else {
                    "Root status: unavailable or authorization denied"
                }
            }
        } catch (_: Exception) {
            "Root status: no usable su provider detected"
        }
    }
}
