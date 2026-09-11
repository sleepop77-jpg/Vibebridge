package dev.vibebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbButtonGreen
import dev.vibebridge.ui.components.VbField
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbIconView
import dev.vibebridge.ui.components.VbLoading
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbWordmark
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.SurfaceHigh
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.VbMono
import dev.vibebridge.viewmodel.AppViewModel

@Composable
fun ConnectScreen(vm: AppViewModel) {
    val ui by vm.ui.collectAsState()
    var pat by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf(vm.prefs.repo) }
    var branch by remember { mutableStateOf(vm.prefs.branch) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Stagger(0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(SurfaceHigh, CircleShape)
                        .border(1.dp, Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    VbIconView(icon = VbIcon.LINK, color = Accent, size = 30.dp)
                }
                Box(contentAlignment = Alignment.Center) {
                    VbWordmark(height = 26.dp, showTagline = true)
                }
                Text(
                    "Link one GitHub repository. The token is stored encrypted on this device and sent only to api.github.com.",
                    color = TextDim,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Stagger(1) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                VbField(
                    label = "FINE-GRAINED PAT",
                    value = pat,
                    onValueChange = { pat = it },
                    placeholder = "github_pat_",
                    isPassword = true,
                    error = ui.validateError
                )
                VbField(
                    label = "OWNER / NAME",
                    value = repo,
                    onValueChange = { repo = it },
                    placeholder = "username/repo"
                )
                VbField(
                    label = "DEFAULT BRANCH",
                    value = branch,
                    onValueChange = { branch = it },
                    placeholder = "main"
                )
            }
        }
        Stagger(2) {
            if (ui.validating) {
                VbLoading("verifying token against api.github.com")
            } else {
                VbButtonGreen(
                    text = "CONNECT",
                    onClick = { vm.saveConnect(pat, repo, branch) },
                    enabled = pat.isNotBlank() && repo.contains("/"),
                    disabledReason = "Token and an owner/name repository are required.",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Stagger(3) {
            VbPanel(title = "SETUP STEPS", modifier = Modifier.fillMaxWidth()) {
                Text("1. github.com → settings → developer settings → fine-grained tokens.", style = VbMono.CodeSmall, color = TextDim)
                Text("2. Repository access: only the repository named above.", style = VbMono.CodeSmall, color = TextDim)
                Text("3. Permissions: Contents read and write. Then paste the token.", style = VbMono.CodeSmall, color = TextDim)
            }
        }
    }
}
