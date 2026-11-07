package com.guiadejogos.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.guiadejogos.app.data.model.Category
import com.guiadejogos.app.data.model.ContentStatus
import com.guiadejogos.app.data.model.GameSummary
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens
import kotlinx.coroutines.delay

/** Ícone do jogo: arte do jogo recortada num quadrado arredondado. */
@Composable
fun GameIcon(url: String, emoji: String, size: Dp = 64.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.24f)).background(AppColors.SurfaceHigh),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, style = MaterialTheme.typography.headlineSmall)
        if (url.isNotBlank()) {
            AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, alignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize())
        }
    }
}

/** Selos do jogo: "Popular", "Novo no app" etc. */
@Composable
fun GameBadges(game: GameSummary) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        game.badges.forEach { b -> Pill(b, if (b.startsWith("Novo")) AppColors.Success else AppColors.Secondary) }
        if (game.status == ContentStatus.COMING_SOON) Pill("Em breve", AppColors.Accent)
    }
}

/**
 * Banner de destaque estilo Google TV: arte grande à direita, texto à esquerda,
 * troca sozinho a cada 6 segundos.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeaturedBanner(games: List<GameSummary>, height: Dp, onOpen: (GameSummary) -> Unit) {
    if (games.isEmpty()) return
    val pager = rememberPagerState { games.size }
    LaunchedEffect(pager.currentPage, games.size) {
        delay(6000)
        if (games.size > 1) pager.animateScrollToPage((pager.currentPage + 1) % games.size)
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalPager(state = pager, pageSpacing = 16.dp, modifier = Modifier.fillMaxWidth().height(height)) { page ->
            val g = games[page]
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimens.BannerRadius))
                    .background(Brush.linearGradient(listOf(Color(0xFF1C2A44), AppColors.Surface, Color(0xFF231E3D))))
                    .pressable { onOpen(g) },
            ) {
                if (g.cover.isNotBlank()) {
                    AsyncImage(
                        model = g.cover, contentDescription = g.name, contentScale = ContentScale.Fit, alignment = Alignment.CenterEnd,
                        modifier = Modifier.fillMaxHeight().fillMaxWidth().padding(start = 160.dp, top = 12.dp, bottom = 12.dp, end = 24.dp),
                    )
                }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to AppColors.Background.copy(alpha = 0.92f), 0.55f to Color.Transparent)))
                Column(
                    Modifier.align(Alignment.CenterStart).fillMaxWidth(0.58f).padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Pill("★ Em destaque", AppColors.Primary)
                    Text(g.name, style = MaterialTheme.typography.displayLarge, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOf(g.developer, g.genres.joinToString(" · ")).filter { it.isNotBlank() }.joinToString("  •  "),
                        style = MaterialTheme.typography.titleMedium, color = AppColors.TextSecondary, maxLines = 1,
                    )
                    Text(g.longDescription.ifBlank { g.subtitle }, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    PrimaryButton("Ver guia", Icons.AutoMirrored.Filled.MenuBook) { onOpen(g) }
                }
            }
        }
        // Indicadores de página
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(games.size) { i ->
                val sel = i == pager.currentPage
                Box(
                    Modifier.padding(horizontal = 4.dp).height(8.dp).width(if (sel) 28.dp else 8.dp)
                        .clip(CircleShape).background(if (sel) AppColors.Primary else AppColors.Outline),
                )
            }
        }
    }
}

/** Cartão de jogo estilo Play Store: arte em destaque, ícone, nome, categoria, descrição e selos. */
@Composable
fun GameCard(game: GameSummary, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(Dimens.CardRadius))
            .background(AppColors.Surface)
            .pressable(onClick),
    ) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(16f / 8f)
                .background(Brush.linearGradient(listOf(Color(0xFF1F2B45), Color(0xFF221F3A)))),
        ) {
            if (game.cover.isNotBlank()) {
                AsyncImage(model = game.cover, contentDescription = game.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(10.dp))
            }
        }
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            GameIcon(game.cover, game.emoji, 56.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(game.name, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOf(game.developer, game.genres.firstOrNull().orEmpty()).filter { it.isNotBlank() }.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, maxLines = 1,
                )
            }
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(game.subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (game.stats.characters > 0) Pill("${game.stats.characters} ${game.stats.label}", AppColors.Primary)
                GameBadges(game)
            }
        }
    }
}

/** Linha compacta de jogo (listas de busca e favoritos). */
@Composable
fun GameRow(game: GameSummary, trailing: @Composable () -> Unit = {}, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 84.dp).clip(RoundedCornerShape(Dimens.CardRadius))
            .background(AppColors.Surface).pressable(onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GameIcon(game.cover, game.emoji, 60.dp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(game.name, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
            Text(listOf(game.developer, game.genres.joinToString(" · ")).filter { it.isNotBlank() }.joinToString(" • "), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, maxLines = 1)
        }
        trailing()
    }
}

/** Bloco de seção do jogo (Personagens, Códigos, Guias...). */
@Composable
fun CategoryTile(category: Category, accent: Color = AppColors.Primary, onClick: () -> Unit) {
    val soon = category.status == ContentStatus.COMING_SOON && category.sections.isEmpty()
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 148.dp)
            .clip(RoundedCornerShape(Dimens.CardRadius))
            .background(AppColors.Surface)
            .pressable(onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(50.dp).background(accent.copy(alpha = if (soon) 0.06f else 0.14f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
            Text(category.emoji, style = MaterialTheme.typography.headlineSmall)
        }
        Text(category.title, style = MaterialTheme.typography.titleMedium, color = if (soon) AppColors.TextSecondary else AppColors.TextPrimary, maxLines = 2)
        Text(
            if (soon) "Em breve" else category.description,
            style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
    }
}
