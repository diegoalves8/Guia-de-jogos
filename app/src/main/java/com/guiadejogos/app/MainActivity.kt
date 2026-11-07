package com.guiadejogos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.guiadejogos.app.ui.navigation.AppNavHost
import com.guiadejogos.app.ui.theme.GuiaDeJogosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as GuiaDeJogosApp
        setContent {
            GuiaDeJogosTheme {
                AppNavHost(repository = app.repository, favorites = app.favorites)
            }
        }
    }
}
