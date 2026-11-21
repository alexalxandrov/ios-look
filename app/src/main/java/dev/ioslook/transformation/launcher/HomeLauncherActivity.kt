package dev.ioslook.transformation.launcher

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class LaunchableApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable
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
            .filter { it.activityInfo.packageName != packageName && it.activityInfo.exported }
            .distinctBy { it.activityInfo.packageName }
            .map {
                LaunchableApp(
                    label = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name,
                    icon = it.loadIcon(packageManager)
                )
            }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
            .toList()

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                LiquidHomeScreen(
                    apps = apps,
                    onLaunch = { app ->
                        runCatching {
                            startActivity(
                                Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_LAUNCHER)
                                    .setClassName(app.packageName, app.activityName)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
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
    var search by remember { mutableStateOf("") }
    val today = remember {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
        ).replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
    val clock = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) }
    val dockApps = remember(apps) {
        listOf(
            listOf("com.samsung.android.dialer", "com.google.android.dialer"),
            listOf("com.samsung.android.messaging", "com.google.android.apps.messaging"),
            listOf("com.sec.android.app.camera", "com.samsung.android.camera"),
            listOf("com.sec.android.app.sbrowser", "com.android.chrome", "com.google.android.apps.chrome")
        ).mapNotNull { alternatives ->
            alternatives.firstNotNullOfOrNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
        }
    }
    val visibleApps = remember(apps, search) {
        if (search.isBlank()) apps
        else apps.filter { it.label.contains(search.trim(), ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF263A55), Color(0xFF121B2B), Color(0xFF070A11))
                )
            )
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(26.dp))
            Text(
                clock,
                color = Color.White,
                fontSize = 58.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp
            )
            Text(
                today,
                color = Color(0xFFD5DEEF),
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 18.dp)
            )

            BasicTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0x664B5D78))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⌕", color = Color(0xFFDCE7F7), fontSize = 22.sp)
                        Box(Modifier.weight(1f).padding(start = 10.dp)) {
                            if (search.isEmpty()) {
                                Text("Search apps", color = Color(0xFFAEBBD0), fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                        if (search.isNotEmpty()) {
                            Text(
                                "×",
                                color = Color(0xFFDCE7F7),
                                fontSize = 20.sp,
                                modifier = Modifier.clickable { search = "" }
                            )
                        }
                    }
                }
            )

            Text(
                if (search.isBlank()) "Applications" else "Search results · ${visibleApps.size}",
                color = Color(0xFFDCE7F7),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
            )

            if (visibleApps.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No matching apps", color = Color(0xFFAEBBD0))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(visibleApps, key = { it.packageName }) { app ->
                        AppIcon(app = app, onClick = { onLaunch(app) })
                    }
                }
            }

            if (search.isBlank() && dockApps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x884F6688), Color(0xAA34445D), Color(0x884F6688))
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockApps.forEach { app ->
                        AppIcon(app = app, compact = true, onClick = { onLaunch(app) })
                    }
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
    val icon: ImageBitmap = remember(app.packageName) { drawableToImageBitmap(app.icon) }
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Image(
            bitmap = icon,
            contentDescription = app.label,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(if (compact) 52.dp else 58.dp)
                .clip(RoundedCornerShape(if (compact) 15.dp else 17.dp))
        )
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

private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
    val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val icon = drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate()
    icon.setBounds(8, 8, 120, 120)
    icon.draw(canvas)
    return bitmap.asImageBitmap()
}
