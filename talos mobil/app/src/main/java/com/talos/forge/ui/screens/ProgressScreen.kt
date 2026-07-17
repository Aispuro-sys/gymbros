package com.talos.forge.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.talos.forge.data.models.ProgressPhoto
import com.talos.forge.ui.ProgressViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val photos by viewModel.photos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedPhotoIndex by viewModel.selectedPhotoIndex.collectAsState()
    val context = LocalContext.current

    var showWeightDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var weightInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadPhotos() }

    LaunchedEffect(error) {
        if (error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingUri = uri
            showWeightDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Progreso Corporal",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.textPrimary
                            )
                            Text(
                                "Documenta tu evolución",
                                fontSize = 13.sp,
                                color = AppColors.textSecondary
                            )
                        }
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.accentMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(22.dp))
                        }
                    }

                    if (photos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = AppColors.divider, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("${photos.size}", "Fotos")
                            val lastWeight = photos.firstOrNull()?.weight_logged
                            StatItem(lastWeight?.let { "${it}kg" } ?: "—", "Peso actual")
                            if (photos.size >= 2) {
                                val firstWeight = photos.lastOrNull()?.weight_logged
                                val lastW = photos.firstOrNull()?.weight_logged
                                if (firstWeight != null && lastW != null) {
                                    val diff = lastW - firstWeight
                                    val sign = if (diff > 0) "+" else ""
                                    StatItem("$sign${String.format("%.1f", diff)}kg", "Cambio")
                                } else {
                                    StatItem("—", "Cambio")
                                }
                            } else {
                                StatItem("—", "Cambio")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val firstDate = photos.lastOrNull()?.date?.take(10)
                        val lastDate = photos.firstOrNull()?.date?.take(10)
                        if (firstDate != null && lastDate != null) {
                            Text(
                                "${formatDate(firstDate)} — ${formatDate(lastDate)}",
                                fontSize = 11.sp,
                                color = AppColors.textSecondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            error?.let { ErrorText(it) }

            if (isLoading && photos.isEmpty()) {
                LoadingSpinner()
                return@Column
            }

            if (photos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = AppColors.textSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sin fotos de progreso",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Sube tu primera foto para empezar\na registrar tu evolución",
                        fontSize = 13.sp,
                        color = AppColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = AppColors.textOnAccent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Subir foto", fontWeight = FontWeight.Bold)
                    }
                }
                return@Column
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(photos) { photo ->
                    ProgressPhotoCard(
                        photo = photo,
                        onClick = {
                            val idx = photos.indexOf(photo)
                            viewModel.selectPhoto(idx)
                        },
                        onDelete = { viewModel.deletePhoto(photo.id) },
                        onEditWeight = { newWeight ->
                            viewModel.updateWeight(photo.id, newWeight)
                        }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { photoPickerLauncher.launch("image/*") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = AppColors.accent,
            contentColor = AppColors.textOnAccent
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = AppColors.textOnAccent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.Add, contentDescription = "Add photo")
            }
        }
    }

    // Weight dialog before upload
    if (showWeightDialog && pendingUri != null) {
        WeightInputDialog(
            title = "Registrar peso",
            subtitle = "¿Quieres registrar tu peso actual junto con la foto?",
            initialWeight = "",
            onConfirm = { weight ->
                pendingUri?.let { viewModel.uploadPhoto(it, context, weight) }
                showWeightDialog = false
                pendingUri = null
                weightInput = ""
            },
            onDismiss = {
                showWeightDialog = false
                pendingUri = null
                weightInput = ""
            }
        )
    }

    // Full screen photo viewer
    if (selectedPhotoIndex >= 0 && selectedPhotoIndex < photos.size) {
        PhotoViewerDialog(
            photos = photos,
            initialIndex = selectedPhotoIndex,
            onDismiss = { viewModel.clearSelectedPhoto() },
            onEditWeight = { photo, weight ->
                viewModel.updateWeight(photo.id, weight)
            },
            onDelete = { photo ->
                viewModel.deletePhoto(photo.id)
                viewModel.clearSelectedPhoto()
            }
        )
    }
}

@Composable
private fun ProgressPhotoCard(
    photo: ProgressPhoto,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEditWeight: (Float?) -> Unit
) {
    var showEditWeight by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            AsyncImage(
                model = photo.photo_url,
                contentDescription = "Progress photo",
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatDate(photo.date.take(10)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )
                    photo.weight_logged?.let {
                        Text("$it kg", fontSize = 12.sp, color = AppColors.accent)
                    } ?: Text(
                        "Sin peso",
                        fontSize = 11.sp,
                        color = AppColors.textSecondary
                    )
                }
                Row {
                    IconButton(onClick = { showEditWeight = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AppColors.textSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppColors.danger.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    if (showEditWeight) {
        WeightInputDialog(
            title = "Editar peso",
            subtitle = formatDate(photo.date.take(10)),
            initialWeight = photo.weight_logged?.toString() ?: "",
            onConfirm = { weight ->
                onEditWeight(weight)
                showEditWeight = false
            },
            onDismiss = { showEditWeight = false }
        )
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Text(label, fontSize = 10.sp, color = AppColors.textSecondary)
    }
}

@Composable
private fun WeightInputDialog(
    title: String,
    subtitle: String,
    initialWeight: String,
    onConfirm: (Float?) -> Unit,
    onDismiss: () -> Unit
) {
    var weightInput by remember { mutableStateOf(initialWeight) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg,
        title = {
            Text(title, color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(subtitle, fontSize = 13.sp, color = AppColors.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Peso (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.accent,
                        focusedBorderColor = AppColors.accent,
                        unfocusedBorderColor = AppColors.border,
                        focusedLabelColor = AppColors.accent,
                        unfocusedLabelColor = AppColors.textSecondary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val weight = weightInput.toFloatOrNull()
                onConfirm(weight)
            }) { Text("Guardar", color = AppColors.accent, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.textSecondary) }
        }
    )
}

@Composable
private fun PhotoViewerDialog(
    photos: List<ProgressPhoto>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onEditWeight: (ProgressPhoto, Float?) -> Unit,
    onDelete: (ProgressPhoto) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(initialIndex) }
    var showEditWeight by remember { mutableStateOf(false) }
    val photo = photos[currentIndex]

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.bg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatDate(photo.date.take(10)),
                        color = AppColors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    photo.weight_logged?.let {
                        Text("$it kg", fontSize = 13.sp, color = AppColors.accent)
                    }
                }
                Row {
                    IconButton(onClick = { showEditWeight = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AppColors.textSecondary)
                    }
                    IconButton(onClick = { onDelete(photo) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppColors.danger)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppColors.textSecondary)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = photo.photo_url,
                    contentDescription = "Progress photo",
                    modifier = Modifier.fillMaxWidth().height(400.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = if (currentIndex > 0) AppColors.accent else AppColors.textSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Anterior", color = if (currentIndex > 0) AppColors.accent else AppColors.textSecondary)
                    }
                    Text("${currentIndex + 1}/${photos.size}", fontSize = 12.sp, color = AppColors.textSecondary)
                    TextButton(
                        onClick = { if (currentIndex < photos.size - 1) currentIndex++ },
                        enabled = currentIndex < photos.size - 1
                    ) {
                        Text("Siguiente", color = if (currentIndex < photos.size - 1) AppColors.accent else AppColors.textSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (currentIndex < photos.size - 1) AppColors.accent else AppColors.textSecondary)
                    }
                }
            }
        },
        confirmButton = {}
    )

    if (showEditWeight) {
        WeightInputDialog(
            title = "Editar peso",
            subtitle = formatDate(photo.date.take(10)),
            initialWeight = photo.weight_logged?.toString() ?: "",
            onConfirm = { weight ->
                onEditWeight(photo, weight)
                showEditWeight = false
            },
            onDismiss = { showEditWeight = false }
        )
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy", Locale("es"))
        val date = inputFormat.parse(dateStr)
        outputFormat.format(date)
    } catch (e: Exception) {
        dateStr
    }
}
