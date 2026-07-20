package com.talos.forge.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.talos.forge.data.models.ProgressPhoto
import com.talos.forge.data.models.WeeklyCount
import com.talos.forge.data.models.WorkoutLog
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
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedPhotoIndex by viewModel.selectedPhotoIndex.collectAsState()
    val context = LocalContext.current

    var showLogDialog by remember { mutableStateOf(false) }
    var showWorkoutDialog by remember { mutableStateOf(false) }
    var showPhotoPicker by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    val workoutLogs by viewModel.workoutLogs.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAll() }

    LaunchedEffect(error) {
        if (error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            pendingUri = cameraImageUri
            showLogDialog = true
        }
    }

    fun launchCamera() {
        val photoFile = java.io.File(context.cacheDir, "photos/progress_${System.currentTimeMillis()}.jpg")
        photoFile.parentFile?.mkdirs()
        cameraImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        cameraImageUri?.let { cameraLauncher.launch(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Seguimiento",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = AppColors.textPrimary
                        )
                        Text(
                            "Tu progreso, tu evidencia",
                            fontSize = 13.sp,
                            color = AppColors.textSecondary
                        )
                    }
                    Row {
                        IconButton(
                            onClick = { showWorkoutDialog = true },
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.accentMuted)
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = "Registrar entreno", tint = AppColors.accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showLogDialog = true },
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.accentMuted)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Registrar", tint = AppColors.accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { launchCamera() },
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.accent)
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(color = AppColors.textOnAccent, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Foto", tint = AppColors.textOnAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Error
            error?.let { item { ErrorText(it) } }

            // Loading
            if (isLoading && photos.isEmpty() && stats.total_workout_days == 0) {
                item { LoadingSpinner(modifier = Modifier.height(200.dp)) }
            }

            // Streak & Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StreakCard(
                        streak = stats.streak,
                        longestStreak = stats.longest_streak,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        label = "Esta semana",
                        value = "${stats.workouts_this_week}",
                        sublabel = "entrenos",
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        label = "Total",
                        value = "${stats.total_workout_days}",
                        sublabel = "días",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Secondary stats: meals & rest days
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMiniCard(
                        label = "Comidas esta semana",
                        value = "${stats.meals_this_week}",
                        sublabel = "registros",
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        label = "Días de descanso",
                        value = "${stats.rest_days}",
                        sublabel = "registrados",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Weekly Activity Chart
            item {
                WeeklyActivityChart(weeklyCounts = stats.weekly_counts)
            }

            // Body Summary Card
            if (photos.isNotEmpty()) {
                item {
                    BodySummaryCard(photos = photos)
                }
            }

            // Tab selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabChip("Fotos", selectedTab == 0) { selectedTab = 0 }
                    TabChip("Mediciones", selectedTab == 1) { selectedTab = 1 }
                    TabChip("Entrenos", selectedTab == 2) { selectedTab = 2 }
                }
            }

            // Content based on tab
            if (selectedTab == 0) {
                if (photos.isEmpty()) {
                    item {
                        EmptyPhotoState { launchCamera() }
                    }
                } else {
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(((photos.size + 1) / 2 * 280).dp),
                            userScrollEnabled = false
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
                }
            } else if (selectedTab == 1) {
                item {
                    MeasurementsList(photos = photos)
                }
            } else {
                item {
                    WorkoutLogList(
                        logs = workoutLogs,
                        onDelete = { viewModel.deleteWorkoutLog(it) },
                        onAdd = { showWorkoutDialog = true }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Workout log dialog
    if (showWorkoutDialog) {
        WorkoutLogDialog(
            onConfirm = { type, duration, intensity, notes ->
                viewModel.logWorkout(type, duration, intensity, notes)
                showWorkoutDialog = false
            },
            onDismiss = { showWorkoutDialog = false }
        )
    }

    // Log dialog (measurements and/or photo)
    if (showLogDialog) {
        LogEntryDialog(
            hasPhoto = pendingUri != null,
            onTakePhoto = {
                pendingUri = null
                launchCamera()
            },
            onConfirm = { weight, waist, chest, hip, arm, leg, bodyFat, note ->
                if (pendingUri != null) {
                    viewModel.uploadPhoto(
                        pendingUri!!, context, weight, waist, chest, hip, arm, leg, bodyFat, note
                    )
                } else {
                    viewModel.logMeasurements(weight, waist, chest, hip, arm, leg, bodyFat, note)
                }
                showLogDialog = false
                pendingUri = null
            },
            onDismiss = {
                showLogDialog = false
                pendingUri = null
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
private fun StreakCard(streak: Int, longestStreak: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.accent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Whatshot, contentDescription = null, tint = AppColors.textOnAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Racha", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.textOnAccent.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$streak",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.textOnAccent
            )
            Text(
                "días seguidos",
                fontSize = 11.sp,
                color = AppColors.textOnAccent.copy(alpha = 0.7f)
            )
            if (longestStreak > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Récord: $longestStreak",
                    fontSize = 10.sp,
                    color = AppColors.textOnAccent.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatMiniCard(label: String, value: String, sublabel: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(label, fontSize = 11.sp, color = AppColors.textSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.textPrimary
            )
            Text(sublabel, fontSize = 10.sp, color = AppColors.textTertiary)
        }
    }
}

@Composable
private fun WeeklyActivityChart(weeklyCounts: List<WeeklyCount>) {
    val maxCount = (weeklyCounts.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val barLabels = weeklyCounts.map { wc ->
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outFmt = SimpleDateFormat("d/M", Locale.getDefault())
            outFmt.format(sdf.parse(wc.week_start)!!)
        } catch (e: Exception) {
            ""
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                Text(
                    "Actividad semanal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Text(
                    "últimas 8 semanas",
                    fontSize = 11.sp,
                    color = AppColors.textTertiary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyCounts.forEachIndexed { index, wc ->
                    val barHeight = (wc.count.toFloat() / maxCount * 100).coerceIn(4f, 100f)
                    val isCurrent = index == weeklyCounts.lastIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (wc.count > 0) "${wc.count}" else "",
                            fontSize = 9.sp,
                            color = AppColors.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (isCurrent) AppColors.accent else AppColors.cardBgAlt)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            barLabels[index],
                            fontSize = 8.sp,
                            color = if (isCurrent) AppColors.textPrimary else AppColors.textTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BodySummaryCard(photos: List<ProgressPhoto>) {
    val latest = photos.firstOrNull()
    val first = photos.lastOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "Resumen corporal",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Weight
            val latestWeight = latest?.weight_logged
            val firstWeight = first?.weight_logged
            val weightDiff = if (latestWeight != null && firstWeight != null) latestWeight - firstWeight else null
            MeasurementRow("Peso", latestWeight?.let { "${String.format("%.1f", it)} kg" } ?: "—", weightDiff?.let { diff ->
                val sign = if (diff > 0) "+" else ""
                "$sign${String.format("%.1f", diff)} kg"
            })

            HorizontalDivider(color = AppColors.divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

            val latestWaist = latest?.waist_cm
            val firstWaist = first?.waist_cm
            val waistDiff = if (latestWaist != null && firstWaist != null) latestWaist - firstWaist else null
            MeasurementRow("Cintura", latestWaist?.let { "${String.format("%.1f", it)} cm" } ?: "—", waistDiff?.let { diff ->
                val sign = if (diff > 0) "+" else ""
                "$sign${String.format("%.1f", diff)} cm"
            })

            HorizontalDivider(color = AppColors.divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

            val latestBodyFat = latest?.body_fat_pct
            val firstBodyFat = first?.body_fat_pct
            val bodyFatDiff = if (latestBodyFat != null && firstBodyFat != null) latestBodyFat - firstBodyFat else null
            MeasurementRow("% Grasa", latestBodyFat?.let { "${String.format("%.1f", it)}%" } ?: "—", bodyFatDiff?.let { diff ->
                val sign = if (diff > 0) "+" else ""
                "$sign${String.format("%.1f", diff)}%"
            })
        }
    }
}

@Composable
private fun MeasurementRow(label: String, value: String, change: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = AppColors.textSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            if (change != null) {
                Spacer(modifier = Modifier.width(10.dp))
                val isPositive = change.startsWith("+")
                Text(
                    change,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) AppColors.warning else AppColors.success
                )
            }
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) AppColors.accent else AppColors.cardBgAlt, label = "tabBg")
    val fg by animateColorAsState(if (selected) AppColors.textOnAccent else AppColors.textSecondary, label = "tabFg")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun EmptyPhotoState(onUpload: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = AppColors.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Sin fotos de progreso", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Sube tu primera foto para empezar\na registrar tu evolución", fontSize = 13.sp, color = AppColors.textSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onUpload,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = AppColors.textOnAccent)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Subir foto", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MeasurementsList(photos: List<ProgressPhoto>) {
    val withMeasurements = photos.filter { p ->
        p.weight_logged != null || p.waist_cm != null || p.chest_cm != null ||
        p.hip_cm != null || p.arm_cm != null || p.leg_cm != null || p.body_fat_pct != null
    }
    if (withMeasurements.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Straighten, contentDescription = null, tint = AppColors.textSecondary.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Sin mediciones", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Registra tus medidas corporales\npara seguir tu progreso", fontSize = 13.sp, color = AppColors.textSecondary, textAlign = TextAlign.Center)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            withMeasurements.forEach { photo ->
                MeasurementEntryCard(photo)
            }
        }
    }
}

@Composable
private fun MeasurementEntryCard(photo: ProgressPhoto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                formatDate(photo.date.take(10)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            if (photo.note != null) {
                Text(photo.note, fontSize = 12.sp, color = AppColors.textSecondary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (photo.weight_logged != null) MeasurementPill("Peso", "${String.format("%.1f", photo.weight_logged)} kg")
                if (photo.waist_cm != null) MeasurementPill("Cintura", "${String.format("%.1f", photo.waist_cm)} cm")
                if (photo.chest_cm != null) MeasurementPill("Pecho", "${String.format("%.1f", photo.chest_cm)} cm")
                if (photo.hip_cm != null) MeasurementPill("Cadera", "${String.format("%.1f", photo.hip_cm)} cm")
                if (photo.arm_cm != null) MeasurementPill("Brazo", "${String.format("%.1f", photo.arm_cm)} cm")
                if (photo.leg_cm != null) MeasurementPill("Pierna", "${String.format("%.1f", photo.leg_cm)} cm")
                if (photo.body_fat_pct != null) MeasurementPill("% Grasa", "${String.format("%.1f", photo.body_fat_pct)}%")
            }
        }
    }
}

@Composable
private fun MeasurementPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.cardBgAlt)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 9.sp, color = AppColors.textTertiary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
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
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            if (photo.photo_url != null) {
                AsyncImage(
                    model = photo.photo_url,
                    contentDescription = "Progress photo",
                    modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop
                )
            }
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
                        Text("${String.format("%.1f", it)} kg", fontSize = 12.sp, color = AppColors.textSecondary)
                    } ?: Text(
                        "Sin peso",
                        fontSize = 11.sp,
                        color = AppColors.textTertiary
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
private fun LogEntryDialog(
    hasPhoto: Boolean,
    onTakePhoto: () -> Unit,
    onConfirm: (Float?, Float?, Float?, Float?, Float?, Float?, Float?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var hip by remember { mutableStateOf("") }
    var arm by remember { mutableStateOf("") }
    var leg by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showAdvanced by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nuevo registro", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                if (hasPhoto) {
                    Text("Con foto", fontSize = 11.sp, color = AppColors.textSecondary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (hasPhoto) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.accentMuted)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Foto lista para subir", fontSize = 12.sp, color = AppColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                MeasurementField("Peso (kg)", weight) { weight = it }
                Spacer(modifier = Modifier.height(10.dp))
                MeasurementField("Cintura (cm)", waist) { waist = it }

                if (showAdvanced) {
                    Spacer(modifier = Modifier.height(10.dp))
                    MeasurementField("Pecho (cm)", chest) { chest = it }
                    Spacer(modifier = Modifier.height(10.dp))
                    MeasurementField("Cadera (cm)", hip) { hip = it }
                    Spacer(modifier = Modifier.height(10.dp))
                    MeasurementField("Brazo (cm)", arm) { arm = it }
                    Spacer(modifier = Modifier.height(10.dp))
                    MeasurementField("Pierna (cm)", leg) { leg = it }
                    Spacer(modifier = Modifier.height(10.dp))
                    MeasurementField("% Grasa corporal", bodyFat) { bodyFat = it }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (showAdvanced) "Ocultar" else "Más mediciones",
                        fontSize = 12.sp,
                        color = AppColors.accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showAdvanced = !showAdvanced }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota (opcional)") },
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
                onConfirm(
                    weight.toFloatOrNull(),
                    waist.toFloatOrNull(),
                    chest.toFloatOrNull(),
                    hip.toFloatOrNull(),
                    arm.toFloatOrNull(),
                    leg.toFloatOrNull(),
                    bodyFat.toFloatOrNull(),
                    note.ifBlank { null }
                )
            }) { Text("Guardar", color = AppColors.accent, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            Row {
                if (!hasPhoto) {
                    TextButton(onClick = onTakePhoto) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Foto", color = AppColors.textSecondary)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.textSecondary) }
            }
        }
    )
}

@Composable
private fun MeasurementField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) },
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
                        Text("${String.format("%.1f", it)} kg", fontSize = 13.sp, color = AppColors.textSecondary)
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
                if (photo.photo_url != null) {
                    AsyncImage(
                        model = photo.photo_url,
                        contentDescription = "Progress photo",
                        modifier = Modifier.fillMaxWidth().height(400.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.cardBgAlt),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Straighten, contentDescription = null, tint = AppColors.textTertiary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Solo mediciones", fontSize = 13.sp, color = AppColors.textSecondary)
                        }
                    }
                }
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
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = if (currentIndex > 0) AppColors.accent else AppColors.textTertiary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Anterior", color = if (currentIndex > 0) AppColors.accent else AppColors.textTertiary)
                    }
                    Text("${currentIndex + 1}/${photos.size}", fontSize = 12.sp, color = AppColors.textSecondary)
                    TextButton(
                        onClick = { if (currentIndex < photos.size - 1) currentIndex++ },
                        enabled = currentIndex < photos.size - 1
                    ) {
                        Text("Siguiente", color = if (currentIndex < photos.size - 1) AppColors.accent else AppColors.textTertiary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (currentIndex < photos.size - 1) AppColors.accent else AppColors.textTertiary)
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

@Composable
private fun WorkoutLogList(
    logs: List<WorkoutLog>,
    onDelete: (String) -> Unit,
    onAdd: () -> Unit
) {
    if (logs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = AppColors.textSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Sin entrenamientos registrados", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Registra tus entrenamientos o días\nde descanso para llevar un control", fontSize = 13.sp, color = AppColors.textSecondary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = AppColors.textOnAccent)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar entreno", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            logs.forEach { log -> WorkoutLogCard(log = log, onDelete = { onDelete(log.id) }) }
        }
    }
}

