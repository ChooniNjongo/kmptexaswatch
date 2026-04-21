package com.jetbrains.spacetutorial.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jetbrains.spacetutorial.MapScreen
import com.jetbrains.spacetutorial.OffendersSnapAndSearchScreen
import com.jetbrains.spacetutorial.R
import com.jetbrains.spacetutorial.RouteScreen
import com.jetbrains.spacetutorial.SettingsScreen
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme

// ── Bottom nav item descriptor (mirrors KotlinConf MainNavDestination) ────────

data class NavDestination<T : Any>(
    val label: String,
    val iconRes: Int,
    val iconSelectedRes: Int,
    val route: T,
)

private val bottomNavItems = listOf(
    NavDestination(
        label = "Offenders",
        iconRes = R.drawable.team_28,
        iconSelectedRes = R.drawable.team_28_fill,
        route = OffendersRoute,
    ),
    NavDestination(
        label = "Map",
        iconRes = R.drawable.location_28,
        iconSelectedRes = R.drawable.location_28_fill,
        route = MapRoute,
    ),
    NavDestination(
        label = "Route",
        iconRes = R.drawable.route_28,
        iconSelectedRes = R.drawable.route_28_fill,
        route = RouteRoute,
    ),
    NavDestination(
        label = "Settings",
        iconRes = R.drawable.info_28,
        iconSelectedRes = R.drawable.info_28_fill,
        route = SettingsRoute,
    ),
)

// ── App scaffold with bottom nav + NavHost ────────────────────────────────────

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDest = navBackStack?.destination
    val colors = TexasWatchTheme.colors

    Scaffold(
        containerColor = colors.mainBackground,
        bottomBar = {
            NavigationBar(
                containerColor = colors.mainBackground,
                contentColor = colors.primaryAccent,
            ) {
                bottomNavItems.forEach { dest ->
                    val selected = currentDest?.hierarchy?.any {
                        it.hasRoute(dest.route::class)
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    if (selected) dest.iconSelectedRes else dest.iconRes
                                ),
                                contentDescription = dest.label,
                            )
                        },
                        label = { Text(dest.label, style = TexasWatchTheme.typography.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.primaryAccent,
                            selectedTextColor = colors.primaryAccent,
                            unselectedIconColor = colors.secondaryText,
                            unselectedTextColor = colors.secondaryText,
                            indicatorColor = colors.mainBackground,
                        ),
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = OffendersRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<OffendersRoute> {
                OffendersSnapAndSearchScreen()
            }
            composable<MapRoute> {
                MapScreen()
            }
            composable<RouteRoute> {
                RouteScreen()
            }
            composable<SettingsRoute> {
                SettingsScreen()
            }
        }
    }
}
