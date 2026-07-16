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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.data.models.SupplementRequest
import com.talos.forge.ui.SupplementsViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner

@Composable
fun SupplementsScreen(viewModel: SupplementsViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val supplements by viewModel.supplements.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var timeOfDay by remember { mutableStateOf("MORNING") }
    var isMedication by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadSupplements() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Suplementos", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            error?.let { ErrorText(it) }

            if (isLoading && supplements.isEmpty()) {
                LoadingSpinner()
                return@Column
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(supplements) { supp ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(supp.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "${supp.dosage} · ${supp.time_of_day}${if (supp.is_medication) " · 💊" else ""}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteSupplement(supp.id) }) {
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
            title = { Text("Nuevo Suplemento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, singleLine = true)
                    OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosis") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("MORNING" to "Mañana", "AFTERNOON" to "Tarde", "EVENING" to "Noche").forEach { (v, l) ->
                            FilterChip(selected = timeOfDay == v, onClick = { timeOfDay = v }, label = { Text(l) })
                        }
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = isMedication, onCheckedChange = { isMedication = it })
                        Text("Es medicamento")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank() && dosage.isNotBlank()) {
                        viewModel.createSupplement(SupplementRequest(name, dosage, timeOfDay, isMedication))
                        name = ""; dosage = ""; isMedication = false
                        showAddDialog = false
                    }
                }) { Text("Agregar") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }
}
