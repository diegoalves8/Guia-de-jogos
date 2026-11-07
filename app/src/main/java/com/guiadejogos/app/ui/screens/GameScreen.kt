package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.model.ContentStatus
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.BigCard
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.HeaderBanner
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.theme.parseHexColor

/** Página do jogo: cabeçalho + botões grandes de categorias (definidas no JSON do jogo). */
@Composable
fun GameScreen(
    repository: ContentRepository,
    gameId: String,
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val state by rememberLoad(gameId, reloadKey) {
        repository.loadGameById(gameId) ?: error("Jogo não encontrado: $gameId")
    }
    val title = (state as? LoadState.Success)?.data?.name ?: ""

    AppScreen(title = title, onBack = onBack, maxContentWidthDp = AppConfig.MAX_GRID_WIDTH_DP) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> {
                val game = s.data
                val accent = parseHexColor(game.colorHex)
                LazyVerticalGrid(
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
                        HeaderBanner(game.emoji, game.name, game.description, accent)
                    }
                    items(game.categories, key = { it.id }) { category ->
                        BigCard(
                            emoji = category.emoji,
                            title = category.title,
                            subtitle = category.description,
                            accent = accent,
                            badge = if (category.status == ContentStatus.COMING_SOON) "EM BREVE" else null,
                            onClick = { onCategoryClick(category.id) },
                        )
                    }
                }
            }
        }
    }
}
