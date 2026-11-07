package com.guiadejogos.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState

/** Estado genérico de carregamento usado pelas telas. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}

/**
 * Carrega dados de forma assíncrona. Mudar [key] (ex.: um contador de "tentar de novo"
 * ou após atualizar conteúdo) recarrega.
 */
@Composable
fun <T> rememberLoad(vararg key: Any?, loader: suspend () -> T): State<LoadState<T>> =
    produceState<LoadState<T>>(initialValue = LoadState.Loading, *key) {
        value = LoadState.Loading
        value = runCatching { loader() }.fold(
            onSuccess = { LoadState.Success(it) },
            onFailure = { LoadState.Error(it.message ?: "Erro desconhecido") },
        )
    }
