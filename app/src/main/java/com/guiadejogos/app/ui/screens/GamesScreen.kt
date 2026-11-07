package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.EmptyState
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.FilterChipRow
import com.guiadejogos.app.ui.components.GameRow
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.Pill
import com.guiadejogos.app.ui.components.SearchField
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens

/** Aba Jogos: catálogo completo com pesquisa e filtros. */
@Composable
fun GamesScreen(
    repository: ContentRepository,
    onGameClick: (String) -> Unit,
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val state by rememberLoad(reloadKey) { repository.loadIndex() }
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(GameFilters.ALL) }

    AppScreen(title = "Jogos", onBack = null, inShell = true, maxContentWidthDp = AppConfig.MAX_GRID_WIDTH_DP) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> {
                val list = GameFilters.apply(s.data.games, filter).filter { g ->
                    query.isBlank() || listOf(g.name, g.developer, g.subtitle, g.genres.joinToString()).any { it.contains(query, ignoreCase = true) }
                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 420.dp),
                    contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = padding.calculateTopPadding() + 4.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Gap),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Gap),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SearchField(query, { query = it }, "Pesquisar no catálogo", Modifier.fillMaxWidth())
                            FilterChipRow(GameFilters.options, filter, onSelect = { filter = it })
                        }
                    }
                    if (list.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyState("🔎", "Nada encontrado", "Tente outra palavra ou outra categoria.")
                        }
                    }
                    items(list, key = { it.id }) { game ->
                        GameRow(game, trailing = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (game.stats.characters > 0) Pill("${game.stats.characters} ${game.stats.label}", AppColors.Primary)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = AppColors.TextSecondary)
                            }
                        }) { onGameClick(game.id) }
                    }
                }
            }
        }
    }
}
