package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.data.model.CodeStatus
import com.guiadejogos.app.data.model.GameCode
import com.guiadejogos.app.data.model.GameSummary
import com.guiadejogos.app.data.model.InfoItem
import com.guiadejogos.app.data.model.NewsItem
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.GameIcon
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.Pill
import com.guiadejogos.app.ui.components.SectionHeader
import com.guiadejogos.app.ui.components.pressable
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens
import kotlinx.coroutines.launch

private data class NewsData(
    val codes: List<Pair<GameSummary, GameCode>>,
    val appNews: List<NewsItem>,
    val gameUpdates: List<Triple<GameSummary, String, InfoItem>>, // jogo, id da seção, item
)

/** Aba Novidades: códigos ativos, novidades do app e atualizações dos jogos. */
@Composable
fun NewsScreen(repository: ContentRepository, onOpenCategory: (gameId: String, categoryId: String) -> Unit) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val state by rememberLoad(reloadKey) {
        val index = repository.loadIndex()
        val codes = mutableListOf<Pair<GameSummary, GameCode>>()
        val ups = mutableListOf<Triple<GameSummary, String, InfoItem>>()
        index.games.forEach { g ->
            val d = runCatching { repository.loadGame(g.file) }.getOrNull() ?: return@forEach
            d.categories.forEach { c ->
                c.sections.filterIsInstance<Section.Codes>().flatMap { it.codes }
                    .filter { it.status == CodeStatus.ACTIVE }.forEach { codes += g to it }
                if (c.id in setOf("atualizacoes", "novidades", "eventos")) {
                    c.sections.filterIsInstance<Section.Items>().flatMap { it.items }.forEach { ups += Triple(g, c.id, it) }
                }
            }
        }
        NewsData(codes, index.news, ups)
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val wide = rememberScreenInfo().widthDp >= 900

    AppScreen(title = "Novidades", onBack = null, inShell = true) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> LazyColumn(
                contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = padding.calculateTopPadding() + 4.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.Gap),
                modifier = Modifier.fillMaxSize(),
            ) {
                item { SectionHeader("Códigos ativos agora", "Toque para copiar") }
                if (s.data.codes.isEmpty()) item { Text("Nenhum código ativo confirmado no momento.", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary) }
                items(s.data.codes) { (g, c) ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 76.dp).clip(RoundedCornerShape(Dimens.CardRadius)).background(AppColors.Surface)
                            .pressable { clipboard.setText(AnnotatedString(c.code)); scope.launch { snackbar.showSnackbar("Código ${c.code} copiado!") } }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GameIcon(g.cover, g.emoji, 52.dp)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.code, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
                            Text("${g.name} · ${c.reward}", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                        }
                        Pill("Ativo", AppColors.Success)
                        IconButton(onClick = { clipboard.setText(AnnotatedString(c.code)); scope.launch { snackbar.showSnackbar("Código ${c.code} copiado!") } }) {
                            Icon(Icons.Filled.ContentCopy, "Copiar", tint = AppColors.Primary)
                        }
                    }
                }

                item { Spacer(Modifier.size(8.dp)); SectionHeader("Novidades do app") }
                items(s.data.appNews) { n -> Timeline(n.date, n.title, n.body) }

                if (s.data.gameUpdates.isNotEmpty()) {
                    item { Spacer(Modifier.size(8.dp)); SectionHeader("Atualizações dos jogos") }
                    items(s.data.gameUpdates) { (g, cat, upd) ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(Dimens.CardRadius)).background(AppColors.Surface)
                                .pressable { onOpenCategory(g.id, cat) }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GameIcon(g.cover, g.emoji, 52.dp)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(upd.name, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                                if (upd.description.isNotBlank()) Text(upd.description, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, maxLines = if (wide) 2 else 3)
                            }
                            Pill(g.name, AppColors.Secondary)
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { SnackbarHost(snackbar) }
    }
}

@Composable
private fun Timeline(date: String, title: String, body: String) {
    Row(Modifier.fillMaxWidth().background(AppColors.Surface, RoundedCornerShape(Dimens.CardRadius)).padding(18.dp)) {
        Box(Modifier.padding(top = 8.dp).size(10.dp).background(AppColors.Primary, CircleShape))
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(10.dp))
                Text(date.split("-").reversed().joinToString("/"), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
            }
            Text(body, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
        }
    }
}
