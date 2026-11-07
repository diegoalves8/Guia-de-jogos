package com.guiadejogos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.data.model.GalleryImage
import com.guiadejogos.app.data.model.Toon
import com.guiadejogos.app.data.model.ToonSkin
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens

/** Largura dos cartões de imagem: maiores no tablet e no carro. */
@Composable
fun mediaCardWidth(): Dp = if (rememberScreenInfo().widthDp >= 600) 184.dp else 156.dp

/**
 * Cartão de personagem/item: a imagem tem prioridade; abaixo, nome, selo e uma linha de detalhe.
 */
@Composable
fun MediaCard(
    image: String,
    title: String,
    badge: String = "",
    detail: String = "",
    width: Dp = mediaCardWidth(),
    imageRatio: Float = 1f,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    // Dp.Unspecified = ocupa a célula da grade.
    val sized = if (width == Dp.Unspecified) modifier.fillMaxWidth() else modifier.width(width)
    val base = sized.clip(RoundedCornerShape(Dimens.CardRadius)).background(AppColors.Surface)
    Column(if (onClick != null) base.pressable(onClick) else base) {
        if (image.isNotBlank()) {
            Box(
                Modifier.fillMaxWidth().aspectRatio(imageRatio)
                    .background(Brush.verticalGradient(listOf(Color(0xFF222B3E), AppColors.Surface))),
            ) {
                RemoteImage(image, title, Modifier.fillMaxSize().padding(10.dp), framed = false)
            }
        }
        Column(Modifier.padding(start = 14.dp, end = 14.dp, top = if (image.isBlank()) 14.dp else 2.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (badge.isNotBlank()) Pill(badge, AppColors.Secondary)
            if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Grade de personagens (Toons, Brawlers, frutas...). Tocar abre a página do personagem. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToonsGrid(toons: List<Toon>, accent: Color = AppColors.Primary, onToonClick: (String) -> Unit) {
    val w = mediaCardWidth()
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.Gap), verticalArrangement = Arrangement.spacedBy(Dimens.Gap), modifier = Modifier.fillMaxWidth()) {
        toons.forEach { t ->
            MediaCard(t.image, t.name, badge = t.kind.substringBefore(" · "), detail = t.cost, width = w) { onToonClick(t.id) }
        }
    }
}

/** Grade de imagens com legenda (skins Vintage, armas, pets...). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryView(images: List<GalleryImage>) {
    val w = mediaCardWidth()
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.Gap), verticalArrangement = Arrangement.spacedBy(Dimens.Gap), modifier = Modifier.fillMaxWidth()) {
        images.forEach { MediaCard(it.image, it.caption, detail = it.subtitle, width = w, imageRatio = 0.85f) }
    }
}

/** Grade de skins de um personagem, com categoria e como conseguir. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkinsGrid(skins: List<ToonSkin>, accent: Color = AppColors.Primary) {
    val w = mediaCardWidth()
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.Gap), verticalArrangement = Arrangement.spacedBy(Dimens.Gap), modifier = Modifier.fillMaxWidth()) {
        skins.forEach { MediaCard(it.image, it.name, badge = it.category, detail = it.howTo, width = w, imageRatio = 0.85f) }
    }
}
