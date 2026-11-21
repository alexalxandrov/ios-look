package dev.ioslook.transformation.diagnostics

import android.os.Build
import java.util.concurrent.TimeUnit

data class DeviceReport(
    val deviceName: String,
    val platform: String,
    val fingerprint: String,
    val rootSummary: String,
    val notes: List<String>
)

object DeviceDiagnostics {
    fun readBasic(): DeviceReport = DeviceReport(
        deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
        platform = "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",
        fingerprint = Build.FINGERPRINT,
        rootSummary = "Root status: not checked",
        notes = listOf("Basic build properties read locally.")
    )

    /**
     * This is an explicit, read-only probe. It does not run on app startup.
     * The su provider may display its own authorization prompt.
     */
    fun runFullCheck(): DeviceReport {
        val base = readBasic()
        val root = probeSu()
        val notes = buildList {
            add("Manufacturer: ${Build.MANUFACTURER}")
            add("Model: ${Build.MODEL}")
            add("Device: ${Build.DEVICE}")
            add("Build: ${Build.DISPLAY}")
            add("SDK: ${Build.VERSION.SDK_INT}")
            add("Fingerprint: ${Build.FINGERPRINT}")
            add("Root probe: $root")
            add("No files, settings, overlays or partitions were modified.")
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
