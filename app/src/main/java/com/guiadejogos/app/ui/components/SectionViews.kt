package com.guiadejogos.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.data.model.CodeStatus
import com.guiadejogos.app.data.model.GameCode
import com.guiadejogos.app.data.model.InfoItem
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens

/**
 * Desenha qualquer bloco de conteúdo com o mesmo padrão visual.
 * Para criar um novo tipo de bloco: adicione em Models.kt, ContentParser.kt e aqui.
 */
@Composable
fun SectionView(
    section: Section,
    accent: Color = AppColors.Primary,
    onCopied: (String) -> Unit,
    onToonClick: (String) -> Unit = {},
) {
    // Grades de imagens ficam "soltas" (sem cartão em volta) para dar destaque às fotos.
    if (section is Section.Toons || section is Section.Gallery) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (section.title.isNotBlank()) Text(section.title, style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
            when (section) {
                is Section.Toons -> ToonsGrid(section.toons, accent, onToonClick)
                is Section.Gallery -> GalleryView(section.images)
                else -> Unit
            }
        }
        return
    }
    SectionContainer(section.title) {
        when (section) {
            is Section.Text -> Text(section.body, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
            is Section.Notice -> NoticeView(section)
            is Section.Steps -> StepsView(section.items)
            is Section.Codes -> CodesView(section.codes, onCopied)
            is Section.Items -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { section.items.forEach { ItemView(it) } }
            is Section.Levels -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { section.levels.forEach { LevelView(it.level, it.tips) } }
            is Section.Links -> LinksView(section)
            is Section.Creators -> CreatorsView(section.creators)
            else -> Unit
        }
    }
}

@Composable
private fun SectionContainer(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(AppColors.Surface, RoundedCornerShape(Dimens.CardRadius)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (title.isNotBlank()) Text(title, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
        content()
    }
}

@Composable
private fun NoticeView(notice: Section.Notice) {
    val color = when (notice.tone) {
        "warning" -> AppColors.Accent
        "success" -> AppColors.Success
        else -> AppColors.Primary
    }
    Row(
        Modifier.fillMaxWidth().background(color.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(16.dp),
    ) {
        Box(Modifier.padding(top = 6.dp).size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(notice.body, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
    }
}

@Composable
private fun StepsView(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items.forEachIndexed { index, step ->
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(36.dp).background(AppColors.Primary.copy(alpha = 0.16f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = AppColors.Primary, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(16.dp))
                Text(step, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary, modifier = Modifier.padding(top = 5.dp))
            }
        }
    }
}

@Composable
private fun CodesView(codes: List<GameCode>, onCopied: (String) -> Unit) {
    if (codes.isEmpty()) {
        Text("Nenhum código cadastrado no momento.", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
        return
    }
    val clipboard = LocalClipboardManager.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        codes.forEach { code ->
            val (label, color) = when (code.status) {
                CodeStatus.ACTIVE -> "Ativo" to AppColors.Success
                CodeStatus.EXPIRED -> "Expirado" to AppColors.TextSecondary
                CodeStatus.UNVERIFIED -> "Não verificado" to AppColors.Accent
            }
            val expired = code.status == CodeStatus.EXPIRED
            Row(
                Modifier.fillMaxWidth().heightIn(min = 68.dp).background(AppColors.SurfaceHigh, RoundedCornerShape(16.dp)).padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            code.code, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge,
                            color = if (expired) AppColors.TextSecondary else AppColors.TextPrimary,
                            textDecoration = if (expired) TextDecoration.LineThrough else null,
                        )
                        Spacer(Modifier.width(12.dp))
                        Pill(label, color)
                    }
                    if (code.reward.isNotBlank()) Text("🎁 ${code.reward}", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                    if (code.note.isNotBlank()) Text(code.note, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                }
                if (!expired) {
                    IconButton(onClick = { clipboard.setText(AnnotatedString(code.code)); onCopied(code.code) }, modifier = Modifier.size(Dimens.TouchMin)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar código", tint = AppColors.Primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemView(item: InfoItem) {
    var expanded by remember { mutableStateOf(false) }
    val hasDetails = item.fields.isNotEmpty()
    val rot by animateFloatAsState(if (expanded) 180f else 0f, label = "rot")
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AppColors.SurfaceHigh)
            .clickable(enabled = hasDetails) { expanded = !expanded }.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.image.isNotBlank()) {
                RemoteImage(item.image, item.name, Modifier.size(84.dp))
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                if (item.tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.tags.forEach { Pill(it, AppColors.Secondary) }
                    }
                }
            }
            if (hasDetails) Icon(Icons.Filled.ExpandMore, null, tint = AppColors.TextSecondary, modifier = Modifier.rotate(rot))
        }
        if (item.description.isNotBlank()) Text(item.description, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
        AnimatedVisibility(expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                item.fields.forEach { (label, value) ->
                    Column {
                        Text(label, style = MaterialTheme.typography.labelLarge, color = AppColors.Primary)
                        Text(value, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelView(level: String, tips: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    val rot by animateFloatAsState(if (expanded) 180f else 0f, label = "rot")
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AppColors.SurfaceHigh)
            .clickable { expanded = !expanded }.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(level, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
            Pill("${tips.size} dicas", AppColors.TextSecondary)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.ExpandMore, null, tint = AppColors.TextSecondary, modifier = Modifier.rotate(rot))
        }
        AnimatedVisibility(expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (tips.isEmpty()) Text("Dicas deste nível em breve.", color = AppColors.TextSecondary)
                tips.forEach { tip ->
                    Row {
                        Box(Modifier.padding(top = 9.dp).size(6.dp).background(AppColors.Primary, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Text(tip, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinksView(section: Section.Links) {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        section.links.forEach { link ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 64.dp).clip(RoundedCornerShape(16.dp)).background(AppColors.SurfaceHigh)
                    .pressable { runCatching { uriHandler.openUri(link.url) } }.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(link.label, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                    Pill(if (link.official) "Oficial" else "Comunidade", if (link.official) AppColors.Success else AppColors.TextSecondary)
                }
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Abrir link", tint = AppColors.Primary)
            }
        }
    }
}

/** Cartões de criadores de conteúdo com botões diretos para YouTube e TikTok. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorsView(creators: List<com.guiadejogos.app.data.model.Creator>) {
    val uri = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        creators.forEach { c ->
            Column(
                Modifier.fillMaxWidth().background(AppColors.SurfaceHigh, RoundedCornerShape(18.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(AppColors.Primary, AppColors.Secondary)), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text(c.name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = Color.White) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(c.name, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
                        if (c.country.isNotBlank()) Pill(c.country, if (c.country.contains("Brasil")) AppColors.Success else AppColors.TextSecondary)
                    }
                }
                if (c.about.isNotBlank()) Text(c.about, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (c.youtube.isNotBlank()) SecondaryButton("YouTube", Icons.Filled.PlayCircle) { runCatching { uri.openUri(c.youtube) } }
                    if (c.tiktok.isNotBlank()) SecondaryButton("TikTok", Icons.Filled.MusicNote) { runCatching { uri.openUri(c.tiktok) } }
                }
                if (c.source.isNotBlank()) Text("Fonte: ${c.source}", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary.copy(alpha = 0.7f))
            }
        }
    }
}
