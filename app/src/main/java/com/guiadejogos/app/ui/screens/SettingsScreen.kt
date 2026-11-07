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
import com.guiadejogos.app.ui.theme.Dimens
import com.guiadejogos.app.ui.components.ChartEntry
import com.guiadejogos.app.ui.components.DonutChart
import com.guiadejogos.app.ui.components.HorizontalBarChart
import com.guiadejogos.app.ui.components.PrimaryButton
import com.guiadejogos.app.ui.components.SecondaryButton
import com.guiadejogos.app.ui.components.compactNumber
import com.guiadejogos.app.ui.components.rememberScreenInfo
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
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

@OptIn(coil.annotation.ExperimentalCoilApi::class)
/** Aba Configurações: atualização do app e do conteúdo, armazenamento, estatísticas e sobre. */
@Composable
fun SettingsScreen(repository: ContentRepository, onBack: (() -> Unit)? = null) {
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
    val stats by rememberLoad(Unit) { repository.loadIndex().games }
    var cacheMsg by remember { mutableStateOf("") }

    AppScreen(title = "Configurações", onBack = onBack, inShell = onBack == null) { padding ->
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
                            SecondaryButton("Procurar de novo", Icons.Filled.Refresh, Modifier.fillMaxWidth()) { check() }
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
                            SecondaryButton("Tentar de novo", Icons.Filled.Refresh, Modifier.fillMaxWidth()) { check() }
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
                UpdateCard(Icons.Filled.Storage, "Armazenamento", "Imagens salvas para uso offline", AppColors.Secondary) {
                    Text("As fotos ficam guardadas no aparelho para abrir rápido e sem internet. Limpe se quiser liberar espaço.", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                    SecondaryButton("Limpar imagens salvas", Icons.Filled.DeleteSweep) {
                        val loader = coil.Coil.imageLoader(context)
                        loader.memoryCache?.clear()
                        loader.diskCache?.clear()
                        cacheMsg = "Imagens apagadas. Elas serão baixadas de novo quando você abrir as telas."
                    }
                    if (cacheMsg.isNotBlank()) Text(cacheMsg, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
                }
            }
            (stats as? LoadState.Success)?.data?.let { games ->
                item {
                    UpdateCard(Icons.Filled.BarChart, "Conteúdo do guia", "${games.size} jogos", AppColors.Primary) {
                        val bars = games.mapIndexed { i, g -> ChartEntry(g.name, g.stats.characters.toFloat(), AppColors.Chart[i % AppColors.Chart.size]) }
                        Text("Personagens por jogo", style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                        HorizontalBarChart(bars)
                        val slices = games.filter { it.stats.skins > 0 }.mapIndexed { i, g -> ChartEntry(g.name, g.stats.skins.toFloat(), AppColors.Chart[i % AppColors.Chart.size]) }
                        if (slices.isNotEmpty()) {
                            Text("Skins e itens", style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            DonutChart(slices, compactNumber(games.sumOf { it.stats.skins }), "no total", wide = rememberScreenInfo().widthDp >= 600)
                        }
                    }
                }
            }
            item {
                UpdateCard(Icons.Filled.Info, "Sobre", "Guia de Jogos ${BuildConfig.VERSION_NAME}", AppColors.TextSecondary) {
                    Text("Dados e imagens vêm de wikis da comunidade (Fandom, licença CC BY-SA) e de fontes oficiais. Marcas e personagens pertencem aos seus donos. Este app não é oficial de nenhum dos jogos.", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
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
            .background(AppColors.Surface, RoundedCornerShape(Dimens.CardRadius))
            .padding(22.dp),
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
    PrimaryButton(text, icon, Modifier.fillMaxWidth(), onClick)
}
