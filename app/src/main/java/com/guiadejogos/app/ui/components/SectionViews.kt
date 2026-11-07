package com.guiadejogos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.data.model.CodeStatus
import com.guiadejogos.app.data.model.GameCode
import com.guiadejogos.app.data.model.InfoItem
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.ui.theme.AppColors

/**
 * Desenha qualquer bloco de conteúdo.
 * Para criar um novo tipo de bloco: adicione em Models.kt, ContentParser.kt e aqui.
 */
@Composable
fun SectionView(section: Section, accent: Color, onCopied: (String) -> Unit) {
    SectionContainer(section.title) {
        when (section) {
            is Section.Text -> Text(section.body, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
            is Section.Notice -> NoticeView(section)
            is Section.Steps -> StepsView(section.items, accent)
            is Section.Codes -> CodesView(section.codes, onCopied)
            is Section.Items -> section.items.forEach { ItemView(it, accent) }
            is Section.Levels -> LevelsView(section, accent)
            is Section.Links -> LinksView(section)
        }
    }
}

@Composable
private fun SectionContainer(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(AppColors.SurfaceHigh.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (title.isNotBlank()) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
        }
        content()
    }
}

@Composable
private fun NoticeView(notice: Section.Notice) {
    val color = when (notice.tone) {
        "warning" -> AppColors.Accent
        "success" -> AppColors.Success
        else -> AppColors.Secondary
    }
    Text(
        notice.body,
        style = MaterialTheme.typography.bodyLarge,
        color = AppColors.TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(16.dp),
    )
}

@Composable
private fun StepsView(items: List<String>, accent: Color) {
    items.forEachIndexed { index, step ->
        Row(verticalAlignment = Alignment.Top) {
            Text(
                "${index + 1}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .size(34.dp)
                    .background(accent, CircleShape)
                    .padding(top = 5.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.width(14.dp))
            Text(step, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun CodesView(codes: List<GameCode>, onCopied: (String) -> Unit) {
    if (codes.isEmpty()) {
        Text("Nenhum código cadastrado no momento.", color = AppColors.TextSecondary)
        return
    }
    val clipboard = LocalClipboardManager.current
    codes.forEach { code ->
        val (label, color) = when (code.status) {
            CodeStatus.ACTIVE -> "ATIVO" to AppColors.Success
            CodeStatus.EXPIRED -> "EXPIRADO" to AppColors.Danger
            CodeStatus.UNVERIFIED -> "NÃO VERIFICADO" to AppColors.Accent
        }
        val expired = code.status == CodeStatus.EXPIRED
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .background(AppColors.Background.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    code.code,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (expired) AppColors.TextSecondary else AppColors.TextPrimary,
                    textDecoration = if (expired) TextDecoration.LineThrough else null,
                )
                if (code.reward.isNotBlank()) {
                    Text("🎁 ${code.reward}", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                }
                if (code.note.isNotBlank()) {
                    Text(code.note, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                }
                Badge(label, color)
            }
            if (!expired) {
                IconButton(onClick = {
                    clipboard.setText(AnnotatedString(code.code))
                    onCopied(code.code)
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar código", tint = AppColors.Secondary)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemView(item: InfoItem, accent: Color) {
    var expanded by remember { mutableStateOf(false) }
    val hasDetails = item.fields.isNotEmpty()
    Column(
        Modifier
            .fillMaxWidth()
            .background(AppColors.Background.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable(enabled = hasDetails) { expanded = !expanded }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.name, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
            if (hasDetails) {
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = accent)
            }
        }
        if (item.tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item.tags.forEach { Badge(it, accent) }
            }
        }
        if (item.description.isNotBlank()) {
            Text(item.description, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
        }
        if (expanded) {
            item.fields.forEach { (label, value) ->
                Column(Modifier.padding(top = 4.dp)) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = accent)
                    Text(value, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun LevelsView(section: Section.Levels, accent: Color) {
    section.levels.forEach { level ->
        var expanded by remember { mutableStateOf(false) }
        Column(
            Modifier
                .fillMaxWidth()
                .background(AppColors.Background.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(level.level, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Text("${level.tips.size} dicas", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = accent)
            }
            if (expanded) {
                if (level.tips.isEmpty()) {
                    Text("Dicas deste nível em breve.", color = AppColors.TextSecondary)
                }
                level.tips.forEach { tip ->
                    Text("• $tip", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun LinksView(section: Section.Links) {
    val uriHandler = LocalUriHandler.current
    section.links.forEach { link ->
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(AppColors.Background.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .clickable { runCatching { uriHandler.openUri(link.url) } }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(link.label, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                Badge(if (link.official) "OFICIAL" else "COMUNIDADE", if (link.official) AppColors.Success else AppColors.TextSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Abrir link", tint = AppColors.Secondary)
        }
    }
}
