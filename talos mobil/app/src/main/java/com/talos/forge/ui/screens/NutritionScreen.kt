package com.talos.forge.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.talos.forge.data.models.MealRequest
import com.talos.forge.ui.NutritionViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.theme.AppColors
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(viewModel: NutritionViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val totalCalories by viewModel.totalCalories.collectAsState()
    val totalProtein by viewModel.totalProtein.collectAsState()
    val totalCarbs by viewModel.totalCarbs.collectAsState()
    val totalFats by viewModel.totalFats.collectAsState()
    val error by viewModel.error.collectAsState()
    val foodAnalysis by viewModel.foodAnalysis.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val weeklySummary by viewModel.weeklySummary.collectAsState()
    val nutritionPlan by viewModel.nutritionPlan.collectAsState()
    val isLoadingPlan by viewModel.isLoadingPlan.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showWeeklySummary by remember { mutableStateOf(false) }
    var showAIPlan by remember { mutableStateOf(false) }
    var mealName by remember { mutableStateOf("") }
    var mealCalories by remember { mutableStateOf("") }
    var mealProtein by remember { mutableStateOf("") }
    var mealCarbs by remember { mutableStateOf("") }
    var mealFats by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("SNACK") }
    var selectedMealType by remember { mutableStateOf("SNACK") }
    val context = LocalContext.current
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMeals()
        viewModel.loadWeeklySummary()
    }

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
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                viewModel.analyzeFoodPhoto("data:image/jpeg;base64,$base64")
            } catch (_: Exception) {}
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(cameraImageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                viewModel.analyzeFoodPhoto("data:image/jpeg;base64,$base64")
            } catch (_: Exception) {}
        }
    }

    fun launchCamera() {
        val photoFile = java.io.File(context.cacheDir, "photos/food_${System.currentTimeMillis()}.jpg")
        photoFile.parentFile?.mkdirs()
        cameraImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        cameraImageUri?.let { cameraLauncher.launch(it) }
    }

    val tabs = listOf("Hoy" to Icons.Default.Restaurant, "IA Cámara" to Icons.Default.PhotoCamera, "Agregar" to Icons.Default.Add)

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AppColors.cardBg,
            contentColor = AppColors.accent
        ) {
            tabs.forEachIndexed { idx, (title, icon) ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, fontSize = 12.sp) },
                    icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        error?.let { ErrorText(it) }

        when (selectedTab) {
            0 -> TodayTab(
                isLoading = isLoading,
                meals = meals,
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFats = totalFats,
                weeklySummary = weeklySummary,
                showWeeklySummary = showWeeklySummary,
                onToggleWeekly = { showWeeklySummary = !showWeeklySummary },
                onDeleteMeal = { viewModel.deleteMeal(it) },
                onNavigateTab = { selectedTab = it }
            )
            1 -> AICameraTab(
                isAnalyzing = isAnalyzing,
                onPickPhoto = { showPhotoSourceDialog = true }
            )
            2 -> ManualAddTab(
                mealName = mealName, onMealNameChange = { mealName = it },
                mealCalories = mealCalories, onMealCaloriesChange = { mealCalories = it.filter { c -> c.isDigit() } },
                mealProtein = mealProtein, onMealProteinChange = { mealProtein = it.filter { c -> c.isDigit() } },
                mealCarbs = mealCarbs, onMealCarbsChange = { mealCarbs = it.filter { c -> c.isDigit() } },
                mealFats = mealFats, onMealFatsChange = { mealFats = it.filter { c -> c.isDigit() } },
                mealType = mealType, onMealTypeChange = { mealType = it },
                onAdd = {
                    if (mealName.isNotBlank()) {
                        viewModel.createMeal(MealRequest(
                            name = mealName,
                            calories = mealCalories.toIntOrNull() ?: 0,
                            protein_g = mealProtein.toIntOrNull() ?: 0,
                            carbs_g = mealCarbs.toIntOrNull() ?: 0,
                            fats_g = mealFats.toIntOrNull() ?: 0,
                            meal_type = mealType
                        ))
                        mealName = ""; mealCalories = ""; mealProtein = ""; mealCarbs = ""; mealFats = ""
                        selectedTab = 0
                    }
                },
                onShowAIPlan = { showAIPlan = true }
            )
        }
    }

    // Photo source selection dialog
    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            containerColor = AppColors.cardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Análisis con IA", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            launchCamera()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tomar foto", color = AppColors.textPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    }
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            photoPickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Elegir de galería", color = AppColors.textPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoSourceDialog = false }) {
                    Text("Cancelar", color = AppColors.textSecondary)
                }
            }
        )
    }

    // AI Food Analysis result
    if (foodAnalysis != null) {
        FoodAnalysisDialog(
            analysis = foodAnalysis!!,
            isAnalyzing = isAnalyzing,
            selectedMealType = selectedMealType,
            onMealTypeChange = { selectedMealType = it },
            onAdd = {
                viewModel.createMealFromAnalysis(foodAnalysis!!, selectedMealType)
                viewModel.clearFoodAnalysis()
                selectedTab = 0
            },
            onDismiss = { viewModel.clearFoodAnalysis() }
        )
    }

    if (showAIPlan) {
        AIPlanDialog(
            plan = nutritionPlan,
            isLoading = isLoadingPlan,
            onLoad = { viewModel.loadNutritionPlan() },
            onAddMeal = { name, type, cal, prot, carb, fat ->
                viewModel.addMealFromPlan(name, type, cal, prot, carb, fat)
            },
            onDismiss = { showAIPlan = false }
        )
    }
}

