package com.talos.forge.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.talos.forge.data.models.SupplementRequest
import com.talos.forge.ui.SupplementsViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.theme.AppColors
import java.io.ByteArrayOutputStream

@Composable
fun SupplementsScreen(viewModel: SupplementsViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val supplements by viewModel.supplements.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var timeOfDay by remember { mutableStateOf("MORNING") }
    var isMedication by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadSupplements() }
    LaunchedEffect(error) {
        if (error != null) {
            kotlinx.coroutines.delay(4000)
            viewModel.clearError()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                viewModel.analyzeSupplementPhoto("data:image/jpeg;base64,$base64")
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // AI camera button
                FloatingActionButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    containerColor = AppColors.accent,
                    contentColor = AppColors.textOnAccent
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "AI Analyze")
                }
                // Add supplement button
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = AppColors.cardBgAlt,
                    contentColor = AppColors.textPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            error?.let { ErrorText(it) }

            if (isLoading && supplements.isEmpty()) {
                LoadingSpinner()
                return@Column
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(supplements) { supp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(AppColors.accentMuted),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (supp.is_medication) Icons.Default.Medication else Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = AppColors.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(supp.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
                                    Text(
                                        "${supp.dosage} · ${supp.time_of_day}${if (supp.is_medication) " · Medicamento" else ""}",
                                        fontSize = 11.sp,
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteSupplement(supp.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppColors.danger)
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Analysis loading dialog
    if (isAnalyzing) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = AppColors.cardBg,
            title = { Text("Analizando suplemento...", color = AppColors.textPrimary) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = AppColors.accent)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("La IA está leyendo la etiqueta...", fontSize = 13.sp, color = AppColors.textSecondary)
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // AI Analysis result dialog
    analysisResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.clearAnalysisResult() },
            containerColor = AppColors.cardBg,
            modifier = Modifier.fillMaxWidth(0.95f),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Análisis IA", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.clearAnalysisResult() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppColors.textSecondary)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(result.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                    result.brand?.let { Text("Marca: $it", fontSize = 13.sp, color = AppColors.textSecondary) }
                    result.category?.let {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                .background(AppColors.accentMuted)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(it, fontSize = 11.sp, color = AppColors.accent)
                        }
                    }
                    result.serving_size?.let { Text("Porción: $it", fontSize = 13.sp, color = AppColors.textSecondary) }
                    result.dose_per_serving?.let { Text("Dosis: $it", fontSize = 13.sp, color = AppColors.textSecondary) }

                    if (result.key_ingredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ingredientes clave:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
                        result.key_ingredients.forEach { ing ->
                            Text("• $ing", fontSize = 12.sp, color = AppColors.textSecondary)
                        }
                    }

                    if (result.calories > 0 || result.protein_g > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (result.calories > 0) MacroPill("${result.calories} kcal")
                            if (result.protein_g > 0) MacroPill("P: ${result.protein_g}g")
                            if (result.carbs_g > 0) MacroPill("C: ${result.carbs_g}g")
                            if (result.fats_g > 0) MacroPill("G: ${result.fats_g}g")
                        }
                    }

                    result.usage_instructions?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Uso: $it", fontSize = 12.sp, color = AppColors.textSecondary)
                    }
                    result.warnings?.let {
                        Text("⚠️ $it", fontSize = 12.sp, color = AppColors.warning)
                    }
                    result.notes?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(it, fontSize = 11.sp, color = AppColors.textTertiary)
                    }
                }
            },
            confirmButton = {
                if (result.name != "No identificado") {
                    TextButton(onClick = {
                        viewModel.createSupplement(SupplementRequest(
                            name = result.name,
                            dosage = result.serving_size ?: "1 porción",
                            time_of_day = "MORNING",
                            is_medication = false
                        ))
                        viewModel.clearAnalysisResult()
                    }) { Text("Agregar a mis suplementos", color = AppColors.accent) }
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.clearAnalysisResult() }) { Text("Cerrar", color = AppColors.textSecondary) } }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = AppColors.cardBg,
            title = { Text("Nuevo Suplemento", color = AppColors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Nombre") }, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.accent,
                            focusedBorderColor = AppColors.accent,
                            unfocusedBorderColor = AppColors.border
                        )
                    )
                    OutlinedTextField(
                        value = dosage, onValueChange = { dosage = it },
                        label = { Text("Dosis") }, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.accent,
                            focusedBorderColor = AppColors.accent,
                            unfocusedBorderColor = AppColors.border
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("MORNING" to "Mañana", "AFTERNOON" to "Tarde", "EVENING" to "Noche").forEach { (v, l) ->
                            FilterChip(
                                selected = timeOfDay == v,
                                onClick = { timeOfDay = v },
                                label = { Text(l) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppColors.accent,
                                    selectedLabelColor = AppColors.textOnAccent
                                )
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isMedication,
                            onCheckedChange = { isMedication = it },
                            colors = CheckboxDefaults.colors(checkedColor = AppColors.accent, checkmarkColor = AppColors.textOnAccent)
                        )
                        Text("Es medicamento", color = AppColors.textPrimary)
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
                }) { Text("Agregar", color = AppColors.accent) }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar", color = AppColors.textSecondary) } }
        )
    }
}

@Composable
private fun MacroPill(text: String) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .background(AppColors.cardBgAlt)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 11.sp, color = AppColors.textSecondary)
    }
}