@Composable
private fun WorkoutLogCard(log: WorkoutLog, onDelete: () -> Unit) {
    val typeConfig = when (log.type) {
        "WORKOUT" -> Triple(Icons.Default.FitnessCenter, "Entrenamiento", AppColors.accent)
        "CARDIO" -> Triple(Icons.Default.DirectionsRun, "Cardio", AppColors.success)
        "REST" -> Triple(Icons.Default.Bed, "Día de descanso", AppColors.textSecondary)
        else -> Triple(Icons.Default.FitnessCenter, log.type, AppColors.accent)
    }
    val (icon, label, color) = typeConfig

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                Text(formatDate(log.date.take(10)), fontSize = 12.sp, color = AppColors.textSecondary)
                log.duration_min?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$it min", fontSize = 12.sp, color = AppColors.textTertiary)
                }
                log.notes?.let {
                    if (it.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(it, fontSize = 12.sp, color = AppColors.textSecondary, maxLines = 2)
                    }
                }
            }
            if (log.intensity != null) {
                val intensityColor = when (log.intensity) {
                    "HIGH" -> AppColors.danger
                    "MEDIUM" -> AppColors.warning
                    "LOW" -> AppColors.success
                    else -> AppColors.textTertiary
                }
                val intensityLabel = when (log.intensity) {
                    "HIGH" -> "Alta"
                    "MEDIUM" -> "Media"
                    "LOW" -> "Baja"
                    else -> log.intensity
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = intensityColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        intensityLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = intensityColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppColors.danger.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun WorkoutLogDialog(
    onConfirm: (String, Int?, String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf("WORKOUT") }
    var duration by remember { mutableStateOf("") }
    var selectedIntensity by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    val typeOptions = listOf(
        Triple("WORKOUT", "Entrenamiento", Icons.Default.FitnessCenter),
        Triple("CARDIO", "Cardio", Icons.Default.DirectionsRun),
        Triple("REST", "Descanso", Icons.Default.Bed)
    )
    val intensityOptions = listOf("LOW" to "Baja", "MEDIUM" to "Media", "HIGH" to "Alta")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Registrar entrenamiento", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Tipo", fontSize = 13.sp, color = AppColors.textSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    typeOptions.forEach { (value, label, icon) ->
                        val isSelected = selectedType == value
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AppColors.accent else AppColors.cardBgAlt)
                                .clickable { selectedType = value }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, tint = if (isSelected) AppColors.textOnAccent else AppColors.textSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) AppColors.textOnAccent else AppColors.textSecondary)
                        }
                    }
                }

                if (selectedType != "REST") {
                    Spacer(modifier = Modifier.height(14.dp))
                    MeasurementField("Duración (min)", duration) { duration = it }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Intensidad", fontSize = 13.sp, color = AppColors.textSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intensityOptions.forEach { (value, label) ->
                            val isSelected = selectedIntensity == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) AppColors.accent else AppColors.cardBgAlt)
                                    .clickable { selectedIntensity = if (isSelected) null else value }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) AppColors.textOnAccent else AppColors.textSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    singleLine = false,
                    maxLines = 3,
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
                onConfirm(
                    selectedType,
                    duration.toIntOrNull(),
                    selectedIntensity,
                    notes.ifBlank { null }
                )
            }) { Text("Guardar", color = AppColors.accent, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.textSecondary) }
        }
    )
}
