package dev.vibebridge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.vibebridge.ui.components.VbBottomBar
import dev.vibebridge.ui.components.VbTab
import dev.vibebridge.ui.screens.ChatScreen
import dev.vibebridge.ui.screens.ConnectScreen
import dev.vibebridge.ui.screens.LibraryScreen
import dev.vibebridge.ui.screens.SettingsScreen
import dev.vibebridge.ui.screens.WorkspaceScreen
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.VbTheme
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
    if (showSettings) {
        SettingsScreen(vm) { showSettings = false }
        return
    }
    Scaffold(
        containerColor = Bg,
        bottomBar = { VbBottomBar(current = tab, onSelect = { tab = it }) }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                VbTab.CHAT -> ChatScreen(chatVm, openSettings = { showSettings = true })
                VbTab.FILES -> WorkspaceScreen(wsVm, gotoChat = { tab = VbTab.CHAT })
                VbTab.LIBRARY -> LibraryScreen(vm, goto = { tab = it })
            }
        }
    }
}
