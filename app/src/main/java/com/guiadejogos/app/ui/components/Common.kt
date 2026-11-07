package com.guiadejogos.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens

// ====================================================================
// Microinteração: cartões "afundam" levemente ao toque.
// ====================================================================

/** Clique com feedback de escala (animação discreta ao selecionar cartões). */
@Composable
fun Modifier.pressable(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed) 0.97f else 1f, spring(stiffness = 600f), label = "press")
    return this
        .scale(s)
        .clickable(interactionSource = source, indication = androidx.compose.material3.ripple(), onClick = onClick)
}

// ====================================================================
// Estrutura de tela
// ====================================================================

/**
 * Estrutura padrão das telas internas: cabeçalho limpo (voltar + título + ações)
 * e conteúdo centralizado com largura máxima.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable () -> Unit = {},
    maxContentWidthDp: Int = AppConfig.MAX_CONTENT_WIDTH_DP,
    /** true nas abas principais: a barra de navegação inferior já cuida do espaço de baixo. */
    inShell: Boolean = false,
    showTopBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = AppColors.Background,
        contentWindowInsets = if (inShell) WindowInsets.statusBars else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack, modifier = Modifier.size(Dimens.TouchMin)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", modifier = Modifier.size(28.dp))
                            }
                        }
                    },
                    actions = { actions() },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = AppColors.Background,
                        scrolledContainerColor = AppColors.Background,
                        titleContentColor = AppColors.TextPrimary,
                        navigationIconContentColor = AppColors.TextPrimary,
                        actionIconContentColor = AppColors.TextPrimary,
                    ),
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.widthIn(max = maxContentWidthDp.dp).fillMaxSize()) { content(padding) }
        }
    }
}

@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.Primary, strokeWidth = 3.dp)
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    EmptyState("⚠️", "Algo deu errado", message, actionLabel = "Tentar novamente", onAction = onRetry)
}

/** Estado vazio elegante (sem resultados, sem favoritos...). */
@Composable
fun EmptyState(emoji: String, title: String, message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(emoji, style = MaterialTheme.typography.displayLarge)
        Text(title, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(6.dp))
            SecondaryButton(actionLabel, onClick = onAction)
        }
    }
}

// ====================================================================
// Indicadores
// ====================================================================

/** Selo pequeno e discreto (raridade, "Popular", "Oficial"...). */
@Composable
fun Badge(text: String, color: Color) = Pill(text, color)

@Composable
fun Chip(text: String, color: Color) = Pill(text, color)

@Composable
fun Pill(text: String, color: Color = AppColors.TextSecondary, filled: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (filled) Color.White else color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .background(if (filled) color else color.copy(alpha = 0.14f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

/** Ícone dentro de um quadrado suave. */
@Composable
fun IconBubble(icon: ImageVector, tint: Color = AppColors.Primary, size: Int = 44) {
    Box(
        Modifier.size(size.dp).background(tint.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.55f).dp)) }
}

// ====================================================================
// Botões
// ====================================================================

@Composable
fun PrimaryButton(text: String, icon: ImageVector? = null, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary, contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
        modifier = modifier.heightIn(min = Dimens.TouchMin),
    ) {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(text: String, icon: ImageVector? = null, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = AppColors.SurfaceHigh, contentColor = AppColors.TextPrimary),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier.heightIn(min = Dimens.TouchMin),
    ) {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ====================================================================
// Pesquisa, filtros e cabeçalhos de seção
// ====================================================================

/** Barra de pesquisa "falsa" (abre a tela de busca), estilo Play Store. */
@Composable
fun SearchBarButton(placeholder: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .heightIn(min = Dimens.TouchMin)
            .clip(RoundedCornerShape(50))
            .background(AppColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, null, tint = AppColors.TextSecondary)
        Spacer(Modifier.width(14.dp))
        Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary, maxLines = 1)
    }
}

/** Campo de pesquisa real. */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
) {
    Row(
        modifier
            .heightIn(min = Dimens.TouchMin)
            .clip(RoundedCornerShape(50))
            .background(AppColors.Surface)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, null, tint = AppColors.TextSecondary)
        Spacer(Modifier.width(14.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary, maxLines = 1)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = AppColors.TextPrimary),
                cursorBrush = SolidColor(AppColors.Primary),
                modifier = Modifier.fillMaxWidth().let { if (focusRequester != null) it.focusRequester(focusRequester) else it },
            )
        }
        if (value.isNotEmpty()) {
            IconButton(onClick = { onValueChange("") }) { Icon(Icons.Filled.Close, "Limpar", tint = AppColors.TextSecondary) }
        }
    }
}

/** Linha de filtros (chips) com transição de cor na seleção. */
@Composable
fun FilterChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit, contentPadding: PaddingValues = PaddingValues(0.dp)) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = contentPadding) {
        items(options) { opt ->
            val isSel = opt == selected
            val bg by animateColorAsState(if (isSel) AppColors.Primary else AppColors.Surface, label = "chip")
            val fg by animateColorAsState(if (isSel) Color.White else AppColors.TextSecondary, label = "chipText")
            Text(
                opt,
                style = MaterialTheme.typography.labelLarge,
                color = fg,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .border(1.dp, if (isSel) Color.Transparent else AppColors.Outline, RoundedCornerShape(50))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 20.dp, vertical = 13.dp),
            )
        }
    }
}

/** Título de seção com ação opcional ("Ver tudo"). */
@Composable
fun SectionHeader(title: String, subtitle: String = "", action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
        }
        if (action != null && onAction != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.Primary,
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onAction).padding(horizontal = 14.dp, vertical = 12.dp),
            )
        }
    }
}

/** Cabeçalho de página de categoria (emoji + título + descrição), sem cores fortes. */
@Composable
fun HeaderBanner(emoji: String, title: String, subtitle: String, color: Color = AppColors.Primary) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(AppColors.Surface, RoundedCornerShape(Dimens.CardRadius))
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(56.dp).background(color.copy(alpha = 0.14f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
