package dev.vibebridge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.vibebridge.ui.components.CloudSky
import dev.vibebridge.ui.components.VbTab
import dev.vibebridge.ui.components.WindowNav
import dev.vibebridge.ui.screens.ChatScreen
import dev.vibebridge.ui.screens.ConnectScreen
import dev.vibebridge.ui.screens.LibraryScreen
import dev.vibebridge.ui.screens.SettingsScreen
import dev.vibebridge.ui.screens.WorkspaceScreen
import dev.vibebridge.ui.theme.VbTheme
import dev.vibebridge.ui.theme.Window
import dev.vibebridge.ui.theme.WindowBorder
import dev.vibebridge.viewmodel.AppViewModel
import dev.vibebridge.viewmodel.ChatViewModel
import dev.vibebridge.viewmodel.WorkspaceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { VbTheme { Root() } }
    }
}

@Composable
fun Root() {
    val ctx = LocalContext.current
    val vm: AppViewModel = viewModel<AppViewModel>()
    val chatVm: ChatViewModel = viewModel<ChatViewModel>()
    val wsVm: WorkspaceViewModel = viewModel<WorkspaceViewModel>()
    val ui by vm.ui.collectAsState()
    var tab by remember { mutableStateOf(VbTab.CHAT) }
    var showSettings by remember { mutableStateOf(false) }
    var hintShown by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showSettings -> showSettings = false
            !hintShown -> {
                hintShown = true
                Toast.makeText(ctx, "Start of stack. Use the home key to leave VibeBridge.", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (!ui.onboarded) {
        ConnectScreen(vm)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CloudSky(still = vm.prefs.stillSky)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Window)
                    .border(1.dp, WindowBorder, RoundedCornerShape(24.dp))
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        showSettings -> SettingsScreen(vm) { showSettings = false }
                        else -> when (tab) {
                            VbTab.CHAT -> ChatScreen(chatVm, openSettings = { showSettings = true })
                            VbTab.FILES -> WorkspaceScreen(wsVm, gotoChat = { tab = VbTab.CHAT })
                            VbTab.LIBRARY -> LibraryScreen(vm, goto = { tab = it })
                        }
                    }
                }
                if (!showSettings) {
                    WindowNav(current = tab, onSelect = { tab = it })
                }
            }
        }
    }
}
