package dev.ioslook.transformation.launcher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class LaunchableApp(
    val label: String,
    val packageName: String,
    val resolveInfo: ResolveInfo
)

class HomeLauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(query, 0)
            .asSequence()
            .filter { it.activityInfo.packageName != packageName }
            .map {
                LaunchableApp(
                    label = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    resolveInfo = it
                )
            }
            .distinctBy { "${it.packageName}/${it.resolveInfo.activityInfo.name}" }
            .sortedBy { it.label.lowercase() }
            .toList()

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                LiquidHomeScreen(
                    apps = apps,
                    onLaunch = { app ->
                        val launch = packageManager.getLaunchIntentForPackage(app.packageName)
                        if (launch != null) startActivity(launch)
                    }
                )
            }
        }
    }
}

@Composable
private fun LiquidHomeScreen(
    apps: List<LaunchableApp>,
    onLaunch: (LaunchableApp) -> Unit
) {
    val dockPackages = remember(apps) {
        listOf(
            "com.google.android.dialer",
            "com.samsung.android.messaging",
            "com.sec.android.app.camera",
            "com.google.android.apps.maps"
        ).mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF24334B), Color(0xFF111827), Color(0xFF070A11))
                )
            )
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(28.dp))
            Text(
                "9:41",
                color = Color.White,
                fontSize = 58.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp
            )
            Text(
                "Sunday, November 22",
                color = Color(0xFFD5DEEF),
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 22.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                items(apps, key = { "${it.packageName}/${it.resolveInfo.activityInfo.name}" }) { app ->
                    AppIcon(app = app, onClick = { onLaunch(app) })
                }
            }

            if (dockPackages.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0x663F526D))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    dockPackages.forEach { app ->
                        AppIcon(app = app, compact = true, onClick = { onLaunch(app) })
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0x663F526D))
                        .height(82.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Liquid Glass Dock", color = Color(0xFFDCE7F7))
                }
            }
        }
    }
}

@Composable
private fun AppIcon(
    app: LaunchableApp,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val colors = listOf(
        Color(0xFF5279A8), Color(0xFF8A668E), Color(0xFF4F8B85),
        Color(0xFF9A7654), Color(0xFF52618E), Color(0xFF8B5966)
    )
    val color = colors[(app.packageName.hashCode().ushr(1)) % colors.size]
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 54.dp else 58.dp)
                .clip(RoundedCornerShape(if (compact) 16.dp else 18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(color.copy(alpha = 0.96f), color.copy(alpha = 0.62f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                app.label.take(1).uppercase(),
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium
            )
        }
        if (!compact) {
            Text(
                app.label,
                color = Color.White,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
