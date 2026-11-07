package com.guiadejogos.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.guiadejogos.app.data.repository.ContentRepository

/** Application: cria as dependências compartilhadas uma única vez. */
class GuiaDeJogosApp : Application(), ImageLoaderFactory {
    lateinit var repository: ContentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ContentRepository(this)
    }

    /** Carregador de imagens com cache em memória e em disco (funciona offline após a 1ª vez). */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(true)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.20).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(150L * 1024 * 1024)
                .build()
        }
        .respectCacheHeaders(false)
        .build()
}
