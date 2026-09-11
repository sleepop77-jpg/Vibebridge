package dev.vibebridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.Danger
import dev.vibebridge.ui.theme.Inset
import dev.vibebridge.ui.theme.SurfaceHigh
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint
import dev.vibebridge.ui.theme.VbMono

private val fieldShape = RoundedCornerShape(10.dp)

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
        Text(label, style = VbMono.Label, color = TextDim, modifier = Modifier.padding(bottom = 6.dp, start = 2.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (error != null) Danger else Border, fieldShape),
            placeholder = { Text(placeholder, color = TextFaint, style = VbMono.Code) },
            singleLine = singleLine,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = fieldShape,
            textStyle = VbMono.Code,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Inset,
                unfocusedContainerColor = Inset,
                disabledContainerColor = Inset,
                focusedTextColor = Text,
                unfocusedTextColor = Text,
                cursorColor = Accent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        if (error != null) {
            Text(error, color = Danger, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 2.dp))
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Text, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Accent,
                checkedThumbColor = Text,
                uncheckedTrackColor = SurfaceHigh,
                uncheckedThumbColor = TextDim,
                uncheckedBorderColor = Border
            )
        )
    }
}
