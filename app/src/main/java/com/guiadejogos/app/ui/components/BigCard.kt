package com.guiadejogos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.ui.theme.AppColors

/**
 * Botão grande em forma de card — usado para jogos e categorias.
 * Área de toque generosa (mín. 96dp de altura).
 */
@Composable
fun BigCard(
    emoji: String,
    title: String,
    subtitle: String,
    accent: Color,
    badge: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.SurfaceHigh,
            disabledContainerColor = AppColors.SurfaceHigh,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .alpha(if (enabled) 1f else 0.6f),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.22f), Color.Transparent)))
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                emoji,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .background(accent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(10.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                if (badge != null) {
                    Spacer(Modifier.size(8.dp))
                    Badge(badge, AppColors.Accent)
                }
            }
            if (enabled) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}
