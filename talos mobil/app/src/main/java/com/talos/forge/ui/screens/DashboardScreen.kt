package com.talos.forge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.ui.DashboardViewModel
import com.talos.forge.ui.components.EmptyState
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.components.SectionCard

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Macros summary
        macros?.let { m ->
            SectionCard(title = "Macros de Hoy") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroItem("Cal", m.calories.toString(), Icons.Default.LocalFireDepartment)
                    MacroItem("Prot", "${m.protein_g}g", Icons.Default.FitnessCenter)
                    MacroItem("Carb", "${m.carbs_g}g", Icons.Default.Grain)
                    MacroItem("Gras", "${m.fats_g}g", Icons.Default.Opacity)
                }
            }
        } ?: run {
            SectionCard(title = "Macros de Hoy") {
                Text("No hay datos registrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Meals today
        SectionCard(title = "Comidas de Hoy (${meals.size})") {
            if (meals.isEmpty()) {
                Text("Sin comidas registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                meals.take(3).forEach { meal ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(meal.name, fontSize = 14.sp)
                        Text("${meal.calories} cal", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Routines
        SectionCard(title = "Mis Rutinas (${routines.size})") {
            if (routines.isEmpty()) {
                Text("Sin rutinas creadas", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                routines.take(3).forEach { routine ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(routine.name, fontSize = 14.sp)
                        Text("${routine.exercises.size} ejercicios", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
