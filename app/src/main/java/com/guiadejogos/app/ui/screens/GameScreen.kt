package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.model.GameDetail
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.CategoryTile
import com.guiadejogos.app.ui.components.Chip
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.parseHexColor

/** Página do jogo: capa em destaque + grade de categorias. */
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
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp,
                        top = padding.calculateTopPadding() + 4.dp,
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) { GameHero(game, accent) }
                    items(game.categories, key = { it.id }) { category ->
                        CategoryTile(category, accent) { onCategoryClick(category.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameHero(game: GameDetail, accent: Color) {
    val short = rememberScreenInfo().isShortHeight
    val characters = game.categories.flatMap { it.sections }.filterIsInstance<Section.Toons>().sumOf { it.toons.size }
    val codes = game.categories.flatMap { it.sections }.filterIsInstance<Section.Codes>().sumOf { it.codes.size }
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (short) 170.dp else 230.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.6f), AppColors.Surface))),
    ) {
        if (game.cover.isNotBlank()) {
            AsyncImage(
                model = game.cover, contentDescription = null, contentScale = ContentScale.Fit,
                alignment = Alignment.CenterEnd,
                modifier = Modifier.fillMaxSize().padding(start = 120.dp, top = 10.dp, bottom = 10.dp, end = 10.dp),
            )
        }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(AppColors.Background.copy(alpha = 0.85f), Color.Transparent))))
        Column(Modifier.align(Alignment.CenterStart).padding(22.dp).fillMaxWidth(0.7f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${game.emoji} ${game.name}", style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
            if (!short) Text(game.description, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, maxLines = 3)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("${game.categories.size} categorias", accent)
                if (characters > 0) Chip("$characters personagens", AppColors.Secondary)
                if (codes > 0) Chip("$codes códigos", AppColors.Success)
            }
        }
    }
}
