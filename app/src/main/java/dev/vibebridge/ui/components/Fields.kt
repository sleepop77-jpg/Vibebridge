package dev.vibebridge.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.theme.GhAccent
import dev.vibebridge.ui.theme.GhBorder
import dev.vibebridge.ui.theme.GhBorderMuted
import dev.vibebridge.ui.theme.GhBtnGreen
import dev.vibebridge.ui.theme.GhDangerBright
import dev.vibebridge.ui.theme.GhInset
import dev.vibebridge.ui.theme.GhTextDisabled
import dev.vibebridge.ui.theme.GhTextPrimary
import dev.vibebridge.ui.theme.GhTextSecondary
import dev.vibebridge.ui.theme.PureWhite

private val fieldShape = RoundedCornerShape(8.dp)

@Composable
fun VbField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = GhTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (error != null) GhDangerBright else GhBorder, fieldShape),
            placeholder = { Text(placeholder, color = GhTextDisabled) },
            singleLine = singleLine,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = fieldShape,
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GhInset,
                unfocusedContainerColor = GhInset,
                disabledContainerColor = GhInset,
                focusedTextColor = GhTextPrimary,
                unfocusedTextColor = GhTextPrimary,
                cursorColor = GhAccent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        if (error != null) {
            Text(
                error,
                style = MaterialTheme.typography.labelMedium,
                color = GhDangerBright,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }
    }
}

@Composable
fun VbToggle(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = GhTextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = GhBtnGreen,
                checkedThumbColor = PureWhite,
                uncheckedTrackColor = GhBorderMuted,
                uncheckedThumbColor = GhTextSecondary,
                uncheckedBorderColor = GhBorder
            )
        )
    }
}
