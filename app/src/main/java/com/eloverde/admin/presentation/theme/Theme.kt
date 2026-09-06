package com.eloverde.admin.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Forest = Color(0xFF176B4A)
val ForestDark = Color(0xFF0D5037)
val Leaf = Color(0xFF27A875)
val Mint = Color(0xFFDDF3E8)
val WarmBackground = Color(0xFFF6F3EE)
val WarmSurface = Color(0xFFFFFBF7)
val Stone = Color(0xFF292524)
val StoneMuted = Color(0xFF78716C)
val AmberSoft = Color(0xFFFFEDC2)
val BlueSoft = Color(0xFFDCEAF8)

private val colors = lightColorScheme(
    primary = Forest, onPrimary = Color.White, primaryContainer = Mint,
    onPrimaryContainer = ForestDark, secondary = Leaf, onSecondary = Color.White,
    background = WarmBackground, onBackground = Stone, surface = WarmSurface,
    onSurface = Stone, surfaceVariant = Color(0xFFEDE8E1), onSurfaceVariant = StoneMuted,
    outline = Color(0xFFD6D3D1), error = Color(0xFFB3261E)
)

private val typography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
)

@Composable fun EloVerdeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
