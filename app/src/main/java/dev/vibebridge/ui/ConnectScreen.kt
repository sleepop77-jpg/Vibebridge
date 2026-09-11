package dev.vibebridge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.vibebridge.core.Prefs
import dev.vibebridge.ui.theme.VbAmber
import dev.vibebridge.ui.theme.VbBg
import dev.vibebridge.ui.theme.VbBorder
import dev.vibebridge.ui.theme.VbCard
import dev.vibebridge.ui.theme.VbDim
import dev.vibebridge.ui.theme.VbYellow

@Composable
fun ConnectScreen(prefs: Prefs, onDone: () -> Unit) {
    var pat by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("main") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VbBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("CONNECT", style = MaterialHeadline())
        Text(
            "VibeBridge never phones home. Your PAT stays on this device and talks only to api.github.com. Use a FINE-GRAINED PAT scoped to ONE repo (Contents: read+write).",
            color = VbDim,
            fontSize = androidx.compose.ui.unit.TextUnit.Companion.sp(11)
        )
        CardBox {
            Text("1. PASTE FINE-GRAINED PAT", color = VbDim, fontSize = androidx.compose.ui.unit.TextUnit.Companion.sp(11))
            Spacer(Modifier.height(6.dp))
            TextField(
                value = pat,
                onValueChange = { pat = it.trim() },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = FieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        CardBox {
            Text("2. REPO (owner/name)", color = VbDim, fontSize = androidx.compose.ui.unit.TextUnit.Companion.sp(11))
            Spacer(Modifier.height(6.dp))
            TextField(
                value = repo,
                onValueChange = { repo = it.trim() },
                singleLine = true,
                colors = FieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Text("3. DEFAULT BRANCH", color = VbDim, fontSize = androidx.compose.ui.unit.TextUnit.Companion.sp(11))
            Spacer(Modifier.height(6.dp))
            TextField(
                value = branch,
                onValueChange = { branch = it },
                singleLine = true,
                colors = FieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(
            onClick = {
                prefs.pat = pat
                prefs.repo = repo
                prefs.branch = branch.ifBlank { "main" }
                onDone()
            },
            enabled = pat.isNotBlank() && repo.contains("/"),
            modifier = Modifier.fillMaxWidth()
        ) { Text("SAVE & SYNC", fontWeight = FontWeight.Black) }
        Text(
            "Push validation, repo picker and CI status arrive in Package 2. For now this just stores your config locally.",
            color = VbDim,
            fontSize = androidx.compose.ui.unit.TextUnit.Companion.sp(10)
        )
    }
}

@Composable
private fun MaterialHeadline() = androidx.compose.material3.MaterialTheme.typography.headlineSmall

@Composable
private fun CardBox(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VbCard, RoundedCornerShape(8.dp))
            .border(1.dp, VbBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) { content() }
}

@Composable
private fun FieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = VbBg,
    unfocusedContainerColor = VbBg,
    focusedIndicatorColor = VbAmber,
    unfocusedIndicatorColor = VbBorder,
    focusedTextColor = VbYellow,
    unfocusedTextColor = VbYellow,
    cursorColor = VbAmber
)
