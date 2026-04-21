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
import androidx.compose.runtime.remember
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
import com.jetbrains.spacetutorial.SearchScreen
import com.jetbrains.spacetutorial.SettingsScreen
import com.jetbrains.spacetutorial.onboarding.OnboardingNotificationsScreen
import com.jetbrains.spacetutorial.onboarding.OnboardingPrivacyScreen
import com.jetbrains.spacetutorial.onboarding.OnboardingViewModel
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import org.koin.androidx.compose.koinViewModel

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
    val onboardingViewModel: OnboardingViewModel = koinViewModel()
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDest = navBackStack?.destination
    val colors = TexasWatchTheme.colors

    val startDestination = remember {
        if (onboardingViewModel.isOnboardingComplete()) OffendersRoute else OnboardingPrivacyRoute
    }

    val isOnboardingScreen = currentDest?.hierarchy?.any {
        it.hasRoute(OnboardingPrivacyRoute::class) || it.hasRoute(OnboardingNotificationsRoute::class)
    } == true
    val isSearchScreen = currentDest?.hierarchy?.any { it.hasRoute(SearchRoute::class) } == true

    Scaffold(
        containerColor = colors.mainBackground,
        bottomBar = {
            if (!isSearchScreen && !isOnboardingScreen) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<OnboardingPrivacyRoute> {
                OnboardingPrivacyScreen(
                    onDecline = {
                        // User declined — stay on privacy screen (or you could close the app)
                    },
                    onAccept = {
                        navController.navigate(OnboardingNotificationsRoute) {
                            popUpTo<OnboardingPrivacyRoute> { inclusive = true }
                        }
                    },
                )
            }
            composable<OnboardingNotificationsRoute> {
                OnboardingNotificationsScreen(
                    viewModel = onboardingViewModel,
                    onDone = {
                        navController.navigate(OffendersRoute) {
                            popUpTo<OnboardingNotificationsRoute> { inclusive = true }
                        }
                    },
                )
            }
            composable<OffendersRoute> {
                OffendersSnapAndSearchScreen(
                    onSearchClick = { navController.navigate(SearchRoute) }
                )
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
            composable<SearchRoute> {
                SearchScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
