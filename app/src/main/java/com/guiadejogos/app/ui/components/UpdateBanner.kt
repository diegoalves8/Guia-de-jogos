package com.guiadejogos.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.data.update.AppUpdater
import com.guiadejogos.app.ui.theme.AppColors
import com.guiadejogos.app.ui.theme.Dimens
import kotlinx.coroutines.launch
import java.io.File

/** Resultado da verificação feita uma vez por abertura do app (compartilhado entre telas). */
object UpdateState {
    var checked = false
    var available: AppUpdater.CheckResult.Available? = null
}

/**
 * Aviso de atualização na tela inicial: aparece sozinho quando há versão nova
 * e baixa + instala com um toque.
 */
@Composable
fun UpdateBanner() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var info by remember { mutableStateOf(UpdateState.available) }
    var progress by remember { mutableFloatStateOf(-1f) }
    var apk by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!UpdateState.checked) {
            UpdateState.checked = true
            val r = AppUpdater.check()
            if (r is AppUpdater.CheckResult.Available) { UpdateState.available = r; info = r }
        }
    }

    AnimatedVisibility(info != null, enter = expandVertically() + fadeIn()) {
        val i = info ?: return@AnimatedVisibility
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(AppColors.Primary.copy(alpha = 0.28f), AppColors.Secondary.copy(alpha = 0.18f))), RoundedCornerShape(Dimens.CardRadius))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.SystemUpdate)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Nova versão disponível", style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
                    Text(i.versionName, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                }
                when {
                    apk != null -> PrimaryButton("Instalar") {
                        if (AppUpdater.canInstall(context)) AppUpdater.install(context, apk!!) else AppUpdater.openInstallPermissionSettings(context)
                    }
                    progress < 0f -> PrimaryButton("Atualizar agora", Icons.Filled.CloudDownload) {
                        progress = 0f; error = ""
                        scope.launch {
                            runCatching { AppUpdater.download(context, i.apkUrl) { p -> progress = p } }
                                .onSuccess { apk = it }
                                .onFailure { error = it.message ?: "Falha no download"; progress = -1f }
                        }
                    }
                }
            }
            if (progress >= 0f && apk == null) {
                Text("Baixando… ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = AppColors.Primary)
            }
            if (apk != null && !AppUpdater.canInstall(context)) {
                Text("Na primeira vez, permita \"Instalar apps desta fonte\" para o Guia de Jogos, volte e toque em Instalar.", style = MaterialTheme.typography.bodyMedium, color = AppColors.Accent)
            }
            if (error.isNotBlank()) Text(error, style = MaterialTheme.typography.bodyMedium, color = AppColors.Danger)
        }
    }
}
