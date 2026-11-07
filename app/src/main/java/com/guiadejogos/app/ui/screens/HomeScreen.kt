package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiadejogos.app.BuildConfig
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.theme.AppColors

/**
 * Tela inicial: nome do app, descrição e o botão gigante ENTRAR.
 * - Celular / tablet / carro em retrato: tudo empilhado e centralizado.
 * - Tela larga e baixa (multimídia BYD em paisagem): título à esquerda, botão à direita.
 */
@Composable
fun HomeScreen(onEnter: () -> Unit) {
    val screen = rememberScreenInfo()
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.Primary.copy(alpha = 0.22f), AppColors.Secondary.copy(alpha = 0.08f), AppColors.Background),
                ),
            )
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        if (screen.isWideLandscape) {
            Row(
                Modifier
                    .widthIn(max = 1200.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Title(alignStart = true, compact = true)
                }
                Spacer(Modifier.width(40.dp))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    EnterButton(onEnter)
                    Spacer(Modifier.height(16.dp))
                    VersionLabel()
                }
            }
        } else {
            Column(
                Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Title(alignStart = false, compact = screen.isShortHeight)
                Spacer(Modifier.height(if (screen.isShortHeight) 24.dp else 48.dp))
                EnterButton(onEnter)
                Spacer(Modifier.height(24.dp))
                VersionLabel()
            }
        }
    }
}

@Composable
private fun Title(alignStart: Boolean, compact: Boolean) {
    val align = if (alignStart) TextAlign.Start else TextAlign.Center
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (alignStart) Alignment.Start else Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier
                .size(if (compact) 72.dp else 96.dp)
                .background(Brush.linearGradient(listOf(AppColors.Primary, AppColors.Secondary)), RoundedCornerShape(if (compact) 22.dp else 28.dp)),
            contentAlignment = Alignment.Center,
        ) { Text("🎮", fontSize = if (compact) 36.sp else 48.sp) }
        Spacer(Modifier.height(8.dp))
        Text("GUIA DE JOGOS", style = MaterialTheme.typography.displayLarge, color = AppColors.TextPrimary, textAlign = align)
        Spacer(Modifier.height(12.dp))
        Text(
            "Códigos, dicas, guias, skins, novidades e informações dos seus jogos favoritos — tudo em um só lugar.",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary,
            textAlign = align,
        )
    }
}

/** Botão MUITO grande, fácil de tocar em qualquer tela (celular, tablet ou carro). */
@Composable
private fun EnterButton(onEnter: () -> Unit) {
    Button(
        onClick = onEnter,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp),
    ) {
        Text("ENTRAR", fontSize = 32.sp, style = MaterialTheme.typography.headlineLarge, color = androidx.compose.ui.graphics.Color.White)
    }
}

@Composable
private fun VersionLabel() {
    Text(
        "Versão ${BuildConfig.VERSION_NAME}",
        style = MaterialTheme.typography.bodyMedium,
        color = AppColors.TextSecondary.copy(alpha = 0.6f),
    )
}
