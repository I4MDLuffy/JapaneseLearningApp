package com.example.personalproject.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.personalproject.navigation.HomeRoute
import com.example.personalproject.navigation.JlptRoute
import com.example.personalproject.navigation.SavedRoute
import com.example.personalproject.navigation.SettingsRoute
import com.example.personalproject.navigation.StudyGamesRoute
import kotlin.reflect.KClass

@Composable
fun BottomNavBar(navController: NavController) {
    data class NavItem(
        val label: String,
        val route: Any,
        val routeClass: KClass<*>,
        val icon: @Composable () -> Unit,
    )

    val items = listOf(
        NavItem("Home", HomeRoute, HomeRoute::class) {
            Icon(Icons.Filled.Home, contentDescription = "Home")
        },
        NavItem("Saved", SavedRoute, SavedRoute::class) {
            Icon(Icons.Filled.Bookmark, contentDescription = "Saved")
        },
        NavItem("Games", StudyGamesRoute, StudyGamesRoute::class) {
            Icon(Icons.Filled.VideogameAsset, contentDescription = "Games")
        },
        NavItem("JLPT", JlptRoute, JlptRoute::class) {
            Icon(Icons.Filled.MenuBook, contentDescription = "JLPT")
        },
        NavItem("Settings", SettingsRoute, SettingsRoute::class) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        },
    )

    val backStack by navController.currentBackStackEntryAsState()

    NavigationBar {
        items.forEach { item ->
            val selected = backStack?.destination?.hasRoute(item.routeClass) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { item.icon() },
                label = { Text(item.label) },
            )
        }
    }
}
