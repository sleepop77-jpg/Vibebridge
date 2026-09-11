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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.vibebridge.ui.components.AppMode
import dev.vibebridge.ui.components.ModeToggle
import dev.vibebridge.ui.components.VbBottomBar
import dev.vibebridge.ui.components.VbTab
import dev.vibebridge.ui.screens.CompilerScreen
import dev.vibebridge.ui.screens.ConnectScreen
import dev.vibebridge.ui.screens.GrabScreen
import dev.vibebridge.ui.screens.HomeScreen
import dev.vibebridge.ui.screens.LibraryScreen
import dev.vibebridge.ui.screens.PushScreen
import dev.vibebridge.ui.screens.SettingsScreen
import dev.vibebridge.ui.screens.WorkspaceScreen
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.VbTheme
import dev.vibebridge.viewmodel.AppViewModel
import dev.vibebridge.viewmodel.GrabViewModel
import dev.vibebridge.viewmodel.PushViewModel
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
    val ui by vm.ui.collectAsState()
    var tab by remember { mutableStateOf(VbTab.HOME) }
    var appMode by remember { mutableStateOf(AppMode.WORK) }
    var showSettings by remember { mutableStateOf(false) }
    var showPush by remember { mutableStateOf(false) }
    var hintShown by remember { mutableStateOf(false) }
    val grabVm: GrabViewModel = viewModel<GrabViewModel>()
    val wsVm: WorkspaceViewModel = viewModel<WorkspaceViewModel>()
    val pushVm: PushViewModel = viewModel<PushViewModel>()

    BackHandler {
        when {
            showPush -> showPush = false
            showSettings -> showSettings = false
            tab != VbTab.HOME -> tab = VbTab.HOME
            !hintShown -> {
                hintShown = true
                Toast.makeText(ctx, "Start of stack. Use the home key to leave VibeBridge.", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(tab, showSettings, showPush) { vm.reloadLocal() }

    if (!ui.onboarded) {
        ConnectScreen(vm)
        return
    }
    if (showSettings) {
        SettingsScreen(vm) { showSettings = false }
        return
    }
    if (showPush) {
        PushScreen(pushVm) { showPush = false }
        return
    }
    Scaffold(
        containerColor = Bg,
        topBar = { ModeToggle(currentMode = appMode, onModeChange = { appMode = it }) },
        bottomBar = { VbBottomBar(current = tab, onSelect = { tab = it }) }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                VbTab.HOME -> HomeScreen(vm, appMode, goto = { tab = it }, openSettings = { showSettings = true })
                VbTab.COMPILER -> CompilerScreen(vm)
                VbTab.GRAB -> GrabScreen(grabVm, gotoPush = { showPush = true }, gotoHome = { tab = VbTab.HOME })
                VbTab.WORKSPACE -> WorkspaceScreen(wsVm, gotoGrab = { tab = VbTab.GRAB })
                VbTab.LIBRARY -> LibraryScreen(vm, goto = { tab = it })
            }
        }
    }
}
