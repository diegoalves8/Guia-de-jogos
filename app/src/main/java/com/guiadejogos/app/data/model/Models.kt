package com.guiadejogos.app.data.model

/*
 * Modelos de dados do app.
 *
 * Todo o conteúdo vem de arquivos JSON (assets/content). O app NÃO tem jogos
 * ou categorias "fixos" no código: para adicionar um jogo, uma categoria ou
 * um bloco de conteúdo basta editar os JSON. Veja docs/ESTRUTURA_CONTEUDO.md.
 */

/** Índice geral: lista de jogos exibida na tela "Escolha o jogo". */
data class GamesIndex(
    val contentVersion: Int,
    val updatedAt: String,
    val games: List<GameSummary>,
)

/** Resumo de um jogo (card na lista de jogos). */
data class GameSummary(
    val id: String,
    val name: String,
    val subtitle: String,
    val platform: String,
    val emoji: String,
    val colorHex: String,
    val status: ContentStatus,
    /** Caminho do arquivo do jogo, relativo a content/ (ex.: "games/dandys-world.json"). */
    val file: String,
)

/** Conteúdo completo de um jogo. */
data class GameDetail(
    val id: String,
    val name: String,
    val description: String,
    val colorHex: String,
    val emoji: String,
    val categories: List<Category>,
)

/** Categoria de um jogo (Códigos, Skins, Guias...). Cada jogo define as suas. */
data class Category(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val status: ContentStatus,
    val updatedAt: String,
    val sections: List<Section>,
)

enum class ContentStatus {
    AVAILABLE, COMING_SOON;

    companion object {
        fun from(value: String?): ContentStatus =
            if (value.equals("coming_soon", ignoreCase = true)) COMING_SOON else AVAILABLE
    }
}

/**
 * Blocos de conteúdo de uma categoria. Cada tipo tem uma forma de exibição.
 * Tipos desconhecidos são ignorados (versões antigas do app não quebram
 * quando o JSON ganha tipos novos).
 */
sealed interface Section {
    val title: String

    /** Texto livre. */
    data class Text(override val title: String, val body: String) : Section

    /** Aviso destacado. tone: info | warning | success */
    data class Notice(override val title: String, val body: String, val tone: String) : Section

    /** Passo a passo numerado. */
    data class Steps(override val title: String, val items: List<String>) : Section

    /** Lista de códigos com status (ativo / expirado / não verificado). */
    data class Codes(override val title: String, val codes: List<GameCode>) : Section

    /** Lista de itens genéricos: skins, termos, personagens, eventos... */
    data class Items(override val title: String, val items: List<InfoItem>) : Section

    /** Dicas organizadas por nível (iniciante → profissional). */
    data class Levels(override val title: String, val levels: List<LevelGroup>) : Section

    /** Links úteis (fontes oficiais marcadas). */
    data class Links(override val title: String, val links: List<UsefulLink>) : Section
}

data class GameCode(
    val code: String,
    val reward: String,
    val status: CodeStatus,
    val note: String,
)

enum class CodeStatus {
    ACTIVE, EXPIRED, UNVERIFIED;

    companion object {
        fun from(value: String?): CodeStatus = when (value?.lowercase()) {
            "active" -> ACTIVE
            "expired" -> EXPIRED
            else -> UNVERIFIED
        }
    }
}

data class InfoItem(
    val name: String,
    val description: String,
    val tags: List<String>,
    /** Pares "rótulo → valor" (ex.: "Como conseguir" → "Completar a Maestria"). */
    val fields: List<Pair<String, String>>,
)

data class LevelGroup(
    val level: String,
    val tips: List<String>,
)

data class UsefulLink(
    val label: String,
    val url: String,
    val official: Boolean,
)
