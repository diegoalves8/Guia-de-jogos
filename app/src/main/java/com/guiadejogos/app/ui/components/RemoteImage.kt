package com.guiadejogos.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.guiadejogos.app.ui.theme.AppColors

/**
 * Imagem da internet com cache no aparelho (Coil) e carregamento elegante:
 * um fundo suave pulsa enquanto carrega e a imagem aparece com fade.
 * Depois de vista uma vez, continua aparecendo sem internet.
 */
@Composable
fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    framed: Boolean = true,
) {
    var loaded by remember(url) { mutableStateOf(false) }
    var failed by remember(url) { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (loaded) 1f else 0f, tween(350), label = "img")
    val shape = RoundedCornerShape(16.dp)
    val base = if (framed) modifier.clip(shape).background(AppColors.SurfaceHigh, shape) else modifier
    Box(base, contentAlignment = Alignment.Center) {
        if (url.isBlank() || failed) {
            Text("🖼️")
        } else {
            if (!loaded) Box(Modifier.fillMaxSize().clip(shape).background(AppColors.SurfaceHigh.copy(alpha = 0.5f)))
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = contentScale,
                onSuccess = { loaded = true },
                onError = { failed = true },
                modifier = Modifier.fillMaxSize().alpha(alpha),
            )
        }
    }
}
