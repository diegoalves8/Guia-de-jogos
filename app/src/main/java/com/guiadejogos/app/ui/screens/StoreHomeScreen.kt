package com.guiadejogos.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.data.repository.FavoritesStore
import com.guiadejogos.app.ui.components.EmptyState
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.FeaturedBanner
import com.guiadejogos.app.ui.components.FilterChipRow
import com.guiadejogos.app.ui.components.GameCard
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.MediaCard
import com.guiadejogos.app.ui.components.SearchBarButton
import com.guiadejogos.app.ui.components.SectionHeader
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens

/**
 * Aba Início — inspirada na Google Play Store:
 * cabeçalho com busca, banner de destaque, filtros por categoria e cartões de jogos.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoreHomeScreen(
    repository: ContentRepository,
    favorites: FavoritesStore,
    onGameClick: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val state by rememberLoad(reloadKey) { repository.loadIndex() }
    var filter by rememberSaveable { mutableStateOf(GameFilters.ALL) }
    val screen = rememberScreenInfo()
    val wide = screen.widthDp >= 700

    Box(
        Modifier.fillMaxSize().background(AppColors.Background).windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.TopCenter,
    ) {
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Cabeçalho: logo + nome + busca + configurações
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(48.dp).background(Brush.linearGradient(listOf(AppColors.Primary, AppColors.Secondary)), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text("🎮", style = MaterialTheme.typography.titleLarge) }
                        Spacer(Modifier.width(14.dp))
                        if (wide) {
                            Text("Guia de Jogos", style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
                            Spacer(Modifier.width(24.dp))
                        }
                        SearchBarButton("Pesquisar jogos, personagens, skins…", Modifier.weight(1f), onClick = onSearch)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onSettings, modifier = Modifier.size(Dimens.TouchMin)) {
                            Icon(Icons.Filled.Settings, contentDescription = "Configurações", tint = AppColors.TextSecondary)
                        }
                    }
                }

                // Banner de destaque
                item {
                    FeaturedBanner(
                        games = s.data.games.filter { it.cover.isNotBlank() },
                        height = if (screen.isShortHeight) 280.dp else 320.dp,
                        onOpen = { onGameClick(it.id) },
                    )
                }

                // Favoritos (atalho), quando houver
                val favs = favorites.all
                if (favs.isNotEmpty()) item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SectionHeader("Seus favoritos")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.Gap)) {
                            items(favs, key = { it.key }) { f ->
                                MediaCard(f.image, f.title, detail = f.subtitle, width = 150.dp) { onOpenRoute(f.route) }
                            }
                        }
                    }
                }

                // Categorias + lista de jogos (com transição ao trocar de categoria)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        SectionHeader("Explorar", "Escolha uma categoria")
                        FilterChipRow(GameFilters.options, filter, onSelect = { filter = it })
                        AnimatedContent(targetState = filter, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "filtro") { f ->
                            val list = GameFilters.apply(s.data.games, f)
                            if (list.isEmpty()) {
                                EmptyState("🔎", "Nenhum jogo aqui ainda", "Ainda não temos jogos nesta categoria. Novos jogos chegam pelas atualizações.")
                            } else {
                                val cols = when {
                                    screen.widthDp >= 1100 -> 3
                                    screen.widthDp >= 640 -> 2
                                    else -> 1
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Gap)) {
                                    list.chunked(cols).forEach { row ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Gap)) {
                                            row.forEach { g -> GameCard(g, Modifier.weight(1f)) { onGameClick(g.id) } }
                                            repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.padding(bottom = 8.dp)) }
            }
        }
    }
}
