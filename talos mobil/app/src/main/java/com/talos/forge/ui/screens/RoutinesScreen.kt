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
import com.talos.forge.ui.RoutinesViewModel
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.components.ErrorText

@Composable
fun RoutinesScreen(viewModel: RoutinesViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newRoutineName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadRoutines() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Rutinas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            error?.let { ErrorText(it) }

            if (isLoading && routines.isEmpty()) {
                LoadingSpinner()
                return@Column
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(routines) { routine ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(routine.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { viewModel.deleteRoutine(routine.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            if (routine.exercises.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                routine.exercises.forEach { ex ->
                                    Text(
                                        "  • ${ex.name} — ${ex.sets}x${ex.reps}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
            title = { Text("Nueva Rutina") },
            text = {
                OutlinedTextField(
                    value = newRoutineName,
                    onValueChange = { newRoutineName = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newRoutineName.isNotBlank()) {
                        viewModel.createRoutine(newRoutineName)
                        newRoutineName = ""
                        showAddDialog = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
