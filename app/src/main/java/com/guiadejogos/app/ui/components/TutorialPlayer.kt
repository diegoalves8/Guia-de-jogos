package com.guiadejogos.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.data.model.TutorialStep
import com.guiadejogos.app.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * "Mini vídeo" do tutorial: os passos passam sozinhos, com animação,
 * barra de progresso estilo stories e controles de play/pause/voltar/avançar.
 */
@Composable
fun TutorialPlayer(steps: List<TutorialStep>, accent: Color, stepDurationMs: Int = 5500) {
    if (steps.isEmpty()) return
    var index by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(true) }
    val progress = remember { Animatable(0f) }
    val finished = index == steps.lastIndex && progress.value >= 1f

    // Avança automaticamente; reinicia a barra a cada passo.
    LaunchedEffect(index, playing) {
        if (!playing) return@LaunchedEffect
        val remaining = ((1f - progress.value) * stepDurationMs).toInt().coerceAtLeast(1)
        progress.animateTo(1f, tween(remaining, easing = LinearEasing))
        if (index < steps.lastIndex) {
            progress.snapTo(0f)
            index++
        } else {
            playing = false
        }
    }

    val scope = rememberCoroutineScope()
    fun goTo(i: Int, play: Boolean = playing) {
        scope.launch {
            progress.snapTo(0f)
            index = i.coerceIn(0, steps.lastIndex)
            playing = play
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(AppColors.Background, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Barras de progresso (uma por passo).
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            steps.indices.forEach { i ->
                LinearProgressIndicator(
                    progress = { if (i < index) 1f else if (i == index) progress.value else 0f },
                    color = accent,
                    trackColor = AppColors.SurfaceHigh,
                    modifier = Modifier.weight(1f).height(5.dp),
                )
            }
        }

        AnimatedContent(
            targetState = index,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * dir / 3 } + fadeIn() + scaleIn(initialScale = 0.96f))
                    .togetherWith(slideOutHorizontally { -it * dir / 3 } + fadeOut())
            },
            label = "tutorial",
        ) { i ->
            val step = steps[i]
            val wide = rememberScreenInfo().widthDp >= 600
            if (wide) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RemoteImage(step.image, step.title, Modifier.size(200.dp))
                    Spacer(Modifier.width(20.dp))
                    StepText(step, Modifier.weight(1f))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RemoteImage(step.image, step.title, Modifier.size(180.dp))
                    Spacer(Modifier.height(12.dp))
                    StepText(step, Modifier.fillMaxWidth())
                }
            }
        }

        // Controles grandes (fáceis no tablet e no carro).
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { goTo(index - 1) }, enabled = index > 0, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Passo anterior", modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(12.dp))
            FilledIconButton(
                onClick = {
                    if (finished) {
                        goTo(0, play = true)
                    } else {
                        playing = !playing
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = accent),
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    when {
                        finished -> Icons.Filled.Replay
                        playing -> Icons.Filled.Pause
                        else -> Icons.Filled.PlayArrow
                    },
                    contentDescription = if (playing) "Pausar" else "Reproduzir",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = { goTo(index + 1) }, enabled = index < steps.lastIndex, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Próximo passo", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun StepText(step: TutorialStep, modifier: Modifier) {
    Box(modifier.heightIn(min = 120.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(step.title, style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
            Text(step.text, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
        }
    }
}
