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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.data.model.Toon
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.data.repository.FavoritesStore
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.components.ErrorView
import com.guiadejogos.app.ui.components.FilterChipRow
import com.guiadejogos.app.ui.components.LoadingView
import com.guiadejogos.app.ui.components.Pill
import com.guiadejogos.app.ui.components.PrimaryButton
import com.guiadejogos.app.ui.components.RemoteImage
import com.guiadejogos.app.ui.components.SecondaryButton
import com.guiadejogos.app.ui.components.SkinsGrid
import com.guiadejogos.app.ui.components.TutorialPlayer
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens

/**
 * Página de personagem (Toon, Brawler, fruta...).
 * Paisagem (tablet/carro): imagem e ações fixas à esquerda, abas à direita — quase sem rolagem.
 * Retrato: cabeçalho em cima e abas embaixo.
 */
@Composable
fun ToonScreen(
    repository: ContentRepository,
    favorites: FavoritesStore,
    gameId: String,
    categoryId: String,
    toonId: String,
    onBack: () -> Unit,
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val state by rememberLoad(gameId, categoryId, toonId, reloadKey) {
        val game = repository.loadGameById(gameId) ?: error("Jogo não encontrado")
        game.categories.first { it.id == categoryId }.sections
            .filterIsInstance<Section.Toons>().flatMap { it.toons }
            .firstOrNull { it.id == toonId } ?: error("Personagem não encontrado")
    }
    val toon = (state as? LoadState.Success)?.data
    val favKey = FavoritesStore.characterKey(gameId, categoryId, toonId)

    AppScreen(
        title = toon?.name ?: "",
        onBack = onBack,
        maxContentWidthDp = AppConfig.MAX_GRID_WIDTH_DP,
        actions = {
            if (toon != null) {
                val fav = favorites.isFavorite(favKey)
                IconButton(onClick = {
                    favorites.toggle(FavoritesStore.Favorite(favKey, toon.name, toon.kind, toon.image, "game/$gameId/category/$categoryId/toon/$toonId", "character"))
                }, modifier = Modifier.size(Dimens.TouchMin)) {
                    Icon(if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = "Favoritar", tint = if (fav) AppColors.Danger else AppColors.TextPrimary)
                }
            }
        },
    ) { padding ->
        when (val s = state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(s.message) { reloadKey++ }
            is LoadState.Success -> ToonContent(s.data, padding)
        }
    }
}

@Composable
private fun ToonContent(toon: Toon, padding: PaddingValues) {
    val screen = rememberScreenInfo()
    val twoPane = screen.widthDp >= 760
    val tabs = buildList {
        add("Visão geral")
        if (toon.tutorial.isNotEmpty()) add("Como conseguir")
        if (toon.mastery.isNotEmpty()) add("Maestria")
        if (toon.skins.isNotEmpty()) add("Skins (${toon.skins.size})")
    }
    var tab by rememberSaveable(toon.id) { mutableIntStateOf(0) }

    val tabContent: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Gap)) {
            FilterChipRow(tabs, tabs.getOrElse(tab) { tabs[0] }, onSelect = { tab = tabs.indexOf(it) })
            AnimatedContent(targetState = tabs.getOrElse(tab) { tabs[0] }, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "aba") { t ->
                when {
                    t == "Visão geral" -> Overview(toon)
                    t == "Como conseguir" -> HowTo(toon)
                    t == "Maestria" -> Mastery(toon, twoPane)
                    t.startsWith("Skins") -> SkinsGrid(toon.skins)
                }
            }
        }
    }

    if (twoPane) {
        Row(
            Modifier.fillMaxSize().padding(top = padding.calculateTopPadding(), start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, bottom = padding.calculateBottomPadding()),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(Modifier.width(320.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) { Hero(toon, vertical = true) }
            LazyColumn(Modifier.weight(1f).fillMaxHeight(), contentPadding = PaddingValues(bottom = 28.dp)) { item { tabContent() } }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = padding.calculateTopPadding() + 4.dp, bottom = padding.calculateBottomPadding() + 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { Hero(toon, vertical = false) }
            item { tabContent() }
        }
    }
}

