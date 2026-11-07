package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.unit.Dp
import com.guiadejogos.app.ui.components.MediaCard
import com.guiadejogos.app.ui.components.mediaCardWidth
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.model.Category
import com.guiadejogos.app.data.model.ContentStatus
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.EmptyState
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.FilterChipRow
import com.guiadejogos.app.ui.components.HeaderBanner
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.SearchField
import com.guiadejogos.app.ui.components.SectionView
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens
import kotlinx.coroutines.launch

/**
 * Conteúdo de uma seção do jogo. Em seções com galerias (personagens, skins, armas, frutas)
 * aparecem pesquisa e filtros (um filtro por grupo, ex.: raridade).
 */
@Composable
fun CategoryScreen(
    repository: ContentRepository,
    gameId: String,
    categoryId: String,
    onBack: () -> Unit,
    onToonClick: (String) -> Unit = {},
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val state by rememberLoad(gameId, categoryId, reloadKey) {
        val game = repository.loadGameById(gameId) ?: error("Jogo não encontrado")
        game.categories.firstOrNull { it.id == categoryId } ?: error("Seção não encontrada")
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val title = (state as? LoadState.Success)?.data?.title ?: ""
    val collection = (state as? LoadState.Success)?.data?.sections?.any { it is Section.Toons || it is Section.Gallery } == true

    AppScreen(title = title, onBack = onBack, maxContentWidthDp = if (collection) AppConfig.MAX_GRID_WIDTH_DP else AppConfig.MAX_CONTENT_WIDTH_DP) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> CategoryContent(
                category = s.data,
                padding = padding,
                onCopied = { code -> scope.launch { snackbar.showSnackbar("Código $code copiado!") } },
                onToonClick = onToonClick,
            )
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { SnackbarHost(snackbar) }
    }
}

private const val ALL = "Todos"

@Composable
private fun CategoryContent(
    category: Category,
    padding: PaddingValues,
    onCopied: (String) -> Unit,
    onToonClick: (String) -> Unit,
) {
    val groups = category.sections.filter { (it is Section.Toons || it is Section.Gallery) && it.title.isNotBlank() }
    val isCollection = category.sections.any { it is Section.Toons || it is Section.Gallery }
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf(ALL) }

    // Aplica pesquisa e filtro às galerias; os demais blocos só aparecem sem filtro.
    fun filtered(): List<Section> {
        val q = query.trim()
        return category.sections.mapNotNull { sec ->
            when (sec) {
                is Section.Toons -> if (group != ALL && sec.title != group) null else
                    sec.copy(toons = sec.toons.filter { q.isBlank() || it.name.contains(q, true) || it.kind.contains(q, true) }).takeIf { it.toons.isNotEmpty() }
                is Section.Gallery -> if (group != ALL && sec.title != group) null else
                    sec.copy(images = sec.images.filter { q.isBlank() || it.caption.contains(q, true) || it.subtitle.contains(q, true) }).takeIf { it.images.isNotEmpty() }
                else -> if (group == ALL && q.isBlank()) sec else null
            }
        }
    }

    val list = filtered()
    // Grade "preguiçosa": só carrega as imagens que aparecem na tela (leve no carro).
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = mediaCardWidth()),
        contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = padding.calculateTopPadding() + 4.dp, bottom = padding.calculateBottomPadding() + 32.dp),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Gap),
        verticalArrangement = Arrangement.spacedBy(Dimens.Gap),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { HeaderBanner(category.emoji, category.title, category.description) }

        if (isCollection) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SearchField(query, { query = it }, "Pesquisar em ${category.title.lowercase()}", Modifier.fillMaxWidth())
                    if (groups.size > 1) FilterChipRow(listOf(ALL) + groups.map { it.title }, group, onSelect = { group = it })
                }
            }
        }

        if (category.status == ContentStatus.COMING_SOON && category.sections.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyState("🚧", "Em preparação", "Esta seção ainda está sendo preparada. Em breve terá conteúdo aqui!") }
        }
        if (list.isEmpty() && category.sections.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyState("🔎", "Nada encontrado", "Tente outra palavra ou outro filtro.") }
        }

        list.forEachIndexed { si, section ->
            when (section) {
                is Section.Toons -> {
                    if (section.title.isNotBlank()) item(key = "t$si", span = { GridItemSpan(maxLineSpan) }) { GroupTitle(section.title) }
                    itemsIndexed(section.toons, key = { i, it -> "toon-$si-$i-${it.id}" }) { _, t ->
                        MediaCard(t.image, t.name, badge = t.kind.substringBefore(" · "), detail = t.cost, width = Dp.Unspecified, modifier = Modifier.animateItem()) { onToonClick(t.id) }
                    }
                }
                is Section.Gallery -> {
                    if (section.title.isNotBlank()) item(key = "g$si", span = { GridItemSpan(maxLineSpan) }) { GroupTitle(section.title) }
                    itemsIndexed(section.images, key = { i, _ -> "img-$si-$i" }) { _, g ->
                        MediaCard(g.image, g.caption, detail = g.subtitle, width = Dp.Unspecified, imageRatio = 0.85f, modifier = Modifier.animateItem())
                    }
                }
                else -> item(key = "s$si", span = { GridItemSpan(maxLineSpan) }) { SectionView(section, AppColors.Primary, onCopied, onToonClick) }
            }
        }

        if (category.updatedAt.isNotBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }) { Text("Última revisão: ${category.updatedAt}", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary) }
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary, modifier = Modifier.padding(top = 8.dp))
}
