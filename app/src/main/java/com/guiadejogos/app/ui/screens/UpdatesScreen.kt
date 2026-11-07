package com.guiadejogos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guiadejogos.app.BuildConfig
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.data.update.AppUpdater
import com.guiadejogos.app.ui.components.AppScreen
import com.guiadejogos.app.ui.theme.AppColors
import kotlinx.coroutines.launch
import java.io.File

private sealed interface AppUpdateUi {
    data object Checking : AppUpdateUi
    data object UpToDate : AppUpdateUi
    data class Available(val info: AppUpdater.CheckResult.Available) : AppUpdateUi
    data class Downloading(val progress: Float) : AppUpdateUi
    data class Ready(val apk: File) : AppUpdateUi
    data class Problem(val message: String) : AppUpdateUi
}

/** Aba Atualizar: atualiza o app (APK) e o conteúdo, tudo por dentro do aplicativo. */
@Composable
fun UpdatesScreen(repository: ContentRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var app by remember { mutableStateOf<AppUpdateUi>(AppUpdateUi.Checking) }
    var contentMsg by remember { mutableStateOf("") }
    var contentBusy by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    fun check() {
        app = AppUpdateUi.Checking
        scope.launch {
            app = when (val r = AppUpdater.check()) {
                is AppUpdater.CheckResult.Available -> AppUpdateUi.Available(r)
                AppUpdater.CheckResult.UpToDate -> AppUpdateUi.UpToDate
                is AppUpdater.CheckResult.Unavailable -> AppUpdateUi.Problem(r.reason)
            }
        }
    }
    LaunchedEffect(Unit) { check() }

    AppScreen(title = "Atualizar", onBack = null, inShell = true) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = padding.calculateTopPadding() + 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                UpdateCard(Icons.Filled.SystemUpdate, "Aplicativo", "Versão instalada: ${BuildConfig.VERSION_NAME}", AppColors.Primary) {
                    when (val s = app) {
                        AppUpdateUi.Checking -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.padding(end = 12.dp), strokeWidth = 2.dp)
                            Text("Procurando atualização…", color = AppColors.TextSecondary)
                        }
                        AppUpdateUi.UpToDate -> {
                            Text("✅ Você já está com a versão mais nova.", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                            OutlinedButton(onClick = { check() }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Procurar de novo") }
                        }
                        is AppUpdateUi.Available -> {
                            Text("🎉 Nova versão disponível: ${s.info.versionName}", style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            if (s.info.notes.isNotBlank()) Text(s.info.notes.take(600), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                            BigButton("Baixar e instalar", Icons.Filled.CloudDownload, AppColors.Primary) {
                                progress = 0f
                                app = AppUpdateUi.Downloading(0f)
                                scope.launch {
                                    app = runCatching {
                                        val apk = AppUpdater.download(context, s.info.apkUrl) { p -> progress = p }
                                        AppUpdateUi.Ready(apk)
                                    }.getOrElse { AppUpdateUi.Problem(it.message ?: "Falha no download") }
                                }
                            }
                        }
                        is AppUpdateUi.Downloading -> {
                            Text("Baixando… ${(progress * 100).toInt()}%", color = AppColors.TextPrimary)
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = AppColors.Primary)
                        }
                        is AppUpdateUi.Ready -> {
                            Text("Download concluído. Toque em Instalar e confirme na tela do Android. Seus dados continuam no app.", color = AppColors.TextPrimary)
                            BigButton("Instalar agora", Icons.Filled.SystemUpdate, AppColors.Success) {
                                if (AppUpdater.canInstall(context)) AppUpdater.install(context, s.apk)
                                else AppUpdater.openInstallPermissionSettings(context)
                            }
                            if (!AppUpdater.canInstall(context)) {
                                Text(
                                    "Na primeira vez, o Android pede para permitir \"Instalar apps desta fonte\" para o Guia de Jogos. Ative, volte aqui e toque em Instalar de novo.",
                                    style = MaterialTheme.typography.bodyMedium, color = AppColors.Accent,
                                )
                            }
                        }
                        is AppUpdateUi.Problem -> {
                            Text(s.message, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
                            OutlinedButton(onClick = { check() }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Tentar de novo") }
                        }
                    }
                }
            }
            item {
                UpdateCard(Icons.Filled.Refresh, "Conteúdo", "Códigos, skins, personagens e guias", AppColors.Secondary) {
                    Text(
                        "Baixa os textos e listas mais recentes sem reinstalar o app.",
                        style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary,
                    )
                    if (contentBusy) {
                        LinearProgressIndicator(Modifier.fillMaxWidth(), color = AppColors.Secondary)
                    } else {
                        BigButton("Atualizar conteúdo", Icons.Filled.Refresh, AppColors.Secondary) {
                            contentBusy = true
                            scope.launch {
                                val ok = repository.refresh()
                                contentBusy = false
                                contentMsg = if (ok) "✅ Conteúdo atualizado! Volte para Início para ver." else "Você já tem o conteúdo mais recente (ou o servidor não está acessível)."
                            }
                        }
                    }
                    if (contentMsg.isNotBlank()) Text(contentMsg, color = AppColors.TextPrimary)
                }
            }
            item {
                Text(
                    "As atualizações vêm do repositório do projeto no GitHub. O Android sempre pede sua confirmação antes de instalar.",
                    style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary.copy(alpha = 0.8f),
                )
            }
            item { Spacer(Modifier.height(padding.calculateBottomPadding())) }
        }
    }
}

@Composable
private fun UpdateCard(icon: ImageVector, title: String, subtitle: String, accent: Color, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.22f), AppColors.Surface)), RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.background(accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp)).padding(10.dp)) { Icon(icon, null, tint = accent) }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
            }
        }
        content()
    }
}

@Composable
private fun BigButton(text: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
