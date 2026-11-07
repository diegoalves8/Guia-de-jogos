package com.guiadejogos.app.ui.screens

import com.guiadejogos.app.data.model.GameSummary

/** Filtros de categoria da Home e da aba Jogos. */
object GameFilters {
    const val ALL = "Todos os jogos"
    val options = listOf(ALL, "Mais populares", "Terror", "Aventura", "Simulação", "Estratégia", "Roblox", "Novidades")

    fun apply(games: List<GameSummary>, filter: String): List<GameSummary> = when (filter) {
        ALL -> games
        "Mais populares" -> games.filter { "Popular" in it.badges }
        "Novidades" -> games.filter { b -> b.badges.any { it.startsWith("Novo") } }
        else -> games.filter { filter in it.genres }
    }
}
