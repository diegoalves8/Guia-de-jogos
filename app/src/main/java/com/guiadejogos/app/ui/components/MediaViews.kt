package com.guiadejogos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.data.model.GalleryImage
import com.guiadejogos.app.data.model.Toon
import com.guiadejogos.app.data.model.ToonSkin
import com.guiadejogos.app.ui.theme.AppColors

/** Largura dos cards de imagem: maiores no tablet/carro, menores no celular. */
@Composable
private fun cardWidth(): Dp = if (rememberScreenInfo().widthDp >= 600) 170.dp else 148.dp

/** Grade de imagens com legenda (skins Vintage, Douradas...). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryView(images: List<GalleryImage>) {
    val w = cardWidth()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        images.forEach { ImageCard(it.image, it.caption, it.subtitle, AppColors.Primary, w) }
    }
}

/** Grade de skins de um Toon, com categoria e como conseguir. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkinsGrid(skins: List<ToonSkin>, accent: Color) {
    val w = cardWidth()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        skins.forEach { skin ->
            Column(
                Modifier
                    .width(w)
                    .background(AppColors.Background.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                RemoteImage(skin.image, skin.name, Modifier.fillMaxWidth().height(w * 1.25f))
                Text(skin.name, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Badge(skin.category, accent)
                Text(skin.howTo, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
            }
        }
    }
}

/** Grade de Toons (foto + nome + custo). Tocar abre a página do Toon. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToonsGrid(toons: List<Toon>, accent: Color, onToonClick: (String) -> Unit) {
    val w = cardWidth()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        toons.forEach { toon ->
            Column(
                Modifier
                    .width(w)
                    .background(AppColors.SurfaceHigh, RoundedCornerShape(18.dp))
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                    .clickable { onToonClick(toon.id) }
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RemoteImage(toon.image, toon.name, Modifier.fillMaxWidth().height(w))
                Text(toon.name, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(toon.cost, style = MaterialTheme.typography.bodyMedium, color = accent, textAlign = TextAlign.Center, maxLines = 2)
            }
        }
    }
}

@Composable
private fun ImageCard(url: String, caption: String, subtitle: String, accent: Color, w: Dp) {
    Column(
        Modifier
            .width(w)
            .background(AppColors.Background.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RemoteImage(url, caption, Modifier.fillMaxWidth().height(w * 1.25f))
        if (caption.isNotBlank()) {
            Text(caption, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary, textAlign = TextAlign.Center)
        }
        if (subtitle.isNotBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
        }
    }
}
