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

private const val STATIC_BASE = "http://100.113.102.34:3000/exercises-dataset/"
private fun assetUrl(path: String?) = if (path != null) STATIC_BASE + path else null

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

    LaunchedEffect(aiMessage) {
        if (aiMessage != null) {
            showAISuccess = true
        }
    }

    LaunchedEffect(Unit) { viewModel.loadRoutines() }

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick = { showAIPlanDialog = true },
                    containerColor = Color(0xFF3D3D3D),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Plan")
                }
                FloatingActionButton(
                    onClick = { showAddRoutineDialog = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF141414)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            // View mode selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
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
                            activeContainerColor = Color.White,
                            activeContentColor = Color(0xFF141414),
                            inactiveContainerColor = Color(0xFF282828),
                            inactiveContentColor = Color.White.copy(alpha = 0.7f)
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

    if (showAISuccess && aiMessage != null) {
        AlertDialog(
            onDismissRequest = { showAISuccess = false; viewModel.clearAiMessage() },
            containerColor = Color(0xFF1F1F1F),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA0F03C), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plan Generado", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(aiMessage!!, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = { showAISuccess = false; viewModel.clearAiMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF141414))
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
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Sin rutinas", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Crea una rutina o genera un plan con IA", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center)
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
    progress: Float = 0f
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
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
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF5C6BC0).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF7986CB), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(routine.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val dayLabel = if (routine.day_of_week != null && routine.day_of_week in 0..6) DAY_NAMES[routine.day_of_week] else null
                        val subtitle = buildString {
                            append("${routine.exercises.size} ejercicios")
                            if (routine.ai_generated) append(" · IA")
                            if (dayLabel != null) append(" · $dayLabel")
                        }
                        Text(subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
                Row {
                    IconButton(onClick = onToggle) {
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = onDeleteRoutine) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935).copy(alpha = 0.9f))
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
                        color = if (progress >= 1f) Color(0xFFA0F03C) else Color.White,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$completedCount/${routine.exercises.size}",
                        fontSize = 11.sp,
                        color = if (progress >= 1f) Color(0xFFA0F03C) else Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    if (progress >= 1f) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFA0F03C), modifier = Modifier.size(14.dp))
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
                        Text("Sin ejercicios", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
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
                checkedColor = Color(0xFFA0F03C),
                uncheckedColor = Color.White.copy(alpha = 0.4f),
                checkmarkColor = Color.White
            ),
            modifier = Modifier.size(36.dp)
        )
        val gifUrl = assetUrl(exercise.gif_url)
        val imgUrl = assetUrl(exercise.image)
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF141414)),
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
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                exercise.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isCompleted) Color.White.copy(alpha = 0.5f) else Color.White,
                textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${exercise.sets}x${exercise.reps} · ${exercise.rest_seconds}s descanso",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = if (isCompleted) 0.4f else 0.6f)
            )
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Default.Info, contentDescription = "Detail", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFE53935).copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
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
    val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 }
    val byDay = remember { routines.groupBy { it.day_of_week ?: 99 } }
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val totalEx = routines.sumOf { it.exercises.size }
                    val completedEx = routines.sumOf { it.exercises.count { e -> e.id in completedExercises } }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Esta semana", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "$completedEx/$totalEx",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA0F03C)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = if (totalEx > 0) completedEx.toFloat() / totalEx else 0f,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFA0F03C),
                        trackColor = Color.White.copy(alpha = 0.15f)
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
                                    isSelected && isToday -> Color(0xFFA0F03C).copy(alpha = 0.25f)
                                    isSelected -> Color(0xFFA0F03C).copy(alpha = 0.25f)
                                    isToday -> Color(0xFFA0F03C).copy(alpha = 0.1f)
                                    hasRoutines -> Color(0xFF282828)
                                    else -> Color(0xFF141414)
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
                                isToday -> Color(0xFFA0F03C)
                                isSelected -> Color(0xFFB8F56A)
                                hasRoutines -> Color.White
                                else -> Color.White.copy(alpha = 0.4f)
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected && isToday -> Color(0xFFA0F03C)
                                        isSelected -> Color(0xFFA0F03C)
                                        isToday -> Color(0xFFA0F03C).copy(alpha = 0.3f)
                                        hasRoutines -> Color.White.copy(alpha = 0.15f)
                                        else -> Color.White.copy(alpha = 0.05f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasRoutines) {
                                Text(
                                    "${dayRoutines.sumOf { it.exercises.size }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                                )
                            } else {
                                Text(
                                    "${day + 1}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.3f)
                                )
                            }
                        }
                        if (isToday) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Hoy", fontSize = 9.sp, color = Color(0xFFA0F03C), fontWeight = FontWeight.Bold)
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
                            .background(Color(0xFFA0F03C).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${selectedDay + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB8F56A))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(DAY_NAMES[selectedDay], fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${dayRoutines.sumOf { it.exercises.size }} ejercicios", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Día de descanso", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No hay rutinas para ${DAY_NAMES[selectedDay]}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
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
                            .background(Color(0xFFFFB74D).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sin día asignado", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
        containerColor = Color(0xFF282828),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // GIF
            Box(
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF141414)),
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
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(exercise.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                        containerColor = if (isCompleted) Color(0xFFA0F03C).copy(alpha = 0.2f) else Color(0xFFA0F03C).copy(alpha = 0.15f),
                        contentColor = if (isCompleted) Color(0xFFA0F03C) else Color(0xFFB8F56A)
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
                        contentColor = Color(0xFFFFB74D)
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
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
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
                    Text("Cambiar ejercicio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showSubstitute = false }) {
                        Text("Volver", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
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
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                    }
                } else if (searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        Text("Sin resultados. Intenta otra búsqueda.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
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
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF141414)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dsGif != null) {
                                            AsyncImage(model = dsGif, contentDescription = ds.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else if (dsImg != null) {
                                            AsyncImage(model = dsImg, contentDescription = ds.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ds.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "${ds.target ?: ""} · ${ds.equipment ?: ""}",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.5f),
                                            maxLines = 1
                                        )
                                    }
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(18.dp))
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
            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
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

    // Map day_of_week (0-6) to routines
    val byDow = routines.groupBy { it.day_of_week ?: 99 }
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${monthNames[currentMonth]} $currentYear", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${routines.size} rutinas · $totalExercises ejercicios", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFA0F03C).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${(overallProgress * 100).toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA0F03C)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = overallProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFA0F03C),
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // Calendar grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF282828))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Day-of-week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        dowLabels.forEach { label ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
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
                                                        isToday -> Color(0xFFA0F03C).copy(alpha = 0.2f)
                                                        isCompleted -> Color(0xFFA0F03C).copy(alpha = 0.08f)
                                                        hasRoutines -> Color(0xFFA0F03C).copy(alpha = 0.1f)
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
                                                    isToday -> Color(0xFFA0F03C)
                                                    hasRoutines -> Color.White
                                                    else -> Color.White.copy(alpha = 0.4f)
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
                                                                        if (isCompleted) Color(0xFFA0F03C)
                                                                        else Color(0xFFB8F56A)
                                                                    )
                                                            )
                                                        }
                                                    }
                                                    if (byDow[dow]!!.size > 3) {
                                                        Text("·", fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f))
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
                LegendDot(Color(0xFFA0F03C), "Hoy")
                LegendDot(Color(0xFFB8F56A), "Rutina")
                LegendDot(Color(0xFFA0F03C).copy(alpha = 0.5f), "Completado")
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
                                .background(Color(0xFFA0F03C))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (dow in 0..6) DAY_NAMES[dow] else "Sin día",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                            .background(Color(0xFFFFB74D))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sin día asignado", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
