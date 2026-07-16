package com.talos.forge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.data.models.MealRequest
import com.talos.forge.ui.NutritionViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.components.SectionCard

@Composable
fun NutritionScreen(viewModel: NutritionViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val totalCalories by viewModel.totalCalories.collectAsState()
    val totalProtein by viewModel.totalProtein.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var mealName by remember { mutableStateOf("") }
    var mealCalories by remember { mutableStateOf("") }
    var mealProtein by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("SNACK") }

    LaunchedEffect(Unit) { viewModel.loadMeals() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Nutrición", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            error?.let { ErrorText(it) }

            // Summary
            SectionCard(title = "Resumen de Hoy") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalCalories", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Calorías", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${totalProtein}g", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Proteína", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Comidas (${meals.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading && meals.isEmpty()) {
                LoadingSpinner()
                return@Column
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(meals) { meal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(meal.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "${meal.calories} cal · ${meal.protein_g}g prot · ${meal.meal_type}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteMeal(meal.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Comida") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mealName, onValueChange = { mealName = it },
                        label = { Text("Nombre") }, singleLine = true
                    )
                    OutlinedTextField(
                        value = mealCalories, onValueChange = { mealCalories = it.filter { c -> c.isDigit() } },
                        label = { Text("Calorías") }, singleLine = true
                    )
                    OutlinedTextField(
                        value = mealProtein, onValueChange = { mealProtein = it.filter { c -> c.isDigit() } },
                        label = { Text("Proteína (g)") }, singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BREAKFAST" to "Desayuno", "LUNCH" to "Comida", "DINNER" to "Cena", "SNACK" to "Snack").forEach { (value, label) ->
                            FilterChip(
                                selected = mealType == value,
                                onClick = { mealType = value },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (mealName.isNotBlank()) {
                        viewModel.createMeal(MealRequest(
                            name = mealName,
                            calories = mealCalories.toIntOrNull() ?: 0,
                            protein_g = mealProtein.toIntOrNull() ?: 0,
                            meal_type = mealType
                        ))
                        mealName = ""; mealCalories = ""; mealProtein = ""
                        showAddDialog = false
                    }
                }) { Text("Agregar") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }
}
