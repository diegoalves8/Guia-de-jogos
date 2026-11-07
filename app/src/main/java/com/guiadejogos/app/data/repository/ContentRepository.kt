package com.guiadejogos.app.data.repository

import android.content.Context
import com.guiadejogos.app.config.AppConfig
import com.guiadejogos.app.data.model.GameDetail
import com.guiadejogos.app.data.model.GamesIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fonte única de conteúdo do app.
 *
 * Ordem de leitura de cada arquivo:
 *   1. Cache local (última versão baixada da internet), se for mais nova que a embutida;
 *   2. Conteúdo embutido no APK (assets/content).
 *
 * [refresh] baixa a versão mais recente do servidor configurado em
 * [AppConfig.remoteContentBaseUrl]. Se não houver internet ou o servidor
 * falhar, o app continua funcionando com o que já tem.
 */
class ContentRepository(private val context: Context) {

    private val cacheDir = File(context.filesDir, "content-cache")

    suspend fun loadIndex(): GamesIndex = withContext(Dispatchers.IO) {
        ContentParser.parseIndex(read(INDEX_FILE))
    }

    suspend fun loadGame(file: String): GameDetail = withContext(Dispatchers.IO) {
        ContentParser.parseGame(read(file))
    }

    suspend fun loadGameById(gameId: String): GameDetail? {
        val summary = loadIndex().games.firstOrNull { it.id == gameId } ?: return null
        return loadGame(summary.file)
    }

    /**
     * Baixa o índice e os arquivos de jogos do servidor remoto.
     * Só salva se o JSON for válido e tiver contentVersion maior que o atual.
     * @return true se algo novo foi baixado.
     */
    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        val base = AppConfig.remoteContentBaseUrl
        if (base.isBlank()) return@withContext false
        runCatching {
            val remoteIndexText = download(base + INDEX_FILE)
            val remoteIndex = ContentParser.parseIndex(remoteIndexText)
            val currentVersion = ContentParser.parseIndex(read(INDEX_FILE)).contentVersion
            if (remoteIndex.contentVersion <= currentVersion) return@runCatching false

            // Baixa e valida todos os arquivos antes de gravar qualquer um.
            val files = remoteIndex.games.map { it.file }.filter { it.isNotBlank() }.distinct()
            val downloaded = files.associateWith { path ->
                download(base + path).also { ContentParser.parseGame(it) }
            }
            downloaded.forEach { (path, text) -> writeCache(path, text) }
            writeCache(INDEX_FILE, remoteIndexText)
            true
        }.getOrDefault(false)
    }

    // ---------- leitura ----------

    private fun read(path: String): String {
        val cached = File(cacheDir, path)
        if (cached.exists() && isCacheNewerThanAssets()) {
            return cached.readText()
        }
        return readAsset(path)
    }

    private fun readAsset(path: String): String =
        context.assets.open("$ASSETS_ROOT/$path").bufferedReader().use { it.readText() }

    /**
     * O cache só vale se for mais novo que o conteúdo embutido.
     * Assim, ao instalar um APK novo com conteúdo atualizado, o cache antigo é ignorado.
     */
    private fun isCacheNewerThanAssets(): Boolean = runCatching {
        val cachedIndex = File(cacheDir, INDEX_FILE)
        if (!cachedIndex.exists()) return false
        val cachedVersion = ContentParser.parseIndex(cachedIndex.readText()).contentVersion
        val assetVersion = ContentParser.parseIndex(readAsset(INDEX_FILE)).contentVersion
        cachedVersion > assetVersion
    }.getOrDefault(false)

    private fun writeCache(path: String, text: String) {
        val file = File(cacheDir, path)
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    private fun download(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            useCaches = false
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                error("HTTP ${connection.responseCode} em $url")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val ASSETS_ROOT = "content"
        private const val INDEX_FILE = "games.json"
    }
}
