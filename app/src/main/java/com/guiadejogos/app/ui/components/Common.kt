package com.guiadejogos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.ui.theme.AppColors

/**
 * Estrutura padrão das telas internas: barra superior com botão VOLTAR,
 * fundo em degradê e conteúdo centralizado com largura máxima (fica bonito no tablet).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable () -> Unit = {},
    /** Largura máxima do conteúdo: grades usam mais espaço (tablet/carro), textos menos. */
    maxContentWidthDp: Int = AppConfig.MAX_CONTENT_WIDTH_DP,
    /** true nas abas principais: a barra de navegação inferior já cuida do espaço de baixo. */
    inShell: Boolean = false,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = AppColors.Background,
        contentWindowInsets = if (inShell) WindowInsets.statusBars else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    if (onBack != null) {
                        // Botão maior que o padrão: fácil de acertar no tablet e na multimídia do carro.
                        IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", modifier = Modifier.size(30.dp))
                        }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.Background,
                    titleContentColor = AppColors.TextPrimary,
                    navigationIconContentColor = AppColors.TextPrimary,
                    actionIconContentColor = AppColors.TextPrimary,
                ),
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(AppColors.Background, AppColors.Surface))),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(Modifier.widthIn(max = maxContentWidthDp.dp).fillMaxSize()) {
                content(padding)
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.Primary)
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Ops! Algo deu errado.", style = MaterialTheme.typography.headlineSmall)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        Button(onClick = onRetry) { Text("Tentar novamente") }
    }
}

/** Selo pequeno (ex.: "EM BREVE", "OFICIAL", "ATIVO"). */
@Composable
fun Badge(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.75f),
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/**
 * Faixa de cabeçalho usada nas páginas de jogo e categoria.
 * Em telas baixas (multimídia em paisagem) vira uma versão compacta em linha.
 */
@Composable
fun HeaderBanner(emoji: String, title: String, subtitle: String, color: androidx.compose.ui.graphics.Color) {
    val compact = rememberScreenInfo().isShortHeight
    val background = Modifier
        .fillMaxWidth()
        .background(
            Brush.linearGradient(listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0.12f))),
            RoundedCornerShape(24.dp),
        )
    if (compact) {
        Row(background.padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        return
    }
    Column(background.padding(24.dp)) {
        Text(emoji, style = MaterialTheme.typography.displayLarge)
        Text(title, style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
        if (subtitle.isNotBlank()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
