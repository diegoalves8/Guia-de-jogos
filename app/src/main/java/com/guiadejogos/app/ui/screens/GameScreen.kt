package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.model.GameDetail
import com.guiadejogos.app.data.model.GameSummary
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.data.repository.FavoritesStore
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.CategoryTile
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.GameBadges
import com.guiadejogos.app.ui.components.GameIcon
import com.guiadejogos.app.ui.components.IconBubble
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.MediaCard
import com.guiadejogos.app.ui.components.Pill
import com.guiadejogos.app.ui.components.PrimaryButton
import com.guiadejogos.app.ui.components.SecondaryButton
import com.guiadejogos.app.ui.components.SectionHeader
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens

/**
 * Página padronizada de cada jogo: banner, ícone, nome, desenvolvedor, categorias,
 * descrição, botão da loja, compatibilidade, seções e prévia de personagens.
 */
@Composable
fun GameScreen(
    repository: ContentRepository,
    favorites: FavoritesStore,
    gameId: String,
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onToonClick: (categoryId: String, toonId: String) -> Unit,
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val state by rememberLoad(gameId, reloadKey) {
        val summary = repository.loadIndex().games.firstOrNull { it.id == gameId } ?: error("Jogo não encontrado: $gameId")
        summary to repository.loadGame(summary.file)
    }
    val title = (state as? LoadState.Success)?.data?.first?.name ?: ""

    AppScreen(title = title, onBack = onBack, maxContentWidthDp = AppConfig.MAX_GRID_WIDTH_DP) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> GameContent(s.data.first, s.data.second, favorites, padding, onCategoryClick, onToonClick)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameContent(
    summary: GameSummary,
    game: GameDetail,
    favorites: FavoritesStore,
    padding: PaddingValues,
    onCategoryClick: (String) -> Unit,
    onToonClick: (String, String) -> Unit,
) {
    val screen = rememberScreenInfo()
    val wide = screen.widthDp >= 760
    val uri = LocalUriHandler.current
    val favKey = FavoritesStore.gameKey(summary.id)
    val isFav = favorites.isFavorite(favKey)
    val toonsCategory = game.categories.firstOrNull { c -> c.sections.any { it is Section.Toons } }
    val previewToons = toonsCategory?.sections?.filterIsInstance<Section.Toons>()?.flatMap { it.toons }?.take(14).orEmpty()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = padding.calculateTopPadding() + 4.dp, bottom = padding.calculateBottomPadding() + 28.dp),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Gap),
        verticalArrangement = Arrangement.spacedBy(Dimens.Gap),
        modifier = Modifier.fillMaxSize(),
    ) {
        // Banner
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                Modifier.fillMaxWidth().height(if (screen.isShortHeight) 200.dp else 260.dp)
                    .clip(RoundedCornerShape(Dimens.BannerRadius))
                    .background(Brush.linearGradient(listOf(Color(0xFF1C2A44), AppColors.Surface, Color(0xFF231E3D)))),
            ) {
                if (game.cover.isNotBlank() || summary.cover.isNotBlank()) {
                    AsyncImage(
                        model = summary.cover.ifBlank { game.cover }, contentDescription = null, contentScale = ContentScale.Fit, alignment = Alignment.CenterEnd,
                        modifier = Modifier.fillMaxSize().padding(start = 200.dp, top = 12.dp, bottom = 12.dp, end = 24.dp),
                    )
                }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to AppColors.Background.copy(alpha = 0.9f), 0.6f to Color.Transparent)))
                Row(Modifier.align(Alignment.BottomStart).padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    GameIcon(summary.cover, summary.emoji, 88.dp)
                    Spacer(Modifier.width(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(summary.name, style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
                        Text(summary.developer, style = MaterialTheme.typography.titleMedium, color = AppColors.Primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            summary.genres.forEach { Pill(it, AppColors.TextSecondary) }
                            GameBadges(summary)
                        }
                    }
                }
            }
        }

        // Ações + descrição + compatibilidade
        item(span = { GridItemSpan(maxLineSpan) }) {
            val actions: @Composable () -> Unit = {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (summary.storeUrl.isNotBlank()) {
                        PrimaryButton(summary.storeLabel.ifBlank { "Abrir o jogo" }, Icons.AutoMirrored.Filled.OpenInNew) { runCatching { uri.openUri(summary.storeUrl) } }
                    }
                    SecondaryButton(if (isFav) "Nos favoritos" else "Favoritar", if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder) {
                        favorites.toggle(FavoritesStore.Favorite(favKey, summary.name, summary.developer, summary.cover, "game/${summary.id}", "game"))
                    }
                }
            }
            val about: @Composable (Modifier) -> Unit = { m ->
                Column(m.background(AppColors.Surface, RoundedCornerShape(Dimens.CardRadius)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sobre o jogo", style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
                    Text(summary.longDescription.ifBlank { game.description }, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
                }
            }
            val compat: @Composable (Modifier) -> Unit = { m ->
                Column(m.background(AppColors.Surface, RoundedCornerShape(Dimens.CardRadius)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(Icons.Filled.PhoneAndroid)
                        Spacer(Modifier.width(14.dp))
                        Text("Compatibilidade", style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
                    }
                    Text(summary.compatibility.ifBlank { summary.platform }, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
                    Text("Este guia funciona no celular, no tablet e na multimídia do carro.", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.Gap)) {
                actions()
                if (wide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Gap)) { about(Modifier.weight(1.6f)); compat(Modifier.weight(1f)) }
                } else {
                    about(Modifier.fillMaxWidth()); compat(Modifier.fillMaxWidth())
                }
            }
        }

        // Prévia de personagens
        if (toonsCategory != null && previewToons.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionHeader(toonsCategory.title, toonsCategory.description, action = "Ver tudo", onAction = { onCategoryClick(toonsCategory.id) })
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.Gap)) {
                        items(previewToons, key = { it.id }) { t ->
                            MediaCard(t.image, t.name, badge = t.kind.substringBefore(" · "), width = 160.dp) { onToonClick(toonsCategory.id, t.id) }
                        }
                    }
                }
            }
        }

        // Seções do jogo
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader("Seções do guia", "${game.categories.size} seções")
        }
        items(game.categories, key = { it.id }) { category ->
            CategoryTile(category) { onCategoryClick(category.id) }
        }
    }
}
