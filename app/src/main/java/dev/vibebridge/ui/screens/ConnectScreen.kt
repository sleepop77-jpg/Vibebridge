package dev.vibebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbButton
import dev.vibebridge.ui.components.VbField
import dev.vibebridge.ui.components.VbLoading
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.theme.AmberHi
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.viewmodel.AppViewModel

@Composable
fun ConnectScreen(vm: AppViewModel) {
    val ui by vm.ui.collectAsState()
    var pat by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf(vm.prefs.repo) }
    var branch by remember { mutableStateOf(vm.prefs.branch) }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Stagger(0) {
            Column {
                Text("CONNECT", style = MaterialTheme.typography.displaySmall, color = AmberHi)
                Text(
                    "Link one GitHub repository. The token is stored encrypted on this device and sent only to api.github.com.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim
                )
            }
        }
        Stagger(1) {
            VbPanel(title = "CREDENTIAL") {
                VbField(
                    label = "FINE-GRAINED PAT",
                    value = pat,
                    onValueChange = { pat = it },
                    placeholder = "github_pat_",
                    isPassword = true,
                    error = ui.validateError
                )
            }
        }
        Stagger(2) {
            VbPanel(title = "REPOSITORY") {
                VbField(
                    label = "OWNER / NAME",
                    value = repo,
                    onValueChange = { repo = it },
                    placeholder = "username/repo"
                )
                Spacer(Modifier.height(12.dp))
                VbField(
                    label = "DEFAULT BRANCH",
                    value = branch,
                    onValueChange = { branch = it },
                    placeholder = "main"
                )
            }
        }
        Stagger(3) {
            if (ui.validating) VbLoading("verifying token against api.github.com")
            else VbButton(
                text = "SAVE AND VERIFY",
                onClick = { vm.saveConnect(pat, repo, branch) },
                enabled = pat.isNotBlank() && repo.contains("/"),
                disabledReason = "Token and an owner/name repository are required."
            )
        }
        Stagger(4) {
            VbPanel(title = "SETUP STEPS") {
                Text("1. github.com settings, developer settings, fine-grained tokens.", style = MaterialTheme.typography.bodyMedium, color = TextDim)
                Text("2. Repository access: only the repository named above.", style = MaterialTheme.typography.bodyMedium, color = TextDim)
                Text("3. Permissions: Contents read and write. Then paste the token.", style = MaterialTheme.typography.bodyMedium, color = TextDim)
            }
        }
    }
}
