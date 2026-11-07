package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import com.guiadejogos.app.ui.theme.AppColors

/** Tela inicial: nome do app, descrição e o botão gigante ENTRAR. */
@Composable
fun HomeScreen(onEnter: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.Primary.copy(alpha = 0.35f), AppColors.Background, AppColors.Background),
                ),
            )
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🎮", fontSize = 84.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "GUIA DE JOGOS",
                style = MaterialTheme.typography.displayLarge,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Códigos, dicas, guias, skins, novidades e informações dos seus jogos favoritos — tudo em um só lugar.",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(48.dp))

            // Botão MUITO grande, fácil de tocar em qualquer tela.
            Button(
                onClick = onEnter,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp),
            ) {
                Text("ENTRAR", fontSize = 34.sp, style = MaterialTheme.typography.displayLarge, color = AppColors.TextPrimary)
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "Versão ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary.copy(alpha = 0.6f),
            )
        }
    }
}
