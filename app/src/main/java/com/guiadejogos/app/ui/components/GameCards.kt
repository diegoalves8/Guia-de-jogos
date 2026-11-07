package com.guiadejogos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.guiadejogos.app.data.model.Category
import com.guiadejogos.app.data.model.ContentStatus
import com.guiadejogos.app.data.model.GameSummary
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.parseHexColor

/** Capa grande de jogo (estilo loja de jogos): imagem, degradê, nome e números. */
@Composable
fun GameCoverCard(game: GameSummary, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val accent = parseHexColor(game.colorHex)
    Box(
        modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.55f), AppColors.Surface)))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
            .clickable(onClick = onClick)
            .aspectRatio(1.35f),
    ) {
        if (game.cover.isNotBlank()) {
            AsyncImage(
                model = game.cover,
                contentDescription = game.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(start = 90.dp, top = 8.dp, bottom = 48.dp, end = 8.dp),
                alignment = Alignment.CenterEnd,
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, AppColors.Background.copy(alpha = 0.92f)), startY = 120f),
            ),
        )
        Column(Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Text(game.emoji, style = MaterialTheme.typography.headlineLarge)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(game.name, style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip(game.platform, AppColors.TextSecondary)
                if (game.stats.characters > 0) Chip("${game.stats.characters} ${game.stats.label}", accent)
                if (game.status == ContentStatus.COMING_SOON) Chip("EM BREVE", AppColors.Accent)
            }
        }
    }
}

@Composable
fun Chip(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.78f),
        color = color,
        maxLines = 1,
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

/** Bloco de categoria em grade (ícone grande + título), estilo apps de jogos. */
@Composable
fun CategoryTile(category: Category, accent: Color, onClick: () -> Unit) {
    val soon = category.status == ContentStatus.COMING_SOON && category.sections.isEmpty()
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(AppColors.Surface)
            .border(1.dp, accent.copy(alpha = if (soon) 0.12f else 0.3f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier.size(52.dp).background(accent.copy(alpha = if (soon) 0.08f else 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(category.emoji, style = MaterialTheme.typography.headlineSmall) }
        Text(category.title, style = MaterialTheme.typography.titleMedium, color = if (soon) AppColors.TextSecondary else AppColors.TextPrimary, maxLines = 2)
        Text(
            if (soon) "Em breve" else category.description,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
        )
    }
}
