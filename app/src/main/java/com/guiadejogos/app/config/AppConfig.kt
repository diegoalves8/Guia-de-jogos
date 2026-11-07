package com.guiadejogos.app.config

import com.guiadejogos.app.BuildConfig

/**
 * Configurações centrais do app. Mude aqui (ou em app/build.gradle.kts)
 * em vez de espalhar constantes pelo código.
 */
object AppConfig {
    /** Servidor de onde o app baixa conteúdo atualizado. Vazio = desativado. */
    val remoteContentBaseUrl: String = BuildConfig.REMOTE_CONTENT_BASE_URL

    /** Largura máxima do conteúdo em telas grandes (tablets), para não esticar demais. */
    const val MAX_CONTENT_WIDTH_DP = 900

    /** Largura mínima de cada card nas grades adaptáveis. */
    const val GRID_MIN_CELL_DP = 260
}
