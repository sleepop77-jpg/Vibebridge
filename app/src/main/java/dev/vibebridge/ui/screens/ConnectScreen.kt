package dev.vibebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.components.BounceDots
import dev.vibebridge.ui.components.CloudSky
import dev.vibebridge.ui.components.VbField
import dev.vibebridge.ui.components.VbWordmark
import dev.vibebridge.ui.theme.ButtonGreen
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint
import dev.vibebridge.ui.theme.VbType
import dev.vibebridge.ui.theme.Window
import dev.vibebridge.ui.theme.WindowBorder
import dev.vibebridge.viewmodel.AppViewModel

@Composable
fun ConnectScreen(vm: AppViewModel) {
    val ui by vm.ui.collectAsState()
    var pat by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf(vm.prefs.repo) }
    var branch by remember { mutableStateOf(vm.prefs.branch) }

    Box(modifier = Modifier.fillMaxSize()) {
        CloudSky(still = vm.prefs.stillSky)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 24.dp)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .background(Window, RoundedCornerShape(24.dp))
                    .border(1.dp, WindowBorder, RoundedCornerShape(24.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                VbWordmark(height = 26.dp, showTagline = true)
                Text(
                    "link one GitHub repository — the token stays encrypted on this device",
                    style = VbType.bodyMedium,
                    color = TextDim,
                    textAlign = TextAlign.Center
                )
                VbField(
                    label = "FINE-GRAINED PAT",
                    value = pat,
                    onValueChange = { pat = it },
                    placeholder = "github_pat_",
                    isPassword = true,
