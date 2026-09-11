package dev.vibebridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
    val vm: AppViewModel by viewModels()
    val ui by vm.ui.collectAsState()
    var tab by remember { mutableStateOf(VbTab.HOME) }
    var showSettings by remember { mutableStateOf(false) }
    var showPush by remember { mutableStateOf(false) }
    val grabVm: GrabViewModel by viewModels()
    val wsVm: WorkspaceViewModel by viewModels()
    val pushVm: PushViewModel by viewModels()

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
        bottomBar = { VbBottomBar(current = tab, onSelect = { tab = it }) }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                VbTab.HOME -> HomeScreen(vm, goto = { tab = it }, openSettings = { showSettings = true })
                VbTab.COMPILER -> CompilerScreen(vm)
                VbTab.GRAB -> GrabScreen(grabVm, gotoPush = { showPush = true })
                VbTab.WORKSPACE -> WorkspaceScreen(wsVm, gotoGrab = { tab = VbTab.GRAB })
                VbTab.LIBRARY -> LibraryScreen(vm, goto = { tab = it })
            }
        }
    }
}
