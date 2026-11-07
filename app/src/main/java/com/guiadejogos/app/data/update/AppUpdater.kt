package com.guiadejogos.app.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.guiadejogos.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Atualização do próprio app, sem precisar baixar manualmente.
 *
 * Cada build do GitHub Actions publica uma Release com a tag "build-<versionCode>"
 * e o arquivo GuiaDeJogos.apk. O app compara com o próprio versionCode,
 * baixa o APK e abre o instalador do Android (o usuário só confirma).
 */
object AppUpdater {

    sealed interface CheckResult {
        data class Available(val versionCode: Int, val versionName: String, val notes: String, val apkUrl: String) : CheckResult
        data object UpToDate : CheckResult
        data class Unavailable(val reason: String) : CheckResult
    }

    private const val APK_NAME = "GuiaDeJogos.apk"

    suspend fun check(): CheckResult = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL("https://api.github.com/repos/${BuildConfig.UPDATE_REPO}/releases/latest").openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000; readTimeout = 10_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "GuiaDeJogos-App")
            }
            try {
                when (conn.responseCode) {
                    200 -> Unit
                    404 -> return@runCatching CheckResult.Unavailable(
                        "Nenhuma versão publicada encontrada. Se o repositório do GitHub for privado, o app não consegue enxergá-lo.",
                    )
                    else -> return@runCatching CheckResult.Unavailable("O GitHub respondeu com erro ${conn.responseCode}.")
                }
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val tag = json.optString("tag_name")
                val remoteCode = tag.substringAfter("build-", "").toIntOrNull()
                    ?: return@runCatching CheckResult.Unavailable("Versão publicada em formato desconhecido ($tag).")
                val assets = json.optJSONArray("assets")
                var url = ""
                if (assets != null) for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i)
                    if (a.optString("name") == APK_NAME) url = a.optString("browser_download_url")
                }
                if (remoteCode <= BuildConfig.VERSION_CODE) CheckResult.UpToDate
                else if (url.isBlank()) CheckResult.Unavailable("A versão nova ainda não tem o APK anexado.")
                else CheckResult.Available(remoteCode, json.optString("name").ifBlank { tag }, json.optString("body"), url)
            } finally {
                conn.disconnect()
            }
        }.getOrElse { CheckResult.Unavailable("Sem conexão com a internet ou GitHub fora do ar.") }
    }

    /** Baixa o APK informando o progresso (0f..1f). */
    suspend fun download(context: Context, url: String, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val out = File(dir, APK_NAME)
        var current = url
        var redirects = 0
        while (true) {
            val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000; readTimeout = 30_000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "GuiaDeJogos-App")
            }
            val code = conn.responseCode
            if (code in 300..399 && redirects < 5) {
                current = conn.getHeaderField("Location"); conn.disconnect(); redirects++; continue
            }
            if (code != 200) { conn.disconnect(); error("Falha no download (HTTP $code)") }
            val total = conn.contentLengthLong
            conn.inputStream.use { input ->
                out.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    var read: Int
                    var done = 0L
                    while (input.read(buf).also { read = it } >= 0) {
                        output.write(buf, 0, read)
                        done += read
                        if (total > 0) onProgress(done.toFloat() / total)
                    }
                }
            }
            conn.disconnect()
            return@withContext out
        }
        out
    }

    /** O Android precisa que o usuário permita "instalar apps desta origem" uma vez. */
    fun canInstall(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    fun openInstallPermissionSettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
