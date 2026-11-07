package com.guiadejogos.app.ui.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Informações da tela atual, usadas para adaptar o layout a:
 *  - celular (retrato),
 *  - tablet (Galaxy Tab S6 Lite: retrato e paisagem),
 *  - multimídia de carro (BYD: tela larga e baixa em paisagem, ou giratória).
 *
 * Recalcula sozinho quando a tela gira (o app não reinicia).
 */
data class ScreenInfo(
    val widthDp: Int,
    val heightDp: Int,
    val isLandscape: Boolean,
) {
    /** Tela baixa (ex.: multimídia em paisagem): usa versões compactas dos cabeçalhos. */
    val isShortHeight: Boolean get() = heightDp < 600

    /** Paisagem larga e baixa: tela inicial em duas colunas. */
    val isWideLandscape: Boolean get() = isLandscape && isShortHeight && widthDp >= 640
}

@Composable
fun rememberScreenInfo(): ScreenInfo {
    val config = LocalConfiguration.current
    return ScreenInfo(
        widthDp = config.screenWidthDp,
        heightDp = config.screenHeightDp,
        isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE,
    )
}
