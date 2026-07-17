package com.talos.forge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.talos.forge.ui.AuthViewModel
import com.talos.forge.ui.CommunityViewModel
import com.talos.forge.ui.DashboardViewModel
import com.talos.forge.ui.NutritionViewModel
import com.talos.forge.ui.ProfileViewModel
import com.talos.forge.ui.ProgressViewModel
import com.talos.forge.ui.RecipesViewModel
import com.talos.forge.ui.RoutinesViewModel
import com.talos.forge.ui.ShoppingViewModel
import com.talos.forge.ui.SupplementsViewModel
import com.talos.forge.ui.TeamsViewModel
import com.talos.forge.ui.ViewModelFactory
import com.talos.forge.ui.navigation.bottomNavItems
import com.talos.forge.ui.screens.*
import com.talos.forge.ui.theme.AppColors
import com.talos.forge.ui.theme.TalosTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TalosTheme(darkTheme = androidx.compose.foundation.isSystemInDarkTheme()) {
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

    val currentUser by authViewModel.currentUser.collectAsState()

    if (!isLoggedIn) {
        AuthScreen(viewModel = authViewModel)
    } else {
        MainApp(factory = factory, onLogout = { authViewModel.logout() }, currentUser = currentUser)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(factory: ViewModelFactory, onLogout: () -> Unit, currentUser: com.talos.forge.data.models.User?) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val dashboardVM: DashboardViewModel = viewModel(factory = factory)
    val routinesVM: RoutinesViewModel = viewModel(factory = factory)
    val nutritionVM: NutritionViewModel = viewModel(factory = factory)
    val supplementsVM: SupplementsViewModel = viewModel(factory = factory)
    val recipesVM: RecipesViewModel = viewModel(factory = factory)
    val shoppingVM: ShoppingViewModel = viewModel(factory = factory)
    val communityVM: CommunityViewModel = viewModel(factory = factory)
    val teamsVM: TeamsViewModel = viewModel(factory = factory)
    val profileVM: ProfileViewModel = viewModel(factory = factory)
    val progressVM: ProgressViewModel = viewModel(factory = factory)

    val currentScreen = bottomNavItems.find { it.route == currentRoute } ?: bottomNavItems.first()

    val drawerWidth = 280.dp
    val drawerGradient = listOf(AppColors.cardBg, AppColors.cardBgAlt)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth),
                drawerShape = RoundedCornerShape(0.dp, 20.dp, 20.dp, 0.dp),
                drawerContainerColor = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(Brush.verticalGradient(drawerGradient))
                ) {
                    // Header compacto con foto de perfil
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Foto de perfil
                        if (currentUser?.profile_photo != null) {
                            AsyncImage(
                                model = currentUser.profile_photo,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.accentMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Sin foto",
                                    tint = AppColors.accent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                currentUser?.username ?: "Usuario",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.textPrimary
                            )
                            Text(
                                currentUser?.email ?: "",
                                fontSize = 11.sp,
                                color = AppColors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    // Separador
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.5.dp,
                        color = AppColors.divider
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Items compactos
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppColors.accentMuted else Color.Transparent)
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) AppColors.accent else AppColors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                screen.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AppColors.accent else AppColors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón cerrar sesión
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.5.dp,
                        color = AppColors.divider
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onLogout() }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = AppColors.danger,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Cerrar Sesión",
                            fontSize = 13.sp,
                            color = AppColors.danger
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            containerColor = AppColors.bg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            currentScreen.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppColors.bg,
                        titleContentColor = AppColors.textPrimary,
                        navigationIconContentColor = AppColors.textPrimary
                    )
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.padding(padding)
            ) {
                composable("dashboard") { DashboardScreen(dashboardVM, onNavigate = { route -> navController.navigate(route) }) }
                composable("routines") { RoutinesScreen(routinesVM) }
                composable("nutrition") { NutritionScreen(nutritionVM) }
                composable("supplements") { SupplementsScreen(supplementsVM) }
                composable("recipes") { RecipesScreen(recipesVM) }
                composable("shopping") { ShoppingScreen(shoppingVM) }
                composable("community") { CommunityScreen(communityVM) }
                composable("teams") { TeamsScreen(teamsVM) }
                composable("progress") { ProgressScreen(progressVM) }
                composable("profile") { ProfileScreen(profileVM, onLogout) }
            }
        }
    }
}
