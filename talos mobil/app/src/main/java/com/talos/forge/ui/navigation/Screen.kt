package com.talos.forge.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Dashboard)
    data object Routines : Screen("routines", "Rutinas", Icons.Default.FitnessCenter)
    data object Nutrition : Screen("nutrition", "Nutrición", Icons.Default.Restaurant)
    data object Supplements : Screen("supplements", "Suplementos", Icons.Default.Medication)
    data object Recipes : Screen("recipes", "Recetas", Icons.Default.MenuBook)
    data object Shopping : Screen("shopping", "Compras", Icons.Default.ShoppingCart)
    data object Community : Screen("community", "Comunidad", Icons.Default.Forum)
    data object Teams : Screen("teams", "Equipos", Icons.Default.Groups)
    data object Progress : Screen("progress", "Progreso", Icons.Default.CameraAlt)
    data object Profile : Screen("profile", "Perfil", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Routines,
    Screen.Nutrition,
    Screen.Supplements,
    Screen.Recipes,
    Screen.Shopping,
    Screen.Teams,
    Screen.Community,
    Screen.Progress,
    Screen.Profile
)
