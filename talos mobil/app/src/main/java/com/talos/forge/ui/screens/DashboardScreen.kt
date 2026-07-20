package com.talos.forge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.ui.DashboardViewModel
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.theme.AppColors

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onNavigate: (String) -> Unit = {}) {
    val isLoading by viewModel.isLoading.collectAsState()
    val macros by viewModel.macros.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val routines by viewModel.routines.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    if (isLoading) {
        LoadingSpinner()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Macros card con gradiente
        macros?.let { m ->
            GradientMacroCard(
                calories = m.calories.toString(),
                protein = "${m.protein_g}g",
                carbs = "${m.carbs_g}g",
                fats = "${m.fats_g}g"
            )
        } ?: run {
            GradientMacroCard(calories = "0", protein = "0g", carbs = "0g", fats = "0g")
        }

        // Comidas de hoy
        DashboardListCard(
            title = "Comidas de Hoy",
            count = meals.size,
            icon = Icons.Default.Restaurant,
            accentColor = AppColors.accent,
            onClick = { onNavigate("nutrition") }
        ) {
            if (meals.isEmpty()) {
                EmptyHint("Sin comidas registradas")
            } else {
                meals.take(4).forEach { meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.accentMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocalDining,
                                    contentDescription = null,
                                    tint = AppColors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(meal.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(
                            "${meal.calories} cal",
                            fontSize = 13.sp,
                            color = AppColors.textSecondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Rutinas
        DashboardListCard(
            title = "Mis Rutinas",
            count = routines.size,
            icon = Icons.Default.FitnessCenter,
            accentColor = AppColors.accent,
            onClick = { onNavigate("routines") }
        ) {
            if (routines.isEmpty()) {
                EmptyHint("Sin rutinas creadas")
            } else {
                routines.take(4).forEach { routine ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.accentMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = AppColors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(routine.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(
                            "${routine.exercises.size} ej",
                            fontSize = 12.sp,
                            color = AppColors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick access grid
        Text("Acceso Rápido", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.textPrimary)
        QuickAccessGrid(onNavigate = onNavigate)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuickAccessGrid(onNavigate: (String) -> Unit) {
    val accent = AppColors.accent
    val items = listOf(
        QuickItem("Suplementos", Icons.Default.Medication, accent, "supplements"),
        QuickItem("Recetas", Icons.Default.MenuBook, accent, "recipes"),
        QuickItem("Compras", Icons.Default.ShoppingCart, accent, "shopping"),
        QuickItem("Comunidad", Icons.Default.Forum, accent, "community"),
        QuickItem("Equipos", Icons.Default.Groups, accent, "teams"),
        QuickItem("Perfil", Icons.Default.Person, accent, "profile")
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { item ->
                    QuickAccessCard(item = item, onClick = { onNavigate(item.route) }, modifier = Modifier.weight(1f))
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class QuickItem(val title: String, val icon: ImageVector, val color: Color, val route: String)

@Composable
private fun QuickAccessCard(item: QuickItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(item.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
        }
    }
}

@Composable
private fun GradientMacroCard(
    calories: String,
    protein: String,
    carbs: String,
    fats: String
) {
    val accent = AppColors.accent
    val onAccent = AppColors.textOnAccent
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(accent, AppColors.accentDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Macros de Hoy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = onAccent
                    )
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = onAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroPill("Calorías", calories, Icons.Default.LocalFireDepartment, onAccent)
                    MacroPill("Proteína", protein, Icons.Default.FitnessCenter, onAccent)
                    MacroPill("Carbs", carbs, Icons.Default.Grain, onAccent)
                    MacroPill("Grasas", fats, Icons.Default.Opacity, onAccent)
                }
            }
        }
    }
}

@Composable
private fun MacroPill(label: String, value: String, icon: ImageVector, onAccent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(onAccent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = onAccent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = onAccent)
        Text(label, fontSize = 10.sp, color = onAccent.copy(alpha = 0.8f))
    }
}

@Composable
private fun DashboardListCard(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                }
                Text(
                    "$count",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 13.sp,
            color = AppColors.textTertiary
        )
    }
}
