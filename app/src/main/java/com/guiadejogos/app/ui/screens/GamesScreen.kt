package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.model.ContentStatus
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.BigCard
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.parseHexColor
import kotlinx.coroutines.launch

/** Tela "Escolha o jogo". Grade adaptável: 1 coluna no celular, 2–3 no tablet. */
@Composable
fun GamesScreen(
    repository: ContentRepository,
    onBack: () -> Unit,
    onGameClick: (String) -> Unit,
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val state by rememberLoad(reloadKey) { repository.loadIndex() }

    AppScreen(
        title = "Escolha o jogo",
        onBack = onBack,
        maxContentWidthDp = AppConfig.MAX_GRID_WIDTH_DP,
        actions = {
            if (refreshing) {
                CircularProgressIndicator(Modifier.padding(12.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = {
                    refreshing = true
                    scope.launch {
                        val updated = repository.refresh()
                        refreshing = false
                        if (updated) reloadKey++
                        snackbar.showSnackbar(
                            if (updated) "Conteúdo atualizado!" else "Você já tem o conteúdo mais recente.",
                        )
                    }
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Atualizar conteúdo")
                }
            }
        },
    ) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = AppConfig.GRID_MIN_CELL_DP.dp),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Toque em um jogo para ver códigos, guias, dicas e novidades.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.TextSecondary,
                    )
                }
                items(s.data.games, key = { it.id }) { game ->
                    val available = game.status == ContentStatus.AVAILABLE
                    BigCard(
                        emoji = game.emoji,
                        title = game.name,
                        subtitle = listOf(game.subtitle, game.platform).filter { it.isNotBlank() }.joinToString(" • "),
                        accent = parseHexColor(game.colorHex),
                        badge = if (available) null else "EM BREVE",
                        onClick = { onGameClick(game.id) },
                    )
                }
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(snackbar)
        }
    }
}
