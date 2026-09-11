package dev.vibebridge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val sans = FontFamily.Default
private val mono = FontFamily.Monospace

val VbType = Typography(
    displaySmall = TextStyle(fontFamily = sans, fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = sans, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = sans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontFamily = sans, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = sans, fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = sans, fontSize = 13.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontFamily = sans, fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontFamily = sans, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily = sans, fontSize = 11.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = sans, fontSize = 10.sp, fontWeight = FontWeight.Medium)
)

object VbMono {
    val Code = TextStyle(fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp)
    val CodeSmall = TextStyle(fontFamily = mono, fontSize = 11.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)
    val Label = TextStyle(fontFamily = mono, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
    val Stat = TextStyle(fontFamily = mono, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
}