private fun MonthlyStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
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
        containerColor = Color(0xFF282828),
        title = { Text("Nueva Rutina", color = Color.White) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name) }) {
                Text("Crear", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.White.copy(alpha = 0.7f)) }
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
        containerColor = Color(0xFF1F1F1F),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
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
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFA0F03C).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFB8F56A), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Plan con IA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Personaliza tu rutina semanal", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                }
            }

            // Days selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Días por semana", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3, 4, 5, 6).forEach { days ->
                        FilterChip(
                            selected = daysPerWeek == days,
                            onClick = { daysPerWeek = days },
                            label = { Text("$days días", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = Color(0xFF141414)
                            )
                        )
                    }
                }
            }

            // Equipment selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Equipo disponible", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("all" to "Todo", "body weight" to "Peso corporal", "dumbbell" to "Mancuernas", "barbell" to "Barra").forEach { (value, label) ->
                        FilterChip(
                            selected = equipment == value,
                            onClick = { equipment = value },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = Color(0xFF141414)
                            )
                        )
                    }
                }
            }

            // Muscle group selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Grupos musculares", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f))
                Text("Selecciona uno o varios. Deja vacío para cuerpo completo.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    muscleGroupOptions.forEach { (value, label) ->
                        val isSelected = selectedMuscleGroups.contains(value)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (value == "full body") {
                                    selectedMuscleGroups.clear()
                                } else {
                                    selectedMuscleGroups.remove("full body")
                                    if (isSelected) selectedMuscleGroups.remove(value) else selectedMuscleGroups.add(value)
                                }
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFA0F03C),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF2D2D2D),
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }

            // Info note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedMuscleGroups.isEmpty()) "La IA creará rutinas divididas por día según tu perfil"
                        else "La IA enfocará los ejercicios en: ${selectedMuscleGroups.joinToString(", ") { v -> muscleGroupOptions.find { it.first == v }?.second ?: v }}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
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
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Generando tu plan...", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onGenerate(daysPerWeek, equipment, selectedMuscleGroups.toList()) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA0F03C),
                            contentColor = Color.White
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
