package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SystemUpdate
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.BuildConfig
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.model.CodeStatus
import com.guiadejogos.app.data.model.GameSummary
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.ChartEntry
import com.guiadejogos.app.ui.components.DashCard
import com.guiadejogos.app.ui.components.DonutChart
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.GameCoverCard
import com.guiadejogos.app.ui.components.HorizontalBarChart
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.StatTile
import com.guiadejogos.app.ui.components.compactNumber
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.parseHexColor

/** Números do painel, calculados a partir do conteúdo. */
private data class DashboardData(
    val games: List<GameSummary>,
    val characters: Int,
    val skins: Int,
    val activeCodes: Int,
    val categories: Int,
    val updatedAt: String,
)

/** Aba Início: painel com indicadores, gráficos e atalhos para os jogos. */
@Composable
fun DashboardScreen(
    repository: ContentRepository,
    onGameClick: (String) -> Unit,
    onOpenGames: () -> Unit,
    onOpenUpdates: () -> Unit,
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val state by rememberLoad(reloadKey) {
        val index = repository.loadIndex()
        var codes = 0
        var cats = 0
        index.games.forEach { g ->
            runCatching { repository.loadGame(g.file) }.getOrNull()?.let { d ->
                cats += d.categories.size
                codes += d.categories.flatMap { it.sections }.filterIsInstance<Section.Codes>()
                    .flatMap { it.codes }.count { it.status == CodeStatus.ACTIVE }
            }
        }
        DashboardData(
            games = index.games,
            characters = index.games.sumOf { it.stats.characters },
            skins = index.games.sumOf { it.stats.skins },
            activeCodes = codes,
            categories = cats,
            updatedAt = index.updatedAt,
        )
    }

    AppScreen(title = "Guia de Jogos", onBack = null, inShell = true, maxContentWidthDp = AppConfig.MAX_GRID_WIDTH_DP) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> DashboardContent(s.data, padding, onGameClick, onOpenGames, onOpenUpdates)
        }
    }
}

@Composable
private fun DashboardContent(
    d: DashboardData,
    padding: PaddingValues,
    onGameClick: (String) -> Unit,
    onOpenGames: () -> Unit,
    onOpenUpdates: () -> Unit,
) {
    val screen = rememberScreenInfo()
    val wide = screen.widthDp >= 700
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = padding.calculateTopPadding() + 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        // Boas-vindas
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(AppColors.Primary.copy(alpha = 0.55f), AppColors.Secondary.copy(alpha = 0.18f))), RoundedCornerShape(26.dp))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Olá, jogador! 👋", style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
                Text(
                    "${d.games.size} jogos, ${compactNumber(d.characters)} personagens e ${compactNumber(d.skins)} skins e itens para consultar.",
                    style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary,
                )
                Text("Conteúdo revisado em ${d.updatedAt}", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary.copy(alpha = 0.8f))
            }
        }

        // Indicadores (KPIs)
        item {
            val tiles = listOf(
                Triple("🎮", d.games.size.toString(), "Jogos") to AppColors.Primary,
                Triple("🧸", compactNumber(d.characters), "Personagens") to AppColors.Secondary,
                Triple("👕", compactNumber(d.skins), "Skins e itens") to AppColors.Accent,
                Triple("🎁", d.activeCodes.toString(), "Códigos ativos") to AppColors.Success,
            )
            val perRow = if (wide) 4 else 2
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tiles.chunked(perRow).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { (t, c) -> StatTile(t.first, t.second, t.third, c, Modifier.weight(1f)) }
                    }
                }
            }
        }

        // Destaques (carrossel de capas)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenGames)) {
                    Text("Seus jogos", style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    Text("Ver todos", style = MaterialTheme.typography.labelLarge, color = AppColors.Secondary)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = AppColors.Secondary)
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(d.games, key = { it.id }) { g ->
                        GameCoverCard(g, Modifier.width(if (wide) 320.dp else 260.dp)) { onGameClick(g.id) }
                    }
                }
            }
        }

        // Gráficos
        item {
            val bars = d.games.map { ChartEntry(it.name, it.stats.characters.toFloat(), parseHexColor(it.colorHex)) }
            val slices = d.games.filter { it.stats.skins > 0 }.map { ChartEntry(it.name, it.stats.skins.toFloat(), parseHexColor(it.colorHex)) }
            val barCard: @Composable (Modifier) -> Unit = { m ->
                DashCard("Personagens por jogo", "Toons, Brawlers, frutas, armas…", m) { HorizontalBarChart(bars) }
            }
            val donutCard: @Composable (Modifier) -> Unit = { m ->
                DashCard("Skins e itens", "Distribuição entre os jogos", m) {
                    DonutChart(slices, compactNumber(d.skins), "no total", wide = !wide || screen.widthDp >= 1000)
                }
            }
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { barCard(Modifier.weight(1f)); donutCard(Modifier.weight(1f)) }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { barCard(Modifier.fillMaxWidth()); donutCard(Modifier.fillMaxWidth()) }
            }
        }

        // Atualização do app
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(AppColors.Surface, RoundedCornerShape(24.dp))
                    .clickable(onClick = onOpenUpdates)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.background(AppColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(14.dp)).padding(10.dp)) {
                    Icon(Icons.Filled.SystemUpdate, null, tint = AppColors.Primary)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Atualizações", style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                    Text("Versão ${BuildConfig.VERSION_NAME} · toque para procurar novidades", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = AppColors.TextSecondary)
            }
        }
        item { Spacer(Modifier.padding(bottom = padding.calculateBottomPadding())) }
    }
}
