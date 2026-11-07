package com.guiadejogos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.guiadejogos.app.ui.theme.AppColors

/**
 * Imagem carregada da internet (com cache no aparelho via Coil).
 * Depois de vista uma vez, a imagem continua aparecendo mesmo sem internet.
 */
@Composable
fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .clip(shape)
            .background(AppColors.Background.copy(alpha = 0.55f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isBlank()) {
            Text("🖼️")
        } else {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = contentScale,
                loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp, color = AppColors.Primary) } },
                error = { Box(contentAlignment = Alignment.Center) { Text("🖼️") } },
            )
        }
    }
}
