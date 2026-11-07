package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.EmptyState
import com.guiadejogos.app.ui.components.GameIcon
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.MediaCard
import com.guiadejogos.app.ui.components.Pill
import com.guiadejogos.app.ui.components.SearchField
import com.guiadejogos.app.ui.components.SectionHeader
import com.guiadejogos.app.ui.components.mediaCardWidth
import com.guiadejogos.app.ui.components.pressable
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens

/** Um resultado de busca. */
private data class Hit(val type: String, val title: String, val subtitle: String, val image: String, val emoji: String, val route: String)

/** Busca global: jogos, personagens, skins/itens e seções de todos os guias. */
@Composable
fun SearchScreen(repository: ContentRepository, onBack: () -> Unit, onOpenRoute: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val state by rememberLoad(Unit) {
        val index = repository.loadIndex()
        val hits = mutableListOf<Hit>()
        index.games.forEach { g ->
            hits += Hit("Jogos", g.name, listOf(g.developer, g.genres.joinToString(" · ")).filter { it.isNotBlank() }.joinToString(" • "), g.cover, g.emoji, "game/${g.id}")
            val d = runCatching { repository.loadGame(g.file) }.getOrNull() ?: return@forEach
            d.categories.forEach { c ->
                hits += Hit("Seções", c.title, g.name, "", c.emoji, "game/${g.id}/category/${c.id}")
                c.sections.forEach { s ->
                    when (s) {
                        is Section.Toons -> s.toons.forEach { t -> hits += Hit("Personagens", t.name, "${g.name} · ${t.kind}", t.image, "", "game/${g.id}/category/${c.id}/toon/${t.id}") }
                        is Section.Gallery -> s.images.forEach { im -> hits += Hit("Skins e itens", im.caption, g.name, im.image, "", "game/${g.id}/category/${c.id}") }
                        else -> Unit
                    }
                }
            }
        }
        hits
    }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    AppScreen(title = "Pesquisar", onBack = onBack, maxContentWidthDp = AppConfig.MAX_GRID_WIDTH_DP) { padding ->
        val all = (state as? LoadState.Success)?.data
        val q = query.trim()
        val results = if (all == null || q.length < 2) emptyList() else all.filter { it.title.contains(q, true) || it.subtitle.contains(q, true) }.take(120)
        val groups = listOf("Jogos", "Personagens", "Skins e itens", "Seções").associateWith { t -> results.filter { it.type == t } }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = mediaCardWidth()),
            contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = padding.calculateTopPadding() + 4.dp, bottom = padding.calculateBottomPadding() + 28.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Gap),
            verticalArrangement = Arrangement.spacedBy(Dimens.Gap),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SearchField(query, { query = it }, "Jogos, personagens, skins, seções…", Modifier.fillMaxWidth(), focusRequester = focus)
            }
            when {
                state is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { LoadingView() }
                q.length < 2 -> item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState("🔍", "O que você procura?", "Digite pelo menos 2 letras. Ex.: \"Shelly\", \"Vintage\", \"Dragon\", \"códigos\".")
                }
                results.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) { EmptyState("🤔", "Nada encontrado", "Tente outro nome ou confira a ortografia.") }
                else -> groups.forEach { (type, list) ->
                    if (list.isEmpty()) return@forEach
                    item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader(type, "${list.size} resultados") }
                    val asCards = type == "Personagens" || type == "Skins e itens"
                    if (asCards) {
                        items(list) { h -> MediaCard(h.image, h.title, detail = h.subtitle, width = Dp.Unspecified) { onOpenRoute(h.route) } }
                    } else {
                        items(list, span = { GridItemSpan(maxLineSpan) }) { h ->
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 76.dp).clip(RoundedCornerShape(Dimens.CardRadius)).background(AppColors.Surface)
                                    .pressable { onOpenRoute(h.route) }.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                GameIcon(h.image, h.emoji, 52.dp)
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(h.title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                                    Text(h.subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                                }
                                Pill(if (type == "Seções") "Seção" else "Jogo", AppColors.TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
