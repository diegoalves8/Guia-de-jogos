package com.guiadejogos.app.data.repository

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import org.json.JSONObject

/**
 * Favoritos (jogos e personagens), salvos no aparelho.
 * Os dados ficam num estado observável: as telas atualizam sozinhas ao favoritar.
 */
class FavoritesStore(context: Context) {

    /** Um favorito: [route] é a rota de navegação para abrir o item. */
    data class Favorite(
        val key: String,
        val title: String,
        val subtitle: String,
        val image: String,
        val route: String,
        val type: String, // "game" | "character"
    )

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val items = mutableStateMapOf<String, Favorite>()

    init {
        runCatching {
            val root = JSONObject(prefs.getString(KEY, "{}") ?: "{}")
            root.keys().forEach { k ->
                val o = root.getJSONObject(k)
                items[k] = Favorite(k, o.optString("title"), o.optString("subtitle"), o.optString("image"), o.optString("route"), o.optString("type"))
            }
        }
    }

    val all: List<Favorite> get() = items.values.sortedBy { it.title.lowercase() }

    fun isFavorite(key: String): Boolean = items.containsKey(key)

    fun toggle(f: Favorite) {
        if (items.containsKey(f.key)) items.remove(f.key) else items[f.key] = f
        save()
    }

    private fun save() {
        val root = JSONObject()
        items.values.forEach { f ->
            root.put(f.key, JSONObject().apply {
                put("title", f.title); put("subtitle", f.subtitle); put("image", f.image); put("route", f.route); put("type", f.type)
            })
        }
        prefs.edit().putString(KEY, root.toString()).apply()
    }

    companion object {
        private const val KEY = "items"
        fun gameKey(id: String) = "game:$id"
        fun characterKey(gameId: String, categoryId: String, id: String) = "char:$gameId/$categoryId/$id"
    }
}
