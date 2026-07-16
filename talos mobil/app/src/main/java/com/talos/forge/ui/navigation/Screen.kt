package com.talos.forge.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Dashboard)
    data object Routines : Screen("routines", "Rutinas", Icons.Default.FitnessCenter)
    data object Nutrition : Screen("nutrition", "Nutri", Icons.Default.Restaurant)
    data object Supplements : Screen("supplements", "Suplem", Icons.Default.Medication)
    data object Recipes : Screen("recipes", "Recetas", Icons.Default.MenuBook)
    data object Shopping : Screen("shopping", "Super", Icons.Default.ShoppingCart)
    data object Community : Screen("community", "Comunidad", Icons.Default.Forum)
    data object Profile : Screen("profile", "Perfil", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Routines,
    Screen.Nutrition,
    Screen.Supplements,
    Screen.Recipes,
    Screen.Shopping,
    Screen.Community,
    Screen.Profile
)
