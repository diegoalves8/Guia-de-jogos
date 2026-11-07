package com.guiadejogos.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Design system do Guia de Jogos.
 * Direção: "Play Store redesenhada para a multimídia do carro" — escuro, elegante,
 * imagens em destaque, um único azul de destaque e violeta como apoio.
 */
object AppColors {
    val Background = Color(0xFF10141D)   // fundo principal (azul-marinho/grafite)
    val Surface = Color(0xFF1B2230)      // cartões e superfícies
    val SurfaceHigh = Color(0xFF242C3D)  // superfícies elevadas, campos, chips
    val Outline = Color(0xFF2E3748)      // divisórias e contornos discretos
    val Primary = Color(0xFF4285F4)      // azul de destaque
    val Secondary = Color(0xFF8B7CF6)    // violeta suave
    val Success = Color(0xFF5CC593)      // verde suave
    val Accent = Color(0xFFE8B75C)       // âmbar discreto (alertas)
    val Danger = Color(0xFFE5737A)
    val TextPrimary = Color(0xFFF5F7FA)
    val TextSecondary = Color(0xFFA5AFBF)

    /** Paleta para gráficos e indicadores por jogo (sem excesso de laranja). */
    val Chart = listOf(Primary, Secondary, Color(0xFF3CC6C2), Success, Accent, Color(0xFF6FA8FF))
}

private val ColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A6B),
    onPrimaryContainer = Color(0xFFD8E5FF),
    secondary = AppColors.Secondary,
    onSecondary = Color.White,
    tertiary = AppColors.Accent,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceHigh,
    onSurfaceVariant = AppColors.TextSecondary,
    surfaceContainer = AppColors.Surface,
    surfaceContainerHigh = AppColors.SurfaceHigh,
    outline = AppColors.Outline,
    outlineVariant = AppColors.Outline,
    error = AppColors.Danger,
)

/** Tipografia legível à distância (multimídia do carro): nada abaixo de 14sp. */
private val AppTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.25).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.2.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Raios e espaçamentos padrão — use estes valores em vez de números soltos. */
object Dimens {
    val CardRadius = 20.dp
    val BannerRadius = 28.dp
    val ScreenPadding = 24.dp
    val Gap = 16.dp
    val TouchMin = 56.dp
}

@Composable
fun GuiaDeJogosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, typography = AppTypography, shapes = AppShapes, content = content)
}

/** Converte "#RRGGBB" do JSON em Color (com fallback para a cor primária). */
fun parseHexColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(AppColors.Primary)
