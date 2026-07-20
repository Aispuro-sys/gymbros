package com.talos.forge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Calendar
import com.talos.forge.data.models.Exercise
import com.talos.forge.data.models.ExerciseDataset
import com.talos.forge.data.models.Routine
import com.talos.forge.ui.RoutinesViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.theme.AppColors
import com.talos.forge.data.ApiClient

private fun assetUrl(path: String?) = if (path != null) ApiClient.staticBaseUrl + path else null

private val DAY_NAMES = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(viewModel: RoutinesViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val error by viewModel.error.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val aiMessage by viewModel.aiMessage.collectAsState()
    val completedExercises by viewModel.completedExercises.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var showAISuccess by remember { mutableStateOf(false) }

    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var showAIPlanDialog by remember { mutableStateOf(false) }
    var expandedRoutineId by remember { mutableStateOf<String?>(null) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var showExerciseDetail by remember { mutableStateOf(false) }
    var showExerciseExplorer by remember { mutableStateOf(false) }
    var explorerRoutineId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(aiMessage) {
        if (aiMessage != null) {
            showAISuccess = true
        }
    }

    LaunchedEffect(Unit) { viewModel.loadRoutines() }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            // Top header with title and action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Rutinas", fontSize = 24.sp, fontWeight = FontWeight.Black, color = AppColors.textPrimary)
                    Text("Tu plan de entrenamiento", fontSize = 12.sp, color = AppColors.textSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showAIPlanDialog = true },
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.accentMuted)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Plan IA", tint = AppColors.accent, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = { showAddRoutineDialog = true },
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.accent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva rutina", tint = AppColors.textOnAccent, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // View mode selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                val modes = listOf(
                    RoutinesViewModel.ViewMode.LIST to "Lista",
                    RoutinesViewModel.ViewMode.WEEKLY to "Semanal",
                    RoutinesViewModel.ViewMode.MONTHLY to "Mensual"
                )
                modes.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = viewMode == mode,
                        onClick = { viewModel.setViewMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = AppColors.accent,
                            activeContentColor = AppColors.textOnAccent,
                            inactiveContainerColor = AppColors.cardBgAlt,
                            inactiveContentColor = AppColors.textSecondary
                        )
                    ) {
                        Text(label, fontSize = 13.sp, fontWeight = if (viewMode == mode) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            error?.let { ErrorText(it) }

            if (isLoading && routines.isEmpty()) {
                LoadingSpinner()
                return@Column
            }

            if (routines.isEmpty()) {
                EmptyRoutines()
                return@Column
            }

            if (viewMode == RoutinesViewModel.ViewMode.LIST) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(routines) { routine ->
                        RoutineCard(
                            routine = routine,
                            isExpanded = expandedRoutineId == routine.id,
                            onToggle = { expandedRoutineId = if (expandedRoutineId == routine.id) null else routine.id },
                            onExerciseClick = { ex -> selectedExercise = ex; showExerciseDetail = true },
                            onDeleteRoutine = { viewModel.deleteRoutine(routine.id) },
                            onDeleteExercise = { exId -> viewModel.deleteExercise(routine.id, exId) },
                            completedExercises = completedExercises,
                            onToggleComplete = { exId -> viewModel.toggleExerciseComplete(exId) },
                            onAddExercise = {
                                explorerRoutineId = routine.id
                                showExerciseExplorer = true
                            },
                            progress = viewModel.getRoutineProgress(routine)
                        )
                    }
                }
            } else if (viewMode == RoutinesViewModel.ViewMode.WEEKLY) {
                WeeklyView(
                    routines = routines,
                    expandedRoutineId = expandedRoutineId,
                    onToggle = { id -> expandedRoutineId = if (expandedRoutineId == id) null else id },
                    onExerciseClick = { ex -> selectedExercise = ex; showExerciseDetail = true },
                    onDeleteExercise = { rId, exId -> viewModel.deleteExercise(rId, exId) },
                    completedExercises = completedExercises,
                    onToggleComplete = { exId -> viewModel.toggleExerciseComplete(exId) },
                    getProgress = { viewModel.getRoutineProgress(it) }
                )
            } else {
                MonthlyView(
                    routines = routines,
                    expandedRoutineId = expandedRoutineId,
                    onToggle = { id -> expandedRoutineId = if (expandedRoutineId == id) null else id },
                    onExerciseClick = { ex -> selectedExercise = ex; showExerciseDetail = true },
                    onDeleteExercise = { rId, exId -> viewModel.deleteExercise(rId, exId) },
                    completedExercises = completedExercises,
                    onToggleComplete = { exId -> viewModel.toggleExerciseComplete(exId) },
                    getProgress = { viewModel.getRoutineProgress(it) }
                )
            }
        }
    }

    // Dialogs
    if (showAddRoutineDialog) {
        AddRoutineDialog(
            onDismiss = { showAddRoutineDialog = false },
            onCreate = { name ->
                viewModel.createRoutine(name)
                showAddRoutineDialog = false
            }
        )
    }

    if (showAIPlanDialog) {
        AIPlanDialog(
            onDismiss = { showAIPlanDialog = false },
            isGenerating = isGenerating,
            onGenerate = { days, equipment, muscleGroups ->
                viewModel.generateWeeklyPlan(days, equipment, muscleGroups)
                showAIPlanDialog = false
            }
        )
    }

    if (showExerciseDetail && selectedExercise != null) {
        ExerciseDetailSheet(
            exercise = selectedExercise!!,
            isCompleted = selectedExercise!!.id in completedExercises,
            onToggleComplete = {
                viewModel.toggleExerciseComplete(selectedExercise!!.id)
            },
            searchResults = searchResults,
            isSearching = isSearching,
            onSearchSubstitutes = { query, equipment ->
                viewModel.searchExercises(query, equipment)
            },
            onSubstitute = { datasetId, name ->
                viewModel.addExercise(
                    routineId = selectedExercise!!.routine_id,
                    name = name,
                    sets = selectedExercise!!.sets,
                    reps = selectedExercise!!.reps,
                    restSeconds = selectedExercise!!.rest_seconds,
                    datasetId = datasetId
                )
                viewModel.deleteExercise(selectedExercise!!.routine_id, selectedExercise!!.id)
                showExerciseDetail = false
            },
            onDismiss = { showExerciseDetail = false }
        )
    }

    if (showExerciseExplorer && explorerRoutineId != null) {
        ExerciseExplorerDialog(
            onDismiss = { showExerciseExplorer = false; explorerRoutineId = null },
            searchResults = searchResults,
            isSearching = isSearching,
            onSearch = { query, equipment -> viewModel.searchExercises(query, equipment) },
            onAddExercise = { datasetId, name, sets, reps, rest ->
                viewModel.addExercise(
                    routineId = explorerRoutineId!!,
                    name = name,
                    sets = sets,
                    reps = reps,
                    restSeconds = rest,
                    datasetId = datasetId
                )
            }
        )
    }

    if (showAISuccess && aiMessage != null) {
        AlertDialog(
            onDismissRequest = { showAISuccess = false; viewModel.clearAiMessage() },
            containerColor = AppColors.cardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plan Generado", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(aiMessage!!, color = AppColors.textSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = { showAISuccess = false; viewModel.clearAiMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = AppColors.textOnAccent)
                ) {
                    Text("Ver mi plan")
                }
            }
        )
    }
}

