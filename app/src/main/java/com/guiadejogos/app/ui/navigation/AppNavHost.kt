package com.guiadejogos.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.screens.CategoryScreen
import com.guiadejogos.app.ui.screens.DashboardScreen
import com.guiadejogos.app.ui.screens.GameScreen
import com.guiadejogos.app.ui.screens.GamesScreen
import com.guiadejogos.app.ui.screens.HomeScreen
import com.guiadejogos.app.ui.screens.ToonScreen
import com.guiadejogos.app.ui.screens.UpdatesScreen
import com.guiadejogos.app.ui.theme.AppColors

/**
 * Rotas do app:
 *   home (ENTRAR) → abas [dashboard | games | updates]
 *   → game/{gameId} → .../category/{categoryId} → .../toon/{toonId}
 */
object Routes {
    const val HOME = "home"
    const val DASHBOARD = "dashboard"
    const val GAMES = "games"
    const val UPDATES = "updates"
    const val GAME = "game/{gameId}"
    const val CATEGORY = "game/{gameId}/category/{categoryId}"
    const val TOON = "game/{gameId}/category/{categoryId}/toon/{toonId}"

    fun game(gameId: String) = "game/$gameId"
    fun category(gameId: String, categoryId: String) = "game/$gameId/category/$categoryId"
    fun toon(gameId: String, categoryId: String, toonId: String) = "game/$gameId/category/$categoryId/toon/$toonId"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab(Routes.DASHBOARD, "Início", Icons.Filled.Home),
    Tab(Routes.GAMES, "Jogos", Icons.Filled.SportsEsports),
    Tab(Routes.UPDATES, "Atualizar", Icons.Filled.SystemUpdate),
)

@Composable
fun AppNavHost(repository: ContentRepository) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showTabs = TABS.any { it.route == route }
    val screen = rememberScreenInfo()
    // Telas largas (tablet em paisagem, multimídia do carro): menu lateral. Celular: barra inferior.
    val useRail = screen.widthDp >= 840 || screen.isWideLandscape

    fun goTab(r: String) = nav.navigate(r) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            if (showTabs && !useRail) {
                NavigationBar(containerColor = AppColors.Surface) {
                    TABS.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = { goTab(t.route) },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.TextPrimary,
                                selectedTextColor = AppColors.TextPrimary,
                                indicatorColor = AppColors.Primary,
                                unselectedIconColor = AppColors.TextSecondary,
                                unselectedTextColor = AppColors.TextSecondary,
                            ),
                        )
                    }
                }
            }
        },
    ) { outer ->
        Row(Modifier.fillMaxSize().padding(bottom = if (showTabs && !useRail) outer.calculateBottomPadding() else 0.dp)) {
            if (showTabs && useRail) {
                NavigationRail(containerColor = AppColors.Surface) {
                    TABS.forEach { t ->
                        NavigationRailItem(
                            selected = route == t.route,
                            onClick = { goTab(t.route) },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = AppColors.TextPrimary,
                                selectedTextColor = AppColors.TextPrimary,
                                indicatorColor = AppColors.Primary,
                                unselectedIconColor = AppColors.TextSecondary,
                                unselectedTextColor = AppColors.TextSecondary,
                            ),
                        )
                    }
                }
            }
            Box(Modifier.weight(1f)) { Graph(nav, repository) }
        }
    }
}

@Composable
private fun Graph(nav: NavHostController, repository: ContentRepository) {
    val back: () -> Unit = { nav.popBackStack() }
    NavHost(
        navController = nav,
        startDestination = Routes.HOME,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable(Routes.HOME) {
            HomeScreen(onEnter = { nav.navigate(Routes.DASHBOARD) })
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                repository = repository,
                onGameClick = { id -> nav.navigate(Routes.game(id)) },
                onOpenGames = { nav.navigate(Routes.GAMES) { launchSingleTop = true } },
                onOpenUpdates = { nav.navigate(Routes.UPDATES) { launchSingleTop = true } },
            )
        }
        composable(Routes.GAMES) {
            GamesScreen(repository = repository, onGameClick = { id -> nav.navigate(Routes.game(id)) })
        }
        composable(Routes.UPDATES) { UpdatesScreen(repository = repository) }
        composable(Routes.GAME, arguments = listOf(navArgument("gameId") { type = NavType.StringType })) { e ->
            val gameId = e.arguments?.getString("gameId").orEmpty()
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
        ) { e ->
            val gameId = e.arguments?.getString("gameId").orEmpty()
            val categoryId = e.arguments?.getString("categoryId").orEmpty()
            CategoryScreen(
                repository = repository,
                gameId = gameId,
                categoryId = categoryId,
                onBack = back,
                onToonClick = { toonId -> nav.navigate(Routes.toon(gameId, categoryId, toonId)) },
            )
        }
        composable(
            Routes.TOON,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("toonId") { type = NavType.StringType },
            ),
        ) { e ->
            ToonScreen(
                repository = repository,
                gameId = e.arguments?.getString("gameId").orEmpty(),
                categoryId = e.arguments?.getString("categoryId").orEmpty(),
                toonId = e.arguments?.getString("toonId").orEmpty(),
                onBack = back,
            )
        }
    }
}
