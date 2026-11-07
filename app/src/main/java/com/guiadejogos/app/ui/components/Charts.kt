package com.guiadejogos.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.ui.theme.AppColors

/** Um valor de gráfico. */
data class ChartEntry(val label: String, val value: Float, val color: Color)

/** Formata números grandes (1.234 → "1,2 mil"). */
fun compactNumber(v: Int): String = when {
    v >= 1_000_000 -> String.format("%.1f mi", v / 1_000_000f).replace('.', ',')
    v >= 10_000 -> "${v / 1000} mil"
    v >= 1_000 -> String.format("%.1f mil", v / 1000f).replace('.', ',')
    else -> v.toString()
}

/** Cartão base dos painéis. */
@Composable
fun DashCard(title: String, subtitle: String = "", modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .background(AppColors.Surface, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
        }
        content()
    }
}

/** Indicador numérico (KPI) com ícone e destaque de cor. */
@Composable
fun StatTile(emoji: String, value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.28f), AppColors.Surface)), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            Modifier.size(40.dp).background(accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, style = MaterialTheme.typography.titleLarge) }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Barras horizontais animadas (ex.: personagens por jogo). */
@Composable
fun HorizontalBarChart(entries: List<ChartEntry>, valueFormatter: (Float) -> String = { compactNumber(it.toInt()) }) {
    val max = (entries.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
    val anim = remember { Animatable(0f) }
    LaunchedEffect(entries) { anim.snapTo(0f); anim.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        entries.forEach { e ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(e.label, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    Text(valueFormatter(e.value), style = MaterialTheme.typography.labelLarge, color = e.color)
                }
                Box(
                    Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(50)).background(AppColors.SurfaceHigh),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction = (e.value / max * anim.value).coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(e.color.copy(alpha = 0.7f), e.color))),
                    )
                }
            }
        }
    }
}

/** Gráfico de rosca animado com legenda. */
@Composable
fun DonutChart(entries: List<ChartEntry>, centerValue: String, centerLabel: String, wide: Boolean) {
    val total = entries.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    val anim = remember { Animatable(0f) }
    LaunchedEffect(entries) { anim.snapTo(0f); anim.animateTo(1f, tween(1100, easing = FastOutSlowInEasing)) }
    val chart: @Composable () -> Unit = {
        Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(170.dp)) {
                val stroke = 26.dp.toPx()
                val d = size.minDimension - stroke
                val tl = Offset((size.width - d) / 2, (size.height - d) / 2)
                drawArc(AppColors.SurfaceHigh, 0f, 360f, false, tl, Size(d, d), style = Stroke(stroke))
                var start = -90f
                entries.forEach { e ->
                    val sweep = e.value / total * 360f * anim.value
                    drawArc(e.color, start, (sweep - 2f).coerceAtLeast(0.5f), false, tl, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Butt))
                    start += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(centerValue, style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
                Text(centerLabel, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
            }
        }
    }
    val legend: @Composable (Modifier) -> Unit = { mod ->
        Column(mod, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            entries.forEach { e ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).background(e.color, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(e.label, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    Text(
                        "${(e.value / total * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
    if (wide) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            chart(); Spacer(Modifier.width(24.dp)); legend(Modifier.weight(1f))
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            chart(); legend(Modifier.fillMaxWidth())
        }
    }
}