@Composable
private fun EmptyRoutines() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.textTertiary, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Sin rutinas", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Crea una rutina o genera un plan con IA", fontSize = 13.sp, color = AppColors.textSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RoutineCard(
    routine: Routine,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onDeleteRoutine: () -> Unit,
    onDeleteExercise: (String) -> Unit,
    completedExercises: Set<String> = emptySet(),
    onToggleComplete: (String) -> Unit = {},
    onAddExercise: () -> Unit = {},
    progress: Float = 0f
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(AppColors.accentMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(routine.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val dayLabel = if (routine.day_of_week != null && routine.day_of_week in 1..7) DAY_NAMES[routine.day_of_week - 1] else null
                        val subtitle = buildString {
                            append("${routine.exercises.size} ejercicios")
                            if (routine.ai_generated) append(" · IA")
                            if (dayLabel != null) append(" · $dayLabel")
                        }
                        Text(subtitle, fontSize = 11.sp, color = AppColors.textSecondary)
                    }
                }
                Row {
                    IconButton(onClick = onToggle) {
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = AppColors.textSecondary
                        )
                    }
                    IconButton(onClick = onDeleteRoutine) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppColors.danger)
                    }
                }
            }

            // Progress bar
            if (routine.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val completedCount = routine.exercises.count { it.id in completedExercises }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = AppColors.accent,
                        trackColor = AppColors.cardBgAlt
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$completedCount/${routine.exercises.size}",
                        fontSize = 11.sp,
                        color = if (progress >= 1f) AppColors.accent else AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    if (progress >= 1f) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(14.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (routine.exercises.isEmpty()) {
                        Text("Sin ejercicios", fontSize = 13.sp, color = AppColors.textSecondary)
                    } else {
                        routine.exercises.forEach { ex ->
                            ExerciseRow(
                                exercise = ex,
                                isCompleted = ex.id in completedExercises,
                                onToggleComplete = { onToggleComplete(ex.id) },
                                onClick = { onExerciseClick(ex) },
                                onDelete = { onDeleteExercise(ex.id) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onAddExercise,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.accent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Agregar ejercicio", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    isCompleted: Boolean = false,
    onToggleComplete: () -> Unit = {},
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isCompleted,
            onCheckedChange = { onToggleComplete() },
            colors = CheckboxDefaults.colors(
                checkedColor = AppColors.accent,
                uncheckedColor = AppColors.textTertiary,
                checkmarkColor = AppColors.textOnAccent
            ),
            modifier = Modifier.size(36.dp)
        )
        val gifUrl = assetUrl(exercise.gif_url)
        val imgUrl = assetUrl(exercise.image)
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.cardBgAlt),
            contentAlignment = Alignment.Center
        ) {
            if (gifUrl != null) {
                AsyncImage(
                    model = gifUrl,
                    contentDescription = exercise.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (imgUrl != null) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = exercise.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                exercise.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isCompleted) AppColors.textSecondary else AppColors.textPrimary,
                textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${exercise.sets}x${exercise.reps} · ${exercise.rest_seconds}s descanso",
                fontSize = 11.sp,
                color = AppColors.textSecondary
            )
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Default.Info, contentDescription = "Detail", tint = AppColors.textSecondary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = AppColors.danger, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun WeeklyView(
    routines: List<Routine>,
    expandedRoutineId: String?,
    onToggle: (String) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onDeleteExercise: (String, String) -> Unit,
    completedExercises: Set<String> = emptySet(),
    onToggleComplete: (String) -> Unit = {},
    getProgress: (Routine) -> Float = { 0f }
) {
    val today = remember { (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7 }
    val byDay = remember { routines.groupBy { it.day_of_week?.minus(1) ?: 99 } }
    val allDays = remember { (0..6).toList() }
    val unassigned = remember { byDay[99] ?: emptyList() }
    var selectedDay by remember { mutableIntStateOf(today) }

    // Ensure selectedDay has routines or is today
    val hasRoutinesOnDay = byDay.containsKey(selectedDay) || selectedDay == today

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Weekly summary header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val totalEx = routines.sumOf { it.exercises.size }
                    val completedEx = routines.sumOf { it.exercises.count { e -> e.id in completedExercises } }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Esta semana", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                        Text(
                            "$completedEx/$totalEx",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.accent
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = if (totalEx > 0) completedEx.toFloat() / totalEx else 0f,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = AppColors.accent,
                        trackColor = AppColors.cardBgAlt
                    )
                }
            }
        }

        // Horizontal day selector
        item {
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allDays) { day ->
                    val dayRoutines = byDay[day] ?: emptyList()
                    val isToday = day == today
                    val isSelected = day == selectedDay
                    val hasRoutines = dayRoutines.isNotEmpty()

                    Column(
                        modifier = Modifier
                            .width(58.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    isSelected && isToday -> AppColors.accentMuted
                                    isSelected -> AppColors.accentMuted
                                    isToday -> AppColors.accent.copy(alpha = 0.08f)
                                    hasRoutines -> AppColors.cardBgAlt
                                    else -> AppColors.cardBgSubtle
                                }
                            )
                            .clickable { selectedDay = day }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            DAY_NAMES[day].take(3),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isToday -> AppColors.accent
                                isSelected -> AppColors.accent
                                hasRoutines -> AppColors.textPrimary
                                else -> AppColors.textTertiary
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected && isToday -> AppColors.accent
                                        isSelected -> AppColors.accent
                                        isToday -> AppColors.accent.copy(alpha = 0.3f)
                                        hasRoutines -> AppColors.cardBgSubtle
                                        else -> AppColors.cardBgSubtle
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasRoutines) {
                                Text(
                                    "${dayRoutines.sumOf { it.exercises.size }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AppColors.textOnAccent else AppColors.textPrimary
                                )
                            } else {
                                Text(
                                    "${day + 1}",
                                    fontSize = 11.sp,
                                    color = AppColors.textTertiary
                                )
                            }
                        }
                        if (isToday) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Hoy", fontSize = 9.sp, color = AppColors.accent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Selected day content
        val dayRoutines = byDay[selectedDay] ?: emptyList()
        if (dayRoutines.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                            .background(AppColors.accentMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${selectedDay + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.accent)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(DAY_NAMES[selectedDay], fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${dayRoutines.sumOf { it.exercises.size }} ejercicios", fontSize = 12.sp, color = AppColors.textSecondary)
                }
            }

            items(dayRoutines) { routine ->
                RoutineCard(
                    routine = routine,
                    isExpanded = expandedRoutineId == routine.id,
                    onToggle = { onToggle(routine.id) },
                    onExerciseClick = onExerciseClick,
                    onDeleteRoutine = { },
                    onDeleteExercise = { exId -> onDeleteExercise(routine.id, exId) },
                    completedExercises = completedExercises,
                    onToggleComplete = onToggleComplete,
                    progress = getProgress(routine)
                )
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBgAlt)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = AppColors.textTertiary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Día de descanso", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No hay rutinas para ${DAY_NAMES[selectedDay]}", fontSize = 12.sp, color = AppColors.textTertiary)
                    }
                }
            }
        }

        // Unassigned routines
        if (unassigned.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppColors.accentMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sin día asignado", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                }
            }
            items(unassigned) { routine ->
                RoutineCard(
                    routine = routine,
                    isExpanded = expandedRoutineId == routine.id,
                    onToggle = { onToggle(routine.id) },
                    onExerciseClick = onExerciseClick,
                    onDeleteRoutine = { },
                    onDeleteExercise = { exId -> onDeleteExercise(routine.id, exId) },
                    completedExercises = completedExercises,
                    onToggleComplete = onToggleComplete,
                    progress = getProgress(routine)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDetailSheet(
    exercise: Exercise,
    isCompleted: Boolean = false,
    onToggleComplete: () -> Unit = {},
    searchResults: List<ExerciseDataset> = emptyList(),
    isSearching: Boolean = false,
    onSearchSubstitutes: (String, String?) -> Unit = { _, _ -> },
    onSubstitute: (String?, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    val gifUrl = assetUrl(exercise.gif_url)
    val imgUrl = assetUrl(exercise.image)
    var showSubstitute by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // GIF
            Box(
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp)).background(AppColors.cardBgAlt),
                contentAlignment = Alignment.Center
            ) {
                if (gifUrl != null) {
                    AsyncImage(
                        model = gifUrl,
                        contentDescription = exercise.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (imgUrl != null) {
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = exercise.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.textTertiary, modifier = Modifier.size(48.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(exercise.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoChip("${exercise.sets}", "Series")
                InfoChip(exercise.reps, "Reps")
                InfoChip("${exercise.rest_seconds}s", "Descanso")
            }

            if (!showSubstitute) {
                Spacer(modifier = Modifier.height(20.dp))
                // Mark complete button
                Button(
                    onClick = onToggleComplete,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) AppColors.accentMuted else AppColors.accent.copy(alpha = 0.12f),
                        contentColor = AppColors.accent
                    )
                ) {
                    Icon(
                        if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isCompleted) "Completado" else "Marcar como completado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Substitute button
                OutlinedButton(
                    onClick = { showSubstitute = true; onSearchSubstitutes("", null) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.warning
                    )
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar ejercicio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.cardBgAlt,
                        contentColor = AppColors.textPrimary
                    )
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                // Substitute search UI
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cambiar ejercicio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showSubstitute = false }) {
                        Text("Volver", color = AppColors.textSecondary, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchSubstitutes(it, null)
                    },
                    placeholder = { Text("Buscar ejercicio alternativo...", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(18.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.accent,
                        focusedBorderColor = AppColors.accent,
                        unfocusedBorderColor = AppColors.border
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = AppColors.accent)
                    }
                } else if (searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        Text("Sin resultados. Intenta otra búsqueda.", fontSize = 13.sp, color = AppColors.textSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(searchResults.take(15)) { ds ->
                            val dsGif = assetUrl(ds.gif_url)
                            val dsImg = assetUrl(ds.image)
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onSubstitute(ds.id, ds.name)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.cardBgAlt)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.cardBgSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dsGif != null) {
                                            AsyncImage(model = dsGif, contentDescription = ds.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else if (dsImg != null) {
                                            AsyncImage(model = dsImg, contentDescription = ds.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.textTertiary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ds.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "${ds.target ?: ""} · ${ds.equipment ?: ""}",
                                            fontSize = 10.sp,
                                            color = AppColors.textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(AppColors.cardBgAlt),
            contentAlignment = Alignment.Center
        ) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = AppColors.textSecondary)
    }
}

@Composable
private fun MonthlyView(
    routines: List<Routine>,
    expandedRoutineId: String?,
    onToggle: (String) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onDeleteExercise: (String, String) -> Unit,
    completedExercises: Set<String> = emptySet(),
    onToggleComplete: (String) -> Unit = {},
    getProgress: (Routine) -> Float = { 0f }
) {
    val calendar = remember { Calendar.getInstance() }
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val todayDow = calendar.get(Calendar.DAY_OF_WEEK) - 1

    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val totalExercises = routines.sumOf { it.exercises.size }
    val completedCount = routines.sumOf { it.exercises.count { e -> e.id in completedExercises } }
    val overallProgress = if (totalExercises > 0) completedCount.toFloat() / totalExercises else 0f

    // Map day_of_week (1=Monday, 7=Sunday from backend) to calendar grid (0=Sunday, 6=Saturday)
    val byDow = routines.groupBy { it.day_of_week?.let { d -> d % 7 } ?: 99 }
    val unassigned = byDow[99] ?: emptyList()

    // Calendar math
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }.get(Calendar.DAY_OF_WEEK) - 1

    val monthNames = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val dowLabels = listOf("D", "L", "M", "X", "J", "V", "S")

    // For each day of the month, determine which day_of_week it is
    fun dowForDay(day: Int): Int {
        return (firstDayOfMonth + day - 1) % 7
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Monthly overview card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${monthNames[currentMonth]} $currentYear", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            Text("${routines.size} rutinas · $totalExercises ejercicios", fontSize = 12.sp, color = AppColors.textSecondary)
                        }
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(AppColors.accentMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${(overallProgress * 100).toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.accent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = overallProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = AppColors.accent,
                        trackColor = AppColors.cardBgAlt
                    )
                }
            }
        }

        // Calendar grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Day-of-week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        dowLabels.forEach { label ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.textSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar days
                    val totalCells = firstDayOfMonth + daysInMonth
                    val totalRows = (totalCells + 6) / 7

                    for (row in 0 until totalRows) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNum = cellIndex - firstDayOfMonth + 1
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                    if (dayNum in 1..daysInMonth) {
                                        val isToday = dayNum == today
                                        val dow = dowForDay(dayNum)
                                        val hasRoutines = byDow[dow]?.isNotEmpty() == true
                                        val dayProgress = if (hasRoutines) {
                                            byDow[dow]!!.sumOf { it.exercises.count { e -> e.id in completedExercises } }.toFloat() /
                                                byDow[dow]!!.sumOf { it.exercises.size }.coerceAtLeast(1)
                                        } else 0f
                                        val isCompleted = hasRoutines && dayProgress >= 1f

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize(0.9f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    when {
                                                        isToday -> AppColors.accent.copy(alpha = 0.2f)
                                                        isCompleted -> AppColors.accent.copy(alpha = 0.08f)
                                                        hasRoutines -> AppColors.accent.copy(alpha = 0.1f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable { if (hasRoutines) selectedDay = dow },
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                "$dayNum",
                                                fontSize = 14.sp,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                                color = when {
                                                    isToday -> AppColors.accent
                                                    hasRoutines -> AppColors.textPrimary
                                                    else -> AppColors.textTertiary
                                                }
                                            )
                                            if (hasRoutines) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    byDow[dow]!!.forEachIndexed { idx, _ ->
                                                        if (idx < 3) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(
                                                                        if (isCompleted) AppColors.accent
                                                                        else AppColors.accentLight
                                                                    )
                                                            )
                                                        }
                                                    }
                                                    if (byDow[dow]!!.size > 3) {
                                                        Text("·", fontSize = 8.sp, color = AppColors.textSecondary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Legend
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendDot(AppColors.accent, "Hoy")
                LegendDot(AppColors.accentLight, "Rutina")
                LegendDot(AppColors.accent.copy(alpha = 0.5f), "Completado")
            }
        }

        // Selected day routines
        selectedDay?.let { dow ->
            val dayRoutines = byDow[dow] ?: emptyList()
            if (dayRoutines.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(width = 4.dp, height = 20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(AppColors.accent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (dow in 0..6) DAY_NAMES[(dow + 6) % 7] else "Sin día",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                    }
                }
                items(dayRoutines) { routine ->
                    RoutineCard(
                        routine = routine,
                        isExpanded = expandedRoutineId == routine.id,
                        onToggle = { onToggle(routine.id) },
                        onExerciseClick = onExerciseClick,
                        onDeleteRoutine = { },
                        onDeleteExercise = { exId -> onDeleteExercise(routine.id, exId) },
                        completedExercises = completedExercises,
                        onToggleComplete = onToggleComplete,
                        progress = getProgress(routine)
                    )
                }
            }
        }

        // Unassigned routines
        if (unassigned.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(width = 4.dp, height = 20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AppColors.warning)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sin día asignado", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                }
            }
            items(unassigned) { routine ->
                RoutineCard(
                    routine = routine,
                    isExpanded = expandedRoutineId == routine.id,
                    onToggle = { onToggle(routine.id) },
                    onExerciseClick = onExerciseClick,
                    onDeleteRoutine = { },
                    onDeleteExercise = { exId -> onDeleteExercise(routine.id, exId) },
                    completedExercises = completedExercises,
                    onToggleComplete = onToggleComplete,
                    progress = getProgress(routine)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = AppColors.textSecondary)
    }
}

@Composable
private fun MonthlyStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Text(label, fontSize = 11.sp, color = AppColors.textSecondary)
    }
}

@Composable
private fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg,
        title = { Text("Nueva Rutina", color = AppColors.textPrimary) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.accent,
                    focusedBorderColor = AppColors.accent,
                    unfocusedBorderColor = AppColors.border
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name) }) {
                Text("Crear", color = AppColors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.textSecondary) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AIPlanDialog(
    onDismiss: () -> Unit,
    isGenerating: Boolean,
    onGenerate: (Int, String, List<String>) -> Unit
) {
    var daysPerWeek by remember { mutableIntStateOf(4) }
    var equipment by remember { mutableStateOf("all") }
    val selectedMuscleGroups = remember { mutableStateListOf<String>() }

    val muscleGroupOptions = listOf(
        "full body" to "Cuerpo Completo",
        "chest" to "Pecho",
        "back" to "Espalda",
        "upper legs" to "Piernas",
        "shoulders" to "Hombros",
        "upper arms" to "Brazos",
        "lower legs" to "Pantorrillas",
        "waist" to "Core"
    )

    ModalBottomSheet(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        containerColor = AppColors.cardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.border) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(AppColors.accentMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Plan con IA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                    Text("Personaliza tu rutina semanal", fontSize = 12.sp, color = AppColors.textSecondary)
                }
            }

            // Days selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Días por semana", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3, 4, 5, 6).forEach { days ->
                        FilterChip(
                            selected = daysPerWeek == days,
                            onClick = { daysPerWeek = days },
                            label = { Text("$days días", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.accent,
                                selectedLabelColor = AppColors.textOnAccent
                            )
                        )
                    }
                }
            }

            // Equipment selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Equipo disponible", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
                val equipmentOptions = listOf("all" to "Todo", "body weight" to "Peso corporal", "dumbbell" to "Mancuernas", "barbell" to "Barra", "kettlebell" to "Kettlebell", "machine" to "Máquina", "cable" to "Cable")
                var equipmentExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = equipmentExpanded,
                    onExpandedChange = { equipmentExpanded = it }
                ) {
                    OutlinedTextField(
                        value = equipmentOptions.find { it.first == equipment }?.second ?: "Todo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipo", fontSize = 13.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipmentExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
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
                    ExposedDropdownMenu(
                        expanded = equipmentExpanded,
                        onDismissRequest = { equipmentExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        equipmentOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 14.sp, color = if (equipment == value) AppColors.accent else AppColors.textPrimary) },
                                onClick = { equipment = value; equipmentExpanded = false }
                            )
                        }
                    }
                }
            }

            // Muscle group selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Grupos musculares", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
                Text("Selecciona uno o varios. Deja vacío para cuerpo completo.", fontSize = 11.sp, color = AppColors.textSecondary)
                val muscleGroupOptions = listOf(
                    "full body" to "Cuerpo Completo",
                    "chest" to "Pecho",
                    "back" to "Espalda",
                    "upper legs" to "Piernas",
                    "shoulders" to "Hombros",
                    "upper arms" to "Brazos",
                    "lower legs" to "Pantorrillas",
                    "waist" to "Core"
                )
                var muscleExpanded by remember { mutableStateOf(false) }
                val selectedLabels = selectedMuscleGroups.map { v -> muscleGroupOptions.find { it.first == v }?.second ?: v }
                val displayText = if (selectedMuscleGroups.isEmpty()) "Cuerpo completo" else selectedLabels.joinToString(", ")
                ExposedDropdownMenuBox(
                    expanded = muscleExpanded,
                    onExpandedChange = { muscleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = displayText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Músculos", fontSize = 13.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
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
                    ExposedDropdownMenu(
                        expanded = muscleExpanded,
                        onDismissRequest = { muscleExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        muscleGroupOptions.forEach { (value, label) ->
                            val isSelected = selectedMuscleGroups.contains(value)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(checkedColor = AppColors.accent),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(label, fontSize = 14.sp, color = AppColors.textPrimary)
                                    }
                                },
                                onClick = {
                                    if (value == "full body") {
                                        selectedMuscleGroups.clear()
                                    } else {
                                        selectedMuscleGroups.remove("full body")
                                        if (isSelected) selectedMuscleGroups.remove(value) else selectedMuscleGroups.add(value)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Info note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBgAlt)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedMuscleGroups.isEmpty()) "La IA creará rutinas divididas por día según tu perfil"
                        else "La IA enfocará los ejercicios en: ${selectedMuscleGroups.joinToString(", ") { v -> muscleGroupOptions.find { it.first == v }?.second ?: v }}",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary
                    )
                }
            }

            // Generate button
            if (isGenerating) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AppColors.accent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Generando tu plan...", fontSize = 14.sp, color = AppColors.textSecondary)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textSecondary)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onGenerate(daysPerWeek, equipment, selectedMuscleGroups.toList()) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.accent,
                            contentColor = AppColors.textOnAccent
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExerciseExplorerDialog(
    onDismiss: () -> Unit,
    searchResults: List<ExerciseDataset>,
    isSearching: Boolean,
    onSearch: (String?, String?) -> Unit,
    onAddExercise: (String?, String, Int, String, Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedEquipment by remember { mutableStateOf("all") }
    var selectedExercise by remember { mutableStateOf<ExerciseDataset?>(null) }
    var sets by remember { mutableIntStateOf(3) }
    var reps by remember { mutableStateOf("8-12") }
    var restSeconds by remember { mutableIntStateOf(90) }

    val equipmentOptions = listOf(
        "all" to "Todo",
        "body weight" to "Peso corporal",
        "dumbbell" to "Mancuernas",
        "barbell" to "Barra",
        "kettlebell" to "Kettlebell",
        "machine" to "Máquina",
        "cable" to "Cable"
    )

    LaunchedEffect(Unit) {
        onSearch(null, null)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.border) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(AppColors.accentMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Explorar ejercicios", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearch(it.ifBlank { null }, selectedEquipment.takeIf { it != "all" })
                },
                placeholder = { Text("Buscar ejercicio...", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.accent,
                    focusedBorderColor = AppColors.accent,
                    unfocusedBorderColor = AppColors.border
                )
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                equipmentOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = selectedEquipment == value,
                        onClick = {
                            selectedEquipment = value
                            onSearch(searchQuery.ifBlank { null }, value.takeIf { it != "all" })
                        },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.accent,
                            selectedLabelColor = AppColors.textOnAccent,
                            containerColor = AppColors.cardBgAlt,
                            labelColor = AppColors.textSecondary
                        )
                    )
                }
            }

            if (isSearching) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp, color = AppColors.accent)
                }
            } else if (searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("Sin resultados. Intenta otra búsqueda.", fontSize = 13.sp, color = AppColors.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(searchResults.take(30)) { ds ->
                        val dsGif = assetUrl(ds.gif_url)
                        val dsImg = assetUrl(ds.image)
                        val isSelected = selectedExercise?.id == ds.id
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedExercise = if (isSelected) null else ds
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AppColors.accent.copy(alpha = 0.06f) else AppColors.cardBgAlt
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.accent) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.cardBgSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dsGif != null) {
                                        AsyncImage(model = dsGif, contentDescription = ds.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else if (dsImg != null) {
                                        AsyncImage(model = dsImg, contentDescription = ds.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.textTertiary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ds.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "${ds.target_es ?: ds.target ?: ""} · ${ds.equipment_es ?: ds.equipment ?: ""}",
                                        fontSize = 11.sp,
                                        color = AppColors.textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            selectedExercise?.let { ds ->
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBgAlt)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Configurar: ${ds.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Series", fontSize = 12.sp, color = AppColors.textSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (sets > 1) sets-- }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
                                    }
                                    Text("$sets", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { if (sets < 10) sets++ }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reps", fontSize = 12.sp, color = AppColors.textSecondary)
                                OutlinedTextField(
                                    value = reps,
                                    onValueChange = { reps = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary, textAlign = TextAlign.Center),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.accent,
                                        unfocusedBorderColor = AppColors.border
                                    )
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Descanso (s)", fontSize = 12.sp, color = AppColors.textSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (restSeconds > 15) restSeconds -= 15 }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
                                    }
                                    Text("$restSeconds", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { if (restSeconds < 300) restSeconds += 15 }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onAddExercise(ds.id, ds.name, sets, reps, restSeconds)
                                selectedExercise = null
                                sets = 3
                                reps = "8-12"
                                restSeconds = 90
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.accent,
                                contentColor = AppColors.textOnAccent
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agregar a la rutina", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
