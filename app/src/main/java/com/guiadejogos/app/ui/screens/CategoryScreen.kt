package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.data.model.Category
import com.guiadejogos.app.data.model.ContentStatus
import com.guiadejogos.app.data.model.GameDetail
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.HeaderBanner
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.SectionView
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.parseHexColor
import kotlinx.coroutines.launch

/** Conteúdo de uma categoria: lista de blocos (texto, passos, códigos, itens, níveis, links). */
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
        val category = game.categories.firstOrNull { it.id == categoryId } ?: error("Categoria não encontrada")
        game to category
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val title = (state as? LoadState.Success)?.data?.second?.title ?: ""

    AppScreen(title = title, onBack = onBack) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> CategoryContent(
                game = s.data.first,
                category = s.data.second,
                padding = padding,
                onCopied = { code -> scope.launch { snackbar.showSnackbar("Código $code copiado!") } },
                onToonClick = onToonClick,
            )
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(snackbar)
        }
    }
}

@Composable
private fun CategoryContent(
    game: GameDetail,
    category: Category,
    padding: PaddingValues,
    onCopied: (String) -> Unit,
    onToonClick: (String) -> Unit,
) {
    val accent = parseHexColor(game.colorHex)
    LazyColumn(
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { HeaderBanner(category.emoji, category.title, category.description, accent) }

        if (category.status == ContentStatus.COMING_SOON && category.sections.isEmpty()) {
            item {
                Text(
                    "🚧 Esta seção ainda está sendo preparada. Em breve terá conteúdo aqui!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextSecondary,
                )
            }
        }

        items(category.sections) { section -> SectionView(section, accent, onCopied, onToonClick) }

        if (category.updatedAt.isNotBlank()) {
            item {
                Text(
                    "Última revisão: ${category.updatedAt}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary.copy(alpha = 0.7f),
                )
            }
        }
    }
}