@Composable
private fun Hero(toon: Toon, vertical: Boolean) {
    val uri = LocalUriHandler.current
    val image: @Composable (Modifier) -> Unit = { m ->
        Box(m.background(Brush.verticalGradient(listOf(Color(0xFF223050), AppColors.Surface)), RoundedCornerShape(Dimens.BannerRadius))) {
            RemoteImage(toon.image, toon.name, Modifier.fillMaxSize().padding(16.dp), framed = false)
        }
    }
    val info: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(toon.name, style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
            if (toon.kind.isNotBlank()) Pill(toon.kind, AppColors.Secondary)
            if (toon.cost.isNotBlank()) Text(toon.cost, style = MaterialTheme.typography.titleMedium, color = AppColors.Accent)
            if (toon.videoUrl.isNotBlank()) {
                SecondaryButton("Ver vídeos", Icons.Filled.PlayCircle) { runCatching { uri.openUri(toon.videoUrl) } }
            }
        }
    }
    if (vertical) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) { image(Modifier.fillMaxWidth().aspectRatio(1f)); info() }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            image(Modifier.size(150.dp))
            Spacer(Modifier.width(20.dp))
            info()
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(AppColors.Surface, RoundedCornerShape(Dimens.CardRadius)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
        content()
    }
}

@Composable
private fun Bullet(text: String) {
    Row {
        Box(Modifier.padding(top = 9.dp).size(6.dp).background(AppColors.Primary, CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
    }
}

@Composable
private fun Overview(toon: Toon) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Gap)) {
        InfoCard("Como desbloquear") {
            Text("Custo", style = MaterialTheme.typography.labelLarge, color = AppColors.Primary)
            Text(toon.cost.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
            Text("Requisitos", style = MaterialTheme.typography.labelLarge, color = AppColors.Primary)
            if (toon.requirements.isEmpty()) Text("Nenhum além do custo.", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
            toon.requirements.forEach { Bullet(it) }
        }
        toon.infoBlocks.filter { it.items.isNotEmpty() }.forEach { b ->
            InfoCard(b.title) { b.items.forEach { Bullet(it) } }
        }
        Text(
            "Dados e imagens: wikis da comunidade (Fandom, CC BY-SA). Marcas e personagens pertencem aos seus donos.",
            style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary,
        )
    }
}

@Composable
private fun HowTo(toon: Toon) {
    val uri = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Gap)) {
        TutorialPlayer(toon.tutorial, AppColors.Primary)
        if (toon.videoUrl.isNotBlank()) {
            PrimaryButton("Ver vídeos no YouTube", Icons.Filled.PlayCircle, Modifier.fillMaxWidth()) { runCatching { uri.openUri(toon.videoUrl) } }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Mastery(toon: Toon, wide: Boolean) {
    InfoCard("Missões de Maestria") {
        val quests: @Composable (Modifier) -> Unit = { m ->
            Column(m, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                toon.mastery.forEachIndexed { i, q ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).background(AppColors.Primary.copy(alpha = 0.16f), CircleShape), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = AppColors.Primary)
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(q, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                    }
                }
                if (toon.vintageImage.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Pill("🎁 Recompensa: skin Vintage ${toon.name}", AppColors.Success)
                }
            }
        }
        if (wide && toon.vintageImage.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RemoteImage(toon.vintageImage, "Vintage ${toon.name}", Modifier.size(170.dp, 230.dp))
                Spacer(Modifier.width(22.dp))
                quests(Modifier.weight(1f))
            }
        } else {
            if (toon.vintageImage.isNotBlank()) RemoteImage(toon.vintageImage, "Vintage ${toon.name}", Modifier.fillMaxWidth().height(240.dp))
            quests(Modifier.fillMaxWidth())
        }
    }
}
