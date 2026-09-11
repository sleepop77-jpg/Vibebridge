package dev.vibebridge.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val VbShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

val BubbleShape = RoundedCornerShape(16.dp)
val BubbleCornerUser = RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
val BubbleCornerBot = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
