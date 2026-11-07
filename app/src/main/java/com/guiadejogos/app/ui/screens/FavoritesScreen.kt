package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.repository.FavoritesStore
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.EmptyState
import com.guiadejogos.app.ui.components.GameIcon
import com.guiadejogos.app.ui.components.MediaCard
import com.guiadejogos.app.ui.components.SectionHeader
import com.guiadejogos.app.ui.components.mediaCardWidth
import com.guiadejogos.app.ui.components.pressable
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens

/** Aba Favoritos: jogos e personagens marcados com ♥. */
@Composable
fun FavoritesScreen(favorites: FavoritesStore, onOpenRoute: (String) -> Unit) {
    val all = favorites.all
    val games = all.filter { it.type == "game" }
    val chars = all.filter { it.type != "game" }

    AppScreen(title = "Favoritos", onBack = null, inShell = true, maxContentWidthDp = AppConfig.MAX_GRID_WIDTH_DP) { padding ->
        if (all.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()), verticalArrangement = Arrangement.Center) {
                EmptyState("♡", "Nenhum favorito ainda", "Toque no coração na página de um jogo ou personagem para guardar aqui e achar rápido depois.")
            }
            return@AppScreen
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = mediaCardWidth()),
            contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = padding.calculateTopPadding() + 4.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Gap),
            verticalArrangement = Arrangement.spacedBy(Dimens.Gap),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (games.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader("Jogos", "${games.size} salvos") }
                items(games, key = { it.key }, span = { GridItemSpan(maxLineSpan) }) { f ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 84.dp).clip(RoundedCornerShape(Dimens.CardRadius)).background(AppColors.Surface)
                            .pressable { onOpenRoute(f.route) }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GameIcon(f.image, "🎮", 60.dp)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(f.title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            Text(f.subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                        }
                        IconButton(onClick = { favorites.toggle(f) }, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                            Icon(Icons.Filled.Favorite, "Remover dos favoritos", tint = AppColors.Danger)
                        }
                    }
                }
            }
            if (chars.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader("Personagens", "${chars.size} salvos") }
                items(chars, key = { it.key }) { f ->
                    MediaCard(f.image, f.title, badge = f.subtitle.substringBefore(" · "), width = Dp.Unspecified) { onOpenRoute(f.route) }
                }
            }
        }
    }
}
