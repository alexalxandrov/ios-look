package dev.ioslook.transformation.diagnostics

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.app.role.RoleManager
import android.provider.Settings
import java.util.concurrent.TimeUnit

enum class DiagnosticState { READY, NEEDS_USER_ACTION, NOT_TARGET, UNTESTED }

data class DiagnosticItem(val id: String, val title: String, val state: DiagnosticState, val detail: String)

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
        val listenerComponent = ComponentName(
            context,
            "dev.ioslook.transformation.notifications.NotificationAccessService"
        )
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty().split(':')
        val notificationAccess = enabledListeners.any {
            ComponentName.unflattenFromString(it) == listenerComponent
        }

        val capabilities = listOf(
            DiagnosticItem(
                "target_device", "Galaxy A26 target match",
                if (targetMatch) DiagnosticState.READY else DiagnosticState.NOT_TARGET,
                if (targetMatch) "Samsung SM-A266B detected. Firmware-specific features still require validation."
                else "Expected Samsung SM-A266B; detected ${Build.MANUFACTURER} ${Build.MODEL}."
            ),
            DiagnosticItem(
                "overlay_permission", "Display over other apps",
                if (overlayAllowed) DiagnosticState.READY else DiagnosticState.NEEDS_USER_ACTION,
                if (overlayAllowed) "The app currently has overlay permission."
                else "Not granted. No permission request was opened; only overlay-based features would need it."
            ),
            DiagnosticItem(
                "write_settings", "Modify system settings",
                if (writeSettingsAllowed) DiagnosticState.READY else DiagnosticState.NEEDS_USER_ACTION,
                if (writeSettingsAllowed) "The app currently has permission to modify supported system settings."
                else "Not granted. This permission is optional and is not requested automatically."
            ),
            DiagnosticItem(
                "home_role", "Default launcher role",
                if (isHomeRoleHeld) DiagnosticState.READY else DiagnosticState.NEEDS_USER_ACTION,
                if (isHomeRoleHeld) "iOS-inspired home launcher is available and currently selected."
                else "The launcher is implemented. Select it as the default HOME app to use it."
            ),
            DiagnosticItem(
                "systemui", "One UI / SystemUI integration", DiagnosticState.UNTESTED,
                "Not probed. Model detection does not prove that SystemUI resources can be changed."
            ),
            DiagnosticItem(
                "notification_listener", "Notification access",
                if (notificationAccess) DiagnosticState.READY else DiagnosticState.NEEDS_USER_ACTION,
                if (notificationAccess) "Notification listener is enabled. This build does not collect or store notification content."
                else "Listener is registered, but access is not granted. Enable it manually to permit future notification features."
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

    /** Fixed, read-only probes only; no writes to files, settings or partitions. */
    fun runFullCheck(context: Context): DeviceReport {
        val base = readBasic(context)
        val output = probeSu()
        val isRoot = output.contains("uid=0")
        val ksud = output.lineSequence().firstOrNull { it.startsWith("KSUD=") }?.substringAfter("=") ?: "not reported"
        val magisk = output.lineSequence().firstOrNull { it.startsWith("MAGISK=") }?.substringAfter("=") ?: "not reported"
        val provider = when {
            ksud != "not found" && ksud.isNotBlank() -> "KernelSU command-line tool found"
            magisk != "not found" && magisk.isNotBlank() -> "Magisk command-line tool found"
            else -> "Root provider CLI not identified"
        }
        val rootSummary = when {
            output.startsWith("TIMEOUT:") -> "Root status: su probe timed out"
            output.startsWith("ERROR:") -> "Root status: ${output.removePrefix("ERROR:").take(140)}"
            isRoot -> "Root status: su returned uid=0"
            else -> "Root status: uid=0 not confirmed"
        }
        fun value(key: String): String =
            output.lineSequence().firstOrNull { it.startsWith("$key=") }
                ?.substringAfter("=")?.ifBlank { "not reported" } ?: "not reported"

        val notes = listOf(
            "Root provider: $provider",
            "su identity: ${value("ID")}",
            "KernelSU CLI: $ksud",
            "Magisk CLI: $magisk",
            "SELinux: ${value("SELINUX")}",
            "Verified boot state: ${value("VERIFIED_BOOT")}",
            "Bootloader lock property: ${value("BOOT_LOCKED")}",
            "su mount namespace: ${value("MOUNT_NS")}",
            "PID 1 mount namespace: ${value("INIT_NS")}",
            "Systemless module directory: ${value("MODULES")}",
            "These observations do not prove SystemUI overlay or persistent-module support.",
            "No files, settings, overlays, boot images or partitions were modified."
        )
        val rootItem = DiagnosticItem(
            "root_access", "Root shell and provider",
            if (isRoot) DiagnosticState.READY else DiagnosticState.NEEDS_USER_ACTION,
            if (isRoot) "su returned uid=0. Provider, mount and boot-state details are listed below."
            else "A usable uid=0 shell was not confirmed."
        )
        return base.copy(
            rootSummary = rootSummary,
            capabilities = base.capabilities + rootItem,
            notes = notes
        )
    }

    private fun probeSu(): String {
        val command = """
            printf 'ID='; id
            printf 'KSUD='; command -v ksud 2>/dev/null || printf 'not found'; printf '\n'
            printf 'MAGISK='; command -v magisk 2>/dev/null || printf 'not found'; printf '\n'
            printf 'SELINUX='; getenforce 2>/dev/null || printf 'unknown'; printf '\n'
            printf 'VERIFIED_BOOT='; getprop ro.boot.verifiedbootstate 2>/dev/null; printf '\n'
            printf 'BOOT_LOCKED='; getprop ro.boot.flash.locked 2>/dev/null; printf '\n'
            printf 'MOUNT_NS='; readlink /proc/self/ns/mnt 2>/dev/null || printf 'unavailable'; printf '\n'
            printf 'INIT_NS='; readlink /proc/1/ns/mnt 2>/dev/null || printf 'unavailable'; printf '\n'
            printf 'MODULES='
            if [ -d /data/adb/modules ]; then ls -1 /data/adb/modules 2>/dev/null | head -20 | tr '\n' ','; else printf 'directory absent'; fi
            printf '\n'
        """.trimIndent()
        return try {
            val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
            if (!process.waitFor(8, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                "TIMEOUT: root diagnostic command exceeded 8 seconds"
            } else {
                val result = process.inputStream.bufferedReader().use { it.readText() }.take(4000)
                if (process.exitValue() == 0) result else result + "\nEXIT=" + process.exitValue()
            }
        } catch (error: Exception) {
            "ERROR: ${error.javaClass.simpleName}"
        }
    }
}
