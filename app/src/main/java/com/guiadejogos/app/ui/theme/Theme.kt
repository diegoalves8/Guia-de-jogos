package com.guiadejogos.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta "gamer": fundo escuro arroxeado com destaques neon.
object AppColors {
    val Background = Color(0xFF0E0B1A)
    val Surface = Color(0xFF1A1530)
    val SurfaceHigh = Color(0xFF251E42)
    val Primary = Color(0xFF8B5CF6)
    val Secondary = Color(0xFF22D3EE)
    val Accent = Color(0xFFFACC15)
    val Success = Color(0xFF22C55E)
    val Danger = Color(0xFFEF4444)
    val TextPrimary = Color(0xFFF5F3FF)
    val TextSecondary = Color(0xFFB8B0D9)
}

private val ColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    secondary = AppColors.Secondary,
    onSecondary = Color.Black,
    tertiary = AppColors.Accent,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceHigh,
    onSurfaceVariant = AppColors.TextSecondary,
    error = AppColors.Danger,
)

// Títulos grandes e textos legíveis em celular e tablet.
private val AppTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 44.sp, lineHeight = 50.sp, letterSpacing = 1.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp),
)

@Composable
fun GuiaDeJogosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, typography = AppTypography, content = content)
}

/** Converte "#RRGGBB" do JSON em Color (com fallback para a cor primária). */
fun parseHexColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(AppColors.Primary)