@Composable
private fun TodayTab(
    isLoading: Boolean,
    meals: List<com.talos.forge.data.models.Meal>,
    totalCalories: Int,
    totalProtein: Int,
    totalCarbs: Int,
    totalFats: Int,
    weeklySummary: com.talos.forge.data.models.WeeklyNutritionSummary?,
    showWeeklySummary: Boolean,
    onToggleWeekly: () -> Unit,
    onDeleteMeal: (String) -> Unit,
    onNavigateTab: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        // Summary card con gradiente
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(AppColors.gradientStart, AppColors.gradientEnd)))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Resumen de hoy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                        TextButton(onClick = onToggleWeekly) {
                            Text("Semana", fontSize = 12.sp, color = AppColors.accent)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MacroPill("$totalCalories", "Cal", Icons.Default.LocalFireDepartment)
                        MacroPill("${totalProtein}g", "Prot", Icons.Default.FitnessCenter)
                        MacroPill("${totalCarbs}g", "Carbs", Icons.Default.Grain)
                        MacroPill("${totalFats}g", "Grasas", Icons.Default.Opacity)
                    }
                }
            }
        }

        if (showWeeklySummary && weeklySummary != null) {
            Spacer(modifier = Modifier.height(8.dp))
            WeeklySummaryCard(weeklySummary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && meals.isEmpty()) {
            LoadingSpinner()
            return@Column
        }

        if (meals.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🍽️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Sin comidas registradas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Toma una foto o agrega manualmente", fontSize = 13.sp, color = AppColors.textSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onNavigateTab(1) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.success)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AppColors.success, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("IA Cámara")
                    }
                    OutlinedButton(
                        onClick = { onNavigateTab(2) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.accent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar")
                    }
                }
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(meals) { meal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        meal.photo_url?.let { photoUrl ->
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = meal.name,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        } ?: run {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(AppColors.accentMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalDining, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meal.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${meal.calories} cal · ${meal.protein_g}g prot · ${meal.carbs_g}g carb · ${meal.fats_g}g grasa",
                                fontSize = 11.sp,
                                color = AppColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (meal.confirmed) {
                                Text("Confirmada con foto", fontSize = 10.sp, color = AppColors.success)
                            }
                        }
                        IconButton(onClick = { onDeleteMeal(meal.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppColors.danger.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AICameraTab(
    isAnalyzing: Boolean,
    onPickPhoto: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(AppColors.success.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AppColors.success, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Análisis con IA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Toma una foto de tu comida y la IA calculará calorías, proteína, carbohidratos y grasas automáticamente.",
                    fontSize = 13.sp,
                    color = AppColors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (isAnalyzing) {
                    CircularProgressIndicator(color = AppColors.accent, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Analizando...", fontSize = 13.sp, color = AppColors.textSecondary)
                } else {
                    Button(
                        onClick = onPickPhoto,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.success, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar/Subir Foto", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManualAddTab(
    mealName: String, onMealNameChange: (String) -> Unit,
    mealCalories: String, onMealCaloriesChange: (String) -> Unit,
    mealProtein: String, onMealProteinChange: (String) -> Unit,
    mealCarbs: String, onMealCarbsChange: (String) -> Unit,
    mealFats: String, onMealFatsChange: (String) -> Unit,
    mealType: String, onMealTypeChange: (String) -> Unit,
    onAdd: () -> Unit,
    onShowAIPlan: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppColors.textPrimary,
        unfocusedTextColor = AppColors.textPrimary,
        cursorColor = AppColors.accent,
        focusedBorderColor = AppColors.accent,
        unfocusedBorderColor = AppColors.border,
        focusedLabelColor = AppColors.accent,
        unfocusedLabelColor = AppColors.textSecondary,
        focusedContainerColor = AppColors.cardBg,
        unfocusedContainerColor = AppColors.cardBg
    )

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Agregar Comida Manualmente", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = mealName, onValueChange = onMealNameChange,
            label = { Text("Nombre de la comida") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text("Tipo de comida", fontSize = 14.sp, color = AppColors.textSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("BREAKFAST" to "Desayuno", "LUNCH" to "Comida", "DINNER" to "Cena", "SNACK" to "Snack").forEach { (value, label) ->
                FilterChip(
                    selected = mealType == value,
                    onClick = { onMealTypeChange(value) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.accent,
                        selectedLabelColor = AppColors.textOnAccent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Macros grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = mealCalories, onValueChange = onMealCaloriesChange,
                label = { Text("Calorías") }, singleLine = true,
                modifier = Modifier.weight(1f),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = mealProtein, onValueChange = onMealProteinChange,
                label = { Text("Prot (g)") }, singleLine = true,
                modifier = Modifier.weight(1f),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = mealCarbs, onValueChange = onMealCarbsChange,
                label = { Text("Carbos (g)") }, singleLine = true,
                modifier = Modifier.weight(1f),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = mealFats, onValueChange = onMealFatsChange,
                label = { Text("Grasas (g)") }, singleLine = true,
                modifier = Modifier.weight(1f),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = AppColors.textOnAccent)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar comida", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onShowAIPlan,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.accent)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Plan nutricional IA")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun MacroPill(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(AppColors.accentMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Text(label, fontSize = 10.sp, color = AppColors.textSecondary)
    }
}

@Composable
private fun WeeklySummaryCard(summary: com.talos.forge.data.models.WeeklyNutritionSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBgAlt)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen semanal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStat("${summary.avgCalories}", "Avg cal")
                SummaryStat("${summary.totalMeals}", "Comidas")
                SummaryStat("${summary.avgProtein}g", "Avg prot")
            }
            if (summary.days.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                summary.days.filter { it.calories > 0 || it.meals_total > 0 }.forEach { day ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            try { java.text.SimpleDateFormat("EEE d", java.util.Locale("es")).format(java.text.SimpleDateFormat("yyyy-MM-dd").parse(day.date)) } catch (e: Exception) { day.date },
                            fontSize = 11.sp, color = AppColors.textSecondary
                        )
                        Text("${day.calories} cal · ${day.meals_total} comidas", fontSize = 11.sp, color = AppColors.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.success)
        Text(label, fontSize = 10.sp, color = AppColors.textSecondary)
    }
}

@Composable
private fun FoodAnalysisDialog(
    analysis: com.talos.forge.data.models.FoodAnalysis,
    isAnalyzing: Boolean,
    selectedMealType: String,
    onMealTypeChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isAnalyzing) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = AppColors.cardBg,
            title = { Text("Analizando comida...", color = AppColors.textPrimary) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = AppColors.accent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    Text("La IA está analizando tu foto", fontSize = 13.sp, color = AppColors.textSecondary)
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.textSecondary) } }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = AppColors.cardBg,
            title = { Text("Análisis de comida", color = AppColors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(analysis.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SummaryStat("${analysis.calories}", "Calorías")
                        SummaryStat("${analysis.protein_g}g", "Proteína")
                        SummaryStat("${analysis.carbs_g}g", "Carbos")
                        SummaryStat("${analysis.fats_g}g", "Grasas")
                    }
                    analysis.notes?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(it, fontSize = 12.sp, color = AppColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tipo de comida", fontSize = 12.sp, color = AppColors.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("BREAKFAST" to "Desayuno", "LUNCH" to "Comida", "DINNER" to "Cena", "SNACK" to "Snack").forEach { (value, label) ->
                            FilterChip(
                                selected = selectedMealType == value,
                                onClick = { onMealTypeChange(value) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppColors.accent,
                                    selectedLabelColor = AppColors.textOnAccent
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onAdd) { Text("Agregar comida", color = AppColors.success) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.textSecondary) } }
        )
    }
}

@Composable
private fun AIPlanDialog(
    plan: com.talos.forge.data.models.NutritionPlan?,
    isLoading: Boolean,
    onLoad: () -> Unit,
    onAddMeal: (String, String, Int, Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg,
        title = { Text("Plan nutricional IA", color = AppColors.textPrimary) },
        text = {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = AppColors.accent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    Text("Generando plan...", fontSize = 13.sp, color = AppColors.textSecondary)
                }
            } else if (plan == null) {
                Text("Genera un plan personalizado con IA basado en tu perfil", fontSize = 13.sp, color = AppColors.textSecondary)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SummaryStat("${plan.calories}", "Cal/día")
                        SummaryStat("${plan.protein_g}g", "Prot")
                        SummaryStat("${plan.carbs_g}g", "Carbos")
                        SummaryStat("${plan.fats_g}g", "Grasas")
                    }
                    plan.notes?.let {
                        Text(it, fontSize = 11.sp, color = AppColors.textSecondary)
                    }
                    if (plan.meals.isNotEmpty()) {
                        Text("Comidas recomendadas", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                        plan.meals.forEach { meal ->
                            val n = meal.numeric
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onAddMeal(meal.meal, meal.meal_type, n?.calories ?: 0, n?.protein_g ?: 0, n?.carbs_g ?: 0, n?.fats_g ?: 0)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.cardBgSubtle)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(meal.meal, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
                                    n?.let {
                                        Text("${it.calories} cal · ${it.protein_g}g prot", fontSize = 11.sp, color = AppColors.textSecondary)
                                    }
                                    Text("Toca para agregar", fontSize = 10.sp, color = AppColors.success)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (plan == null && !isLoading) {
                TextButton(onClick = onLoad) { Text("Generar plan", color = AppColors.accent) }
            } else if (plan != null) {
                TextButton(onClick = onDismiss) { Text("Cerrar", color = AppColors.textSecondary) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.textSecondary) } }
    )
}

@Composable
private fun whiteFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppColors.textPrimary,
    unfocusedTextColor = AppColors.textPrimary,
    cursorColor = AppColors.accent,
    focusedBorderColor = AppColors.accent,
    unfocusedBorderColor = AppColors.border
)
