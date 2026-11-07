package com.guiadejogos.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guiadejogos.app.data.repository.ContentRepository
import com.guiadejogos.app.data.repository.FavoritesStore
import com.guiadejogos.app.ui.components.rememberScreenInfo
import com.guiadejogos.app.ui.screens.CategoryScreen
import com.guiadejogos.app.ui.screens.FavoritesScreen
import com.guiadejogos.app.ui.screens.GameScreen
import com.guiadejogos.app.ui.screens.GamesScreen
import com.guiadejogos.app.ui.screens.HomeScreen
import com.guiadejogos.app.ui.screens.NewsScreen
import com.guiadejogos.app.ui.screens.SearchScreen
import com.guiadejogos.app.ui.screens.SettingsScreen
import com.guiadejogos.app.ui.screens.StoreHomeScreen
import com.guiadejogos.app.ui.screens.ToonScreen
import com.guiadejogos.app.ui.theme.AppColors

/**
 * Rotas do app:
 *   welcome (ENTRAR) → abas [home | games | favorites | news | settings]
 *   → search, game/{gameId} → .../category/{categoryId} → .../toon/{toonId}
 */
object Routes {
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val GAMES = "games"
    const val FAVORITES = "favorites"
    const val NEWS = "news"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val GAME = "game/{gameId}"
    const val CATEGORY = "game/{gameId}/category/{categoryId}"
    const val TOON = "game/{gameId}/category/{categoryId}/toon/{toonId}"

    fun game(gameId: String) = "game/$gameId"
    fun category(gameId: String, categoryId: String) = "game/$gameId/category/$categoryId"
    fun toon(gameId: String, categoryId: String, toonId: String) = "game/$gameId/category/$categoryId/toon/$toonId"
}

private data class Tab(val route: String, val label: String, val selected: ImageVector, val unselected: ImageVector)

private val TABS = listOf(
    Tab(Routes.HOME, "Início", Icons.Filled.Home, Icons.Outlined.Home),
    Tab(Routes.GAMES, "Jogos", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    Tab(Routes.FAVORITES, "Favoritos", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    Tab(Routes.NEWS, "Novidades", Icons.Filled.NewReleases, Icons.Outlined.NewReleases),
    Tab(Routes.SETTINGS, "Configurações", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun AppNavHost(repository: ContentRepository, favorites: FavoritesStore) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showTabs = TABS.any { it.route == route }
    val compactBar = rememberScreenInfo().isShortHeight

    fun goTab(r: String) = nav.navigate(r) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            if (showTabs) {
                // Barra inferior fixa; mais baixa em telas horizontais baixas (multimídia).
                NavigationBar(
                    containerColor = AppColors.Surface,
                    tonalElevation = 0.dp,
                    modifier = if (compactBar) Modifier.height(72.dp) else Modifier,
                ) {
                    TABS.forEach { t ->
                        val sel = route == t.route
                        NavigationBarItem(
                            selected = sel,
                            onClick = { goTab(t.route) },
                            icon = { Icon(if (sel) t.selected else t.unselected, contentDescription = t.label) },
                            label = { Text(t.label, style = MaterialTheme.typography.labelMedium) },
                            alwaysShowLabel = !compactBar || sel,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.Primary,
                                selectedTextColor = AppColors.Primary,
                                indicatorColor = AppColors.Primary.copy(alpha = 0.16f),
                                unselectedIconColor = AppColors.TextSecondary,
                                unselectedTextColor = AppColors.TextSecondary,
                            ),
                        )
                    }
                }
            }
        },
    ) { outer ->
        Box(Modifier.fillMaxSize().padding(bottom = if (showTabs) outer.calculateBottomPadding() else 0.dp)) {
            Graph(nav, repository, favorites, ::goTab)
        }
    }
}

// Transições suaves: deslizar + esmaecer nas páginas internas, só esmaecer entre abas.
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    TABS.any { it.route == initialState.destination.route } && TABS.any { it.route == targetState.destination.route }

@Composable
private fun Graph(nav: NavHostController, repository: ContentRepository, favorites: FavoritesStore, goTab: (String) -> Unit) {
    val back: () -> Unit = { nav.popBackStack() }
    val open: (String) -> Unit = { r -> nav.navigate(r) }
    NavHost(
        navController = nav,
        startDestination = Routes.WELCOME,
        enterTransition = {
            if (isTabSwitch()) fadeIn(tween(220))
            else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) { it / 8 } + fadeIn(tween(300))
        },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = {
            if (isTabSwitch()) fadeOut(tween(200))
            else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(280)) { it / 8 } + fadeOut(tween(280))
        },
    ) {
        composable(Routes.WELCOME) {
            HomeScreen(onEnter = { nav.navigate(Routes.HOME) })
        }
        composable(Routes.HOME) {
            StoreHomeScreen(
                repository = repository,
                favorites = favorites,
                onGameClick = { id -> open(Routes.game(id)) },
                onOpenRoute = open,
                onSearch = { open(Routes.SEARCH) },
                onSettings = { goTab(Routes.SETTINGS) },
            )
        }
        composable(Routes.GAMES) { GamesScreen(repository = repository, onGameClick = { id -> open(Routes.game(id)) }) }
        composable(Routes.FAVORITES) { FavoritesScreen(favorites = favorites, onOpenRoute = open) }
        composable(Routes.NEWS) { NewsScreen(repository = repository, onOpenCategory = { g, c -> open(Routes.category(g, c)) }) }
        composable(Routes.SETTINGS) { SettingsScreen(repository = repository) }
        composable(Routes.SEARCH) { SearchScreen(repository = repository, onBack = back, onOpenRoute = open) }
        composable(Routes.GAME, arguments = listOf(navArgument("gameId") { type = NavType.StringType })) { e ->
            val gameId = e.arguments?.getString("gameId").orEmpty()
            GameScreen(
                repository = repository,
                favorites = favorites,
                gameId = gameId,
                onBack = back,
                onCategoryClick = { categoryId -> open(Routes.category(gameId, categoryId)) },
                onToonClick = { categoryId, toonId -> open(Routes.toon(gameId, categoryId, toonId)) },
            )
        }
        composable(
            Routes.CATEGORY,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType }, navArgument("categoryId") { type = NavType.StringType }),
        ) { e ->
            val gameId = e.arguments?.getString("gameId").orEmpty()
            val categoryId = e.arguments?.getString("categoryId").orEmpty()
            CategoryScreen(
                repository = repository,
                gameId = gameId,
                categoryId = categoryId,
                onBack = back,
                onToonClick = { toonId -> open(Routes.toon(gameId, categoryId, toonId)) },
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
                favorites = favorites,
                gameId = e.arguments?.getString("gameId").orEmpty(),
                categoryId = e.arguments?.getString("categoryId").orEmpty(),
                toonId = e.arguments?.getString("toonId").orEmpty(),
                onBack = back,
            )
        }
    }
}
