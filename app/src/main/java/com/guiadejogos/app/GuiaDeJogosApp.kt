package com.guiadejogos.app

import android.app.Application
import com.guiadejogos.app.data.repository.ContentRepository

/** Application: cria as dependências compartilhadas uma única vez. */
class GuiaDeJogosApp : Application() {
    lateinit var repository: ContentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ContentRepository(this)
    }
}
