package dev.ioslook.transformation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.os.Bundle
import android.app.role.RoleManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ioslook.transformation.diagnostics.DeviceDiagnostics
import dev.ioslook.transformation.diagnostics.DeviceReport
import dev.ioslook.transformation.diagnostics.DiagnosticItem
import dev.ioslook.transformation.diagnostics.DiagnosticState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val GlassColors = darkColorScheme(
    primary = Color(0xFFDCE7FF),
    secondary = Color(0xFFAEBBD4),
    background = Color(0xFF080B12),
    surface = Color(0xFF171D2A),
    onPrimary = Color(0xFF101827),
    onBackground = Color(0xFFF4F6FC),
    onSurface = Color(0xFFF4F6FC)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = GlassColors) {
                TransformationHome(context = this)
            }
        }
    }
}

@Composable
private fun TransformationHome(context: Context) {
    val scope = rememberCoroutineScope()
    var report by remember { mutableStateOf<DeviceReport?>(null) }
    var checking by remember { mutableStateOf(false) }
    val homeRoleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        scope.launch {
            report = withContext(Dispatchers.IO) { DeviceDiagnostics.readBasic(context) }
        }
    }
    val notificationSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        scope.launch {
            report = withContext(Dispatchers.IO) { DeviceDiagnostics.readBasic(context) }
        }
    }

    LaunchedEffect(context) {
        report = withContext(Dispatchers.IO) { DeviceDiagnostics.readBasic(context) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF080B12)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF18253B), Color(0xFF090D16), Color(0xFF05070C))
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "iOS TRANSFORMATION",
                color = Color(0xFFB8C8E8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp
            )
            Text(
                text = "Liquid Glass.",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Dark system experience · Android edition",
                color = Color(0xFFADB8CC),
                fontSize = 14.sp
            )

            GlassCard {
                SectionLabel("DEVICE BASELINE")
                Text(
                    report?.deviceName ?: "Detecting device…",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    report?.platform ?: "Reading Android build information",
                    color = Color(0xFFB5C0D3),
                    fontSize = 13.sp
                )
                Text(
                    "Security patch: ${report?.securityPatch ?: "—"}",
                    color = Color(0xFFB5C0D3),
                    fontSize = 12.sp
                )
                Text(
                    report?.fingerprint ?: "Build fingerprint unavailable",
                    color = Color(0xFF8795AD),
                    fontSize = 10.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    report?.rootSummary ?: "Root status: not checked",
                    color = Color(0xFFCAD5E8),
                    fontSize = 13.sp
                )
            }

            GlassCard {
                SectionLabel("TRANSFORMATION PROFILE")
                Text(
                    "iOS 26 · Dark · Liquid Glass",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "An independent Android implementation. Components will be enabled only after compatibility checks.",
                    color = Color(0xFFB5C0D3),
                    fontSize = 13.sp
                )
            }

            val homeRoleReady = report?.capabilities
                ?.firstOrNull { it.id == "home_role" }
                ?.state == DiagnosticState.READY

            if (!homeRoleReady) {
                Button(
                    onClick = {
                        val roleManager = context.getSystemService(RoleManager::class.java)
                        if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                            homeRoleLauncher.launch(
                                roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9DB7E8),
                        contentColor = Color(0xFF101827)
                    )
                ) {
                    Text("Set as default launcher")
                }
            }

            val notificationAccessReady = report?.capabilities
                ?.firstOrNull { it.id == "notification_listener" }
                ?.state == DiagnosticState.READY
            if (!notificationAccessReady) {
                Button(
                    onClick = {
                        notificationSettingsLauncher.launch(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34445D),
                        contentColor = Color.White
                    )
                ) {
                    Text("Open notification access settings")
                }
            }

            GlassCard {
                SectionLabel("LIVE CAPABILITY CHECKS")
                val capabilities = report?.capabilities.orEmpty()
                if (capabilities.isEmpty()) {
                    Text("Collecting device and permission state…", color = Color(0xFFAAB8D0), fontSize = 13.sp)
                } else {
                    capabilities.forEach { item ->
                        CapabilityRow(item)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        checking = true
                        scope.launch {
                            report = withContext(Dispatchers.IO) {
                                DeviceDiagnostics.runFullCheck(context)
                            }
                            checking = false
                        }
                    },
                    enabled = !checking,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDCE7FF),
                        contentColor = Color(0xFF101827)
                    )
                ) {
                    Text(if (checking) "Checking…" else "Run full diagnostics")
                }
                Text(
                    "Full diagnostics also makes one read-only su probe. Your root manager may ask for authorization. Nothing is modified.",
                    color = Color(0xFF8F9BB0),
                    fontSize = 11.sp
                )
            }

            if (report != null && report?.rootSummary != "Root status: not checked") {
                GlassCard {
                    SectionLabel("ROOT DIAGNOSTIC DETAILS")
                    report?.notes.orEmpty().forEach { detail ->
                        Text(detail, color = Color(0xFFAAB8D0), fontSize = 11.sp)
                    }
                }
            }

            Text(
                "The transaction engine now requires a verified backup before applying any registered change. No customization operation is enabled in this build yet.",
                color = Color(0xFF8F9BB0),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SectionLabel(value: String) {
    Text(value, color = Color(0xFF9EADC7), fontSize = 10.sp, letterSpacing = 1.8.sp)
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xAA1B2638)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            content = content
        )
    }
}

@Composable
private fun CapabilityRow(item: DiagnosticItem) {
    val statusLabel = when (item.state) {
        DiagnosticState.READY -> "READY"
        DiagnosticState.NEEDS_USER_ACTION -> "ACTION"
        DiagnosticState.NOT_TARGET -> "MISMATCH"
        DiagnosticState.UNTESTED -> "UNTESTED"
    }
    val statusColor = when (item.state) {
        DiagnosticState.READY -> Color(0xFF9BD7BB)
        DiagnosticState.NEEDS_USER_ACTION -> Color(0xFFE4C58A)
        DiagnosticState.NOT_TARGET -> Color(0xFFE69A9A)
        DiagnosticState.UNTESTED -> Color(0xFF9DAAC1)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.title, color = Color(0xFFE7ECF6), fontSize = 13.sp)
            Text(statusLabel, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(item.detail, color = Color(0xFFAAB8D0), fontSize = 11.sp)
    }
}
