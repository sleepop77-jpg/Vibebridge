package dev.vibebridge.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.core.DeviceCode
import dev.vibebridge.core.GitHubAuth
import dev.vibebridge.core.VbResult
import dev.vibebridge.ui.components.BounceDots
import dev.vibebridge.ui.components.RepoPickerSheet
import dev.vibebridge.ui.components.VbButtonGreen
import dev.vibebridge.ui.components.VbButtonSecondary
import dev.vibebridge.ui.components.VbField
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbWordmark
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Danger
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint
import dev.vibebridge.ui.theme.VbType
import dev.vibebridge.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun ConnectScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    var pat by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf(vm.prefs.repo) }
    var branch by remember { mutableStateOf(vm.prefs.branch) }
    var clientId by remember { mutableStateOf(vm.prefs.clientId) }
    var device by remember { mutableStateOf<DeviceCode?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    var startTick by remember { mutableStateOf(0) }
    var showPicker by remember { mutableStateOf(false) }
    var pickerToken by remember { mutableStateOf("") }
    val tokenNow = pat.ifBlank { vm.secure.pat }

    LaunchedEffect(startTick) {
        if (startTick == 0) return@LaunchedEffect
        authError = null
        when (val r = GitHubAuth.startDeviceFlow(clientId.trim())) {
            is VbResult.Ok -> {
                vm.prefs.clientId = clientId.trim()
                device = r.value
                runCatching {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.value.verificationUri)))
                }
            }
            is VbResult.Err -> authError = r.message
        }
    }

    LaunchedEffect(device) {
        val d = device ?: return@LaunchedEffect
        while (true) {
            delay(d.intervalSec * 1000L)
            when (val r = GitHubAuth.pollToken(clientId.trim(), d.deviceCode)) {
                is GitHubAuth.PollResult.Token -> {
                    vm.secure.pat = r.token
                    vm.prefs.tokenKind = "oauth"
                    device = null
                    pickerToken = r.token
                    showPicker = true
                    return@LaunchedEffect
                }
                is GitHubAuth.PollResult.Failed -> {
                    authError = r.message
                    device = null
                    return@LaunchedEffect
                }
                is GitHubAuth.PollResult.Pending -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
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

        VbPanel(title = "LOGIN WITH GITHUB", modifier = Modifier.fillMaxWidth()) {
            VbField(
                label = "OAUTH APP CLIENT ID",
                value = clientId,
                onValueChange = { clientId = it },
                placeholder = "one-time: github.com → settings → developer settings → oauth apps"
            )
            Spacer(Modifier.height(10.dp))
            VbButtonSecondary(
                text = if (device == null) "START GITHUB LOGIN" else "WAITING FOR APPROVAL…",
                onClick = { startTick++ },
                enabled = clientId.isNotBlank() && device == null,
                modifier = Modifier.fillMaxWidth()
            )
            device?.let { d ->
                Spacer(Modifier.height(12.dp))
                Text("enter this code in the browser window:", color = TextDim, fontSize = 11.sp)
                Text(
                    d.userCode,
                    color = Text,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Spacer(Modifier.height(6.dp))
                BounceDots()
                Spacer(Modifier.height(8.dp))
                VbButtonSecondary(text = "CANCEL", onClick = { device = null }, modifier = Modifier.fillMaxWidth())
            }
            authError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Danger, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "one-time setup: create an OAuth App (homepage url can be anything), enable Device Flow in its settings, paste the client id here. no secret needed — safe for a phone app.",
                color = TextFaint,
                fontSize = 10.sp
            )
        }

        VbPanel(title = "OR USE A PAT", modifier = Modifier.fillMaxWidth()) {
            VbField(
                label = "FINE-GRAINED PAT",
                value = pat,
                onValueChange = { pat = it },
                placeholder = "github_pat_",
                isPassword = true,
                error = ui.validateError
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VbField(
                    label = "OWNER / NAME",
                    value = repo,
                    onValueChange = { repo = it },
                    placeholder = "username/repo",
                    modifier = Modifier.weight(1f)
                )
                VbButtonSecondary(
                    text = "BROWSE",
                    onClick = {
                        pickerToken = tokenNow
                        showPicker = true
                    },
                    enabled = tokenNow.isNotBlank()
                )
            }
            Spacer(Modifier.height(10.dp))
            VbField(
                label = "DEFAULT BRANCH",
                value = branch,
                onValueChange = { branch = it },
                placeholder = "main"
            )
            Spacer(Modifier.height(10.dp))
            VbButtonGreen(
                text = "CONNECT",
                onClick = { vm.saveConnect(pat, repo, branch) },
                enabled = pat.isNotBlank() && repo.contains("/"),
                disabledReason = "Token and an owner/name repository are required.",
                modifier = Modifier.fillMaxWidth()
            )
            if (vm.secure.pat.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                VbButtonSecondary(
                    text = "CHANGE REPOSITORY",
                    onClick = {
                        pickerToken = vm.secure.pat
                        showPicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (ui.validating) {
            BounceDots()
        }
    }

    if (showPicker) {
        RepoPickerSheet(
            token = pickerToken,
            current = repo,
            onPick = { picked ->
                repo = picked
                vm.prefs.repo = picked
                showPicker = false
                if (vm.secure.pat.isNotBlank() && vm.prefs.branch.isNotBlank()) {
                    vm.refresh()
                }
            },
            onDismiss = { showPicker = false }
        )
    }
}
