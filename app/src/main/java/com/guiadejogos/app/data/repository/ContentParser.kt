package com.guiadejogos.app.data.repository

import com.guiadejogos.app.data.model.Category
import com.guiadejogos.app.data.model.CodeStatus
import com.guiadejogos.app.data.model.ContentStatus
import com.guiadejogos.app.data.model.GameCode
import com.guiadejogos.app.data.model.GameDetail
import com.guiadejogos.app.data.model.GalleryImage
import com.guiadejogos.app.data.model.GameSummary
import com.guiadejogos.app.data.model.GamesIndex
import com.guiadejogos.app.data.model.InfoItem
import com.guiadejogos.app.data.model.LevelGroup
import com.guiadejogos.app.data.model.Section
import com.guiadejogos.app.data.model.Toon
import com.guiadejogos.app.data.model.ToonSkin
import com.guiadejogos.app.data.model.TutorialStep
import com.guiadejogos.app.data.model.UsefulLink
import org.json.JSONArray
import org.json.JSONObject

/**
 * Converte os JSON de conteúdo em modelos Kotlin.
 * Usa org.json (já incluso no Android) para não depender de bibliotecas extras.
 * Campos ausentes recebem valores padrão, então o JSON pode crescer sem quebrar o app.
 */
object ContentParser {

    fun parseIndex(json: String): GamesIndex {
        val root = JSONObject(json)
        return GamesIndex(
            contentVersion = root.optInt("contentVersion", 0),
            updatedAt = root.optString("updatedAt"),
            games = root.optJSONArray("games").objects().map { g ->
                GameSummary(
                    id = g.getString("id"),
                    name = g.optString("name"),
                    subtitle = g.optString("subtitle"),
                    platform = g.optString("platform"),
                    emoji = g.optString("emoji", "🎮"),
                    colorHex = g.optString("color", "#8B5CF6"),
                    status = ContentStatus.from(g.optString("status")),
                    file = g.optString("file"),
                )
            },
        )
    }

    fun parseGame(json: String): GameDetail {
        val root = JSONObject(json)
        return GameDetail(
            id = root.getString("id"),
            name = root.optString("name"),
            description = root.optString("description"),
            colorHex = root.optString("color", "#8B5CF6"),
            emoji = root.optString("emoji", "🎮"),
            categories = root.optJSONArray("categories").objects().map { c ->
                Category(
                    id = c.getString("id"),
                    title = c.optString("title"),
                    emoji = c.optString("emoji", "📄"),
                    description = c.optString("description"),
                    status = ContentStatus.from(c.optString("status")),
                    updatedAt = c.optString("updatedAt"),
                    sections = c.optJSONArray("sections").objects().mapNotNull(::parseSection),
                )
            },
        )
    }

    private fun parseSection(s: JSONObject): Section? {
        val title = s.optString("title")
        return when (s.optString("type")) {
            "text" -> Section.Text(title, s.optString("body"))
            "notice" -> Section.Notice(title, s.optString("body"), s.optString("tone", "info"))
            "steps" -> Section.Steps(title, s.optJSONArray("items").strings())
            "codes" -> Section.Codes(
                title,
                s.optJSONArray("codes").objects().map { c ->
                    GameCode(
                        code = c.optString("code"),
                        reward = c.optString("reward"),
                        status = CodeStatus.from(c.optString("status")),
                        note = c.optString("note"),
                    )
                },
            )
            "items" -> Section.Items(
                title,
                s.optJSONArray("items").objects().map { i ->
                    val fieldsObj = i.optJSONObject("fields")
                    val fields = fieldsObj?.keys()?.asSequence()
                        ?.map { key -> key to fieldsObj.optString(key) }
                        ?.toList()
                        .orEmpty()
                    InfoItem(
                        name = i.optString("name"),
                        image = i.optString("image"),
                        description = i.optString("description"),
                        tags = i.optJSONArray("tags").strings(),
                        fields = fields,
                    )
                },
            )
            "levels" -> Section.Levels(
                title,
                s.optJSONArray("levels").objects().map { l ->
                    LevelGroup(l.optString("level"), l.optJSONArray("tips").strings())
                },
            )
            "links" -> Section.Links(
                title,
                s.optJSONArray("links").objects().map { l ->
                    UsefulLink(l.optString("label"), l.optString("url"), l.optBoolean("official"))
                },
            )
            "gallery" -> Section.Gallery(
                title,
                s.optJSONArray("images").objects().map { g ->
                    GalleryImage(g.optString("image"), g.optString("caption"), g.optString("subtitle"))
                },
            )
            "toons" -> Section.Toons(title, s.optJSONArray("toons").objects().map(::parseToon))
            else -> null // tipo desconhecido: ignorado
        }
    }

    private fun parseToon(t: JSONObject) = Toon(
        id = t.getString("id"),
        name = t.optString("name"),
        image = t.optString("image"),
        vintageImage = t.optString("vintageImage"),
        kind = t.optString("kind"),
        cost = t.optString("cost"),
        requirements = t.optJSONArray("requirements").strings(),
        mastery = t.optJSONArray("mastery").strings(),
        skins = t.optJSONArray("skins").objects().map { s ->
            ToonSkin(s.optString("name"), s.optString("image"), s.optString("category"), s.optString("howTo"))
        },
        tutorial = t.optJSONArray("tutorial").objects().map { s ->
            TutorialStep(s.optString("title"), s.optString("text"), s.optString("image"))
        },
        videoUrl = t.optString("videoUrl"),
    )

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }

    private fun JSONArray?.strings(): List<String> =
        if (this == null) emptyList() else (0 until length()).map { optString(it) }
}
