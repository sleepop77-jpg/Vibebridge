package dev.vibebridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.core.Prefs
import dev.vibebridge.core.Workspace
import dev.vibebridge.ui.CompilerScreen
import dev.vibebridge.ui.ConnectScreen
import dev.vibebridge.ui.GrabScreen
import dev.vibebridge.ui.HomeScreen
import dev.vibebridge.ui.LibraryScreen
import dev.vibebridge.ui.WorkspaceScreen
import dev.vibebridge.ui.theme.VbAmber
import dev.vibebridge.ui.theme.VbBg
import dev.vibebridge.ui.theme.VbDim
import dev.vibebridge.ui.theme.VbTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { VbTheme { Root() } }
    }
}

private val TABS = listOf("HOME", "COMPILER", "GRAB", "SPACE", "LIBRARY")

@Composable
fun Root() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val workspace = remember { Workspace(File(ctx.filesDir, "workspace")) }
    var onboarded by remember { mutableStateOf(prefs.onboarded) }
    var tab by remember { mutableStateOf("HOME") }

    if (!onboarded) {
        ConnectScreen(prefs) { onboarded = true }
        return
    }
    Scaffold(
        containerColor = VbBg,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(VbBg).padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TABS.forEach { t ->
                    val sel = t == tab
                    Box(
                        modifier = Modifier
                            .clickable { tab = t }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            t,
                            color = if (sel) VbAmber else VbDim,
                            fontSize = 11.sp,
                                                        fontWeight = if (sel) FontWeight.Black else FontWeight.Normal
                        )
                    }
                }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (tab) {
                "HOME" -> HomeScreen(workspace, prefs) { tab = it }
                "COMPILER" -> CompilerScreen()
                "GRAB" -> GrabScreen(workspace)
                "SPACE" -> WorkspaceScreen(workspace)
                else -> LibraryScreen()
            }
        }
    }
}