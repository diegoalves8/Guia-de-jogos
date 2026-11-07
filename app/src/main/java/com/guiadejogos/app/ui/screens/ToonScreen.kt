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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.data.model.Toon
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.Badge
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.RemoteImage
import com.guiadejogos.app.ui.components.SkinsGrid
import com.guiadejogos.app.ui.components.TutorialPlayer
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.parseHexColor

/** Página de um Toon: foto, custo, requisitos, mini tutorial, Maestria e skins. */
@Composable
fun ToonScreen(
    repository: ContentRepository,
    gameId: String,
    categoryId: String,
    toonId: String,
    onBack: () -> Unit,
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val state by rememberLoad(gameId, categoryId, toonId, reloadKey) {
        val game = repository.loadGameById(gameId) ?: error("Jogo não encontrado")
        val toon = game.categories.first { it.id == categoryId }.sections
            .filterIsInstance<Section.Toons>().flatMap { it.toons }
            .firstOrNull { it.id == toonId } ?: error("Toon não encontrado")
        parseHexColor(game.colorHex) to toon
    }
    val title = (state as? LoadState.Success)?.data?.second?.name ?: ""

    AppScreen(title = title, onBack = onBack) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> ToonContent(s.data.second, s.data.first, padding)
        }
    }
}

@Composable
private fun ToonContent(toon: Toon, accent: Color, padding: PaddingValues) {
    val uri = LocalUriHandler.current
    LazyColumn(
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { ToonHeader(toon, accent) }

        item {
            Block("🎬 Mini tutorial: como conseguir") {
                TutorialPlayer(toon.tutorial, accent)
                if (toon.videoUrl.isNotBlank()) {
                    Button(
                        onClick = { runCatching { uri.openUri(toon.videoUrl) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    ) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ver vídeos no YouTube", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            Block("🔓 Como desbloquear") {
                Info("Custo / como liberar", toon.cost.ifBlank { "—" }, accent)
                if (toon.requirements.isEmpty()) {
                    Info("Requisitos", "Nenhum além do custo.", accent)
                } else {
                    Info("Requisitos", toon.requirements.joinToString("\n") { "• $it" }, accent)
                }
            }
        }

        toon.infoBlocks.filter { it.items.isNotEmpty() }.forEach { block ->
            item {
                Block(block.title) {
                    block.items.forEach { line ->
                        Text("• $line", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                    }
                }
            }
        }

        if (toon.mastery.isNotEmpty()) item {
            Block("🏅 Maestria") {
                val wide = rememberScreenInfo().widthDp >= 600
                val quests: @Composable (Modifier) -> Unit = { mod ->
                    Column(mod, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Missões (complete todas):", style = MaterialTheme.typography.titleMedium, color = accent)
                        toon.mastery.forEachIndexed { i, q ->
                            Text("${i + 1}. $q", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                        }
                        Text(
                            "🎁 Recompensa: skin Vintage ${toon.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.Accent,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
                if (wide) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RemoteImage(toon.vintageImage, "Vintage ${toon.name}", Modifier.size(170.dp, 230.dp))
                        Spacer(Modifier.width(20.dp))
                        quests(Modifier.weight(1f))
                    }
                } else {
                    RemoteImage(toon.vintageImage, "Vintage ${toon.name}", Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 260.dp))
                    quests(Modifier.fillMaxWidth())
                }
            }
        }

        if (toon.skins.isNotEmpty()) item {
            Block("👕 Skins (${toon.skins.size})") {
                SkinsGrid(toon.skins, accent)
            }
        }

        item {
            Text(
                "Dados e imagens: wikis da comunidade (Fandom, CC BY-SA). Marcas e personagens pertencem aos seus donos.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ToonHeader(toon: Toon, accent: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.5f), accent.copy(alpha = 0.1f))), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RemoteImage(toon.image, toon.name, Modifier.size(140.dp))
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(toon.name, style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
            Badge(toon.kind, AppColors.Secondary)
            if (toon.cost.isNotBlank()) Text("💰 ${toon.cost}", style = MaterialTheme.typography.titleMedium, color = AppColors.Accent)
        }
    }
}

@Composable
private fun Block(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(AppColors.SurfaceHigh.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
        content()
    }
}

@Composable
private fun Info(label: String, value: String, accent: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = accent)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
    }
}
