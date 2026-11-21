package dev.ioslook.transformation

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ioslook.transformation.diagnostics.DeviceDiagnostics
import dev.ioslook.transformation.diagnostics.DeviceReport
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
                TransformationHome()
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun TransformationHome() {
    val scope = rememberCoroutineScope()
    var report by remember { mutableStateOf<DeviceReport?>(null) }
    var checking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        report = DeviceDiagnostics.readBasic()
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
                Text("DEVICE", color = Color(0xFF9EADC7), fontSize = 10.sp, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(8.dp))
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
                    report?.rootSummary ?: "Root status: not checked",
                    color = Color(0xFFCAD5E8),
                    fontSize = 13.sp
                )
            }

            GlassCard {
                Text("TRANSFORMATION PROFILE", color = Color(0xFF9EADC7), fontSize = 10.sp, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(8.dp))
                Text("iOS 26 · Dark · Liquid Glass", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "A visual target, not an iOS port. Components will be enabled only after compatibility checks.",
                    color = Color(0xFFB5C0D3),
                    fontSize = 13.sp
                )
            }

            GlassCard {
                Text("CAPABILITY CHECK", color = Color(0xFF9EADC7), fontSize = 10.sp, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(8.dp))
                CapabilityRow("Launcher / icon pack", "Planned · no root")
                CapabilityRow("Theme and typography", "Needs device validation")
                CapabilityRow("Notifications / media", "Permission-based")
                CapabilityRow("Quick Settings / SystemUI", "Restricted · experimental")
                CapabilityRow("Root adapter", report?.rootSummary ?: "Not checked")
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        checking = true
                        scope.launch {
                            report = withContext(Dispatchers.IO) { DeviceDiagnostics.runFullCheck() }
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
                    Text(if (checking) "Checking…" else "Run device diagnostics")
                }
            }

            Text(
                "No system files are changed by this build. Backup, operation journal and rollback are foundational requirements before any mutating feature is enabled.",
                color = Color(0xFF8F9BB0),
                fontSize = 12.sp
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun GlassCard(content: @androidx.compose.runtime.Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xAA1B2638)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            content = content
        )
    }
}

@androidx.compose.runtime.Composable
private fun CapabilityRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFFE7ECF6), fontSize = 13.sp)
        Text(value, color = Color(0xFFAAB8D0), fontSize = 11.sp)
    }
}
