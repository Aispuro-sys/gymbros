package com.talos.forge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.talos.forge.ui.AuthViewModel
import com.talos.forge.ui.CommunityViewModel
import com.talos.forge.ui.DashboardViewModel
import com.talos.forge.ui.NutritionViewModel
import com.talos.forge.ui.ProfileViewModel
import com.talos.forge.ui.RecipesViewModel
import com.talos.forge.ui.RoutinesViewModel
import com.talos.forge.ui.ShoppingViewModel
import com.talos.forge.ui.SupplementsViewModel
import com.talos.forge.ui.ViewModelFactory
import com.talos.forge.ui.navigation.bottomNavItems
import com.talos.forge.ui.screens.*
import com.talos.forge.ui.theme.TalosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TalosTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TalosApp
    val factory = remember { ViewModelFactory.create(app) }

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    if (!isLoggedIn) {
        AuthScreen(viewModel = authViewModel)
    } else {
        MainApp(factory = factory, onLogout = { authViewModel.logout() })
    }
}

@Composable
fun MainApp(factory: ViewModelFactory, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val dashboardVM: DashboardViewModel = viewModel(factory = factory)
    val routinesVM: RoutinesViewModel = viewModel(factory = factory)
    val nutritionVM: NutritionViewModel = viewModel(factory = factory)
    val supplementsVM: SupplementsViewModel = viewModel(factory = factory)
    val recipesVM: RecipesViewModel = viewModel(factory = factory)
    val shoppingVM: ShoppingViewModel = viewModel(factory = factory)
    val communityVM: CommunityViewModel = viewModel(factory = factory)
    val profileVM: ProfileViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontSize = 10.sp) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") { DashboardScreen(dashboardVM) }
            composable("routines") { RoutinesScreen(routinesVM) }
            composable("nutrition") { NutritionScreen(nutritionVM) }
            composable("supplements") { SupplementsScreen(supplementsVM) }
            composable("recipes") { RecipesScreen(recipesVM) }
            composable("shopping") { ShoppingScreen(shoppingVM) }
            composable("community") { CommunityScreen(communityVM) }
            composable("profile") { ProfileScreen(profileVM, onLogout) }
        }
    }
}
