package dev.vibebridge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val mono = FontFamily.Monospace

val VbType = Typography(
    displaySmall = TextStyle(fontFamily = mono, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp),
    titleLarge = TextStyle(fontFamily = mono, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
    titleMedium = TextStyle(fontFamily = mono, fontSize = 15.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontFamily = mono, fontSize = 14.sp, fontWeight = FontWeight.Medium),
    bodyMedium = TextStyle(fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontFamily = mono, fontSize = 11.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = mono, fontSize = 10.sp, fontWeight = FontWeight.Medium)
)
