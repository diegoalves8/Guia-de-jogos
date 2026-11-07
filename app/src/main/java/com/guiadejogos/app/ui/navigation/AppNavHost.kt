package com.guiadejogos.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.screens.CategoryScreen
import com.guiadejogos.app.ui.screens.GameScreen
import com.guiadejogos.app.ui.screens.GamesScreen
import com.guiadejogos.app.ui.screens.HomeScreen

/**
 * Rotas do app:
 *   home → games → game/{gameId} → game/{gameId}/category/{categoryId}
 * Novas telas (busca, favoritos, notícias...) entram aqui como novas rotas.
 */
object Routes {
    const val HOME = "home"
    const val GAMES = "games"
    const val GAME = "game/{gameId}"
    const val CATEGORY = "game/{gameId}/category/{categoryId}"

    fun game(gameId: String) = "game/$gameId"
    fun category(gameId: String, categoryId: String) = "game/$gameId/category/$categoryId"
}

@Composable
fun AppNavHost(repository: ContentRepository) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onEnter = { nav.navigate(Routes.GAMES) })
        }
        composable(Routes.GAMES) {
            GamesScreen(
                repository = repository,
                onBack = back,
                onGameClick = { id -> nav.navigate(Routes.game(id)) },
            )
        }
        composable(
            Routes.GAME,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType }),
        ) { entry ->
            val gameId = entry.arguments?.getString("gameId").orEmpty()
            GameScreen(
                repository = repository,
                gameId = gameId,
                onBack = back,
                onCategoryClick = { categoryId -> nav.navigate(Routes.category(gameId, categoryId)) },
            )
        }
        composable(
            Routes.CATEGORY,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType },
            ),
        ) { entry ->
            CategoryScreen(
                repository = repository,
                gameId = entry.arguments?.getString("gameId").orEmpty(),
                categoryId = entry.arguments?.getString("categoryId").orEmpty(),
                onBack = back,
            )
        }
    }
}
