package com.talos.forge.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.talos.forge.R
import com.talos.forge.data.AppSettings
import com.talos.forge.ui.ProfileViewModel
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.theme.AppColors
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onLogout: () -> Unit) {
    val user by viewModel.user.collectAsState()
    val error by viewModel.error.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(false) }
    var photoBase64 by remember { mutableStateOf<String?>(null) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            isEditing = false
            photoBase64 = null
            viewModel.clearUpdateSuccess()
        }
    }
    LaunchedEffect(error) {
        if (error != null) {
            kotlinx.coroutines.delay(4000)
            viewModel.clearError()
        }
    }

    if (user == null) {
        LoadingSpinner()
        return
    }

    val u = user!!

    fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) return null

            val maxDim = 512
            val ratio = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
            val scaledBitmap = if (ratio < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    true
                )
            } else {
                bitmap
            }
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"
        } catch (_: Exception) { null }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoBase64 = uriToBase64(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            photoBase64 = uriToBase64(cameraImageUri!!)
        }
    }

    fun launchCamera() {
        val photoFile = java.io.File(context.cacheDir, "photos/profile_${System.currentTimeMillis()}.jpg")
        photoFile.parentFile?.mkdirs()
        cameraImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        cameraImageUri?.let { cameraLauncher.launch(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header card con gradiente
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
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Photo with edit overlay when editing
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .then(
                                if (isEditing) Modifier.clickable { showPhotoSourceDialog = true } else Modifier
                            ),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val photoToShow = photoBase64 ?: u.profile_photo
                        if (photoToShow != null) {
                            AsyncImage(
                                model = photoToShow,
                                contentDescription = "Foto",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(AppColors.accentMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(36.dp))
                            }
                        }
                        if (isEditing) {
                            Box(
                                modifier = Modifier.size(24.dp).clip(CircleShape).background(AppColors.accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", tint = AppColors.textOnAccent, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    if (isEditing) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Toca la foto para cambiar", fontSize = 11.sp, color = AppColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(u.username, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                    Text(u.email, fontSize = 13.sp, color = AppColors.textSecondary)
                    if (u.role != "NORMAL") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.accentMuted
                        ) {
                            Text(
                                u.role,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.accent
                            )
                        }
                    }
                }
            }
        }

        error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.danger.copy(alpha = 0.08f))
            ) {
                Text(
                    it,
                    modifier = Modifier.padding(14.dp),
                    fontSize = 13.sp,
                    color = AppColors.danger
                )
            }
        }

        if (isEditing) {
            // ===== Inline edit form =====
            InlineEditForm(
                user = u,
                isUpdating = isUpdating,
                onCancel = {
                    isEditing = false
                    photoBase64 = null
                },
                onSave = { username, age, heightCm, weightKg, goal, bodyType, gender, bio, phone ->
                    viewModel.updateProfile(
                        username = username,
                        age = age,
                        heightCm = heightCm,
                        weightKg = weightKg,
                        goal = goal,
                        bodyType = bodyType,
                        gender = gender,
                        bio = bio,
                        phone = phone,
                        profilePhoto = photoBase64
                    )
                }
            )
        } else {
            // ===== Read-only profile data =====
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
                        Text("Datos Personales", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                        TextButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar", fontSize = 13.sp, color = AppColors.accent)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    ProfileRow("Edad", u.age?.toString() ?: "—")
                    ProfileRow("Peso", u.weight_kg?.let { "${it}kg" } ?: "—")
                    ProfileRow("Altura", u.height_cm?.let { "${it}cm" } ?: "—")
                    ProfileRow("Teléfono", u.phone ?: "—")
                    ProfileRow("Objetivo", goalLabel(u.goal))
                    ProfileRow("Tipo de cuerpo", bodyTypeLabel(u.body_type))
                    ProfileRow("Género", if (u.gender == "M") "Masculino" else "Femenino")
                    if (u.bio != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Bio", fontSize = 14.sp, color = AppColors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(u.bio, fontSize = 14.sp, color = AppColors.textPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Settings gear with dropdown
        var showLogoutDialog by remember { mutableStateOf(false) }
        var showSettingsMenu by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { showSettingsMenu = !showSettingsMenu },
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(AppColors.cardBgAlt)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Configuración",
                    tint = AppColors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            DropdownMenu(
                expanded = showSettingsMenu,
                onDismissRequest = { showSettingsMenu = false },
                modifier = Modifier.width(220.dp)
            ) {
                // Language toggle
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Idioma", fontSize = 14.sp, color = AppColors.textPrimary, fontWeight = FontWeight.Medium)
                                Text(if (AppSettings.isSpanish) "Español" else "English", fontSize = 11.sp, color = AppColors.textSecondary)
                            }
                            Text(
                                if (AppSettings.isSpanish) "EN" else "ES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.accent
                            )
                        }
                    },
                    onClick = { AppSettings.toggleLanguage(); showSettingsMenu = false }
                )
                HorizontalDivider(color = AppColors.divider)
                // Dark mode toggle
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (AppSettings.isDarkMode.value) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = AppColors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tema", fontSize = 14.sp, color = AppColors.textPrimary, fontWeight = FontWeight.Medium)
                                Text(if (AppSettings.isDarkMode.value) "Oscuro" else "Claro", fontSize = 11.sp, color = AppColors.textSecondary)
                            }
                            Switch(
                                checked = AppSettings.isDarkMode.value,
                                onCheckedChange = { AppSettings.setDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AppColors.textOnAccent,
                                    checkedTrackColor = AppColors.accent,
                                    uncheckedThumbColor = AppColors.textSecondary,
                                    uncheckedTrackColor = AppColors.cardBgAlt
                                )
                            )
                        }
                    },
                    onClick = { AppSettings.toggleDarkMode() }
                )
                HorizontalDivider(color = AppColors.divider)
                // Logout
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = AppColors.danger, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Cerrar Sesión", fontSize = 14.sp, color = AppColors.danger, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    onClick = { showSettingsMenu = false; showLogoutDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showPhotoSourceDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoSourceDialog = false },
                containerColor = AppColors.cardBg,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Cambiar foto", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
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
                                galleryLauncher.launch("image/*")
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

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = AppColors.cardBg,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = AppColors.danger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Cerrar Sesión", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        "¿Estás seguro de que quieres cerrar sesión?",
                        fontSize = 14.sp,
                        color = AppColors.textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                            onLogout()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.danger,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancelar", color = AppColors.textSecondary, fontSize = 14.sp)
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = AppColors.textSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun goalLabel(goal: String?): String = when (goal) {
    "LOSE_WEIGHT" -> "Bajar de peso"
    "GAIN_MUSCLE" -> "Ganar músculo"
    "MAINTENANCE" -> "Mantenerse"
    "RECOMP" -> "Recomposición"
    else -> goal ?: "—"
}

private fun bodyTypeLabel(bodyType: String?): String = when (bodyType) {
    "ECTOMORPH" -> "Ectomorfo"
    "MESOMORPH" -> "Mesomorfo"
    "ENDOMORPH" -> "Endomorfo"
    else -> bodyType ?: "—"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InlineEditForm(
    user: com.talos.forge.data.models.User,
    isUpdating: Boolean,
    onCancel: () -> Unit,
    onSave: (username: String?, age: Int?, heightCm: Float?, weightKg: Float?, goal: String?, bodyType: String?, gender: String?, bio: String?, phone: String?) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var age by remember { mutableStateOf(user.age?.toString() ?: "") }
    var heightCm by remember { mutableStateOf(user.height_cm?.toString() ?: "") }
    var weightKg by remember { mutableStateOf(user.weight_kg?.toString() ?: "") }
    var goal by remember { mutableStateOf(user.goal ?: "MAINTENANCE") }
    var bodyType by remember { mutableStateOf(user.body_type ?: "") }
    var gender by remember { mutableStateOf(user.gender ?: "M") }
    var bio by remember { mutableStateOf(user.bio ?: "") }
    var phone by remember { mutableStateOf(user.phone ?: "") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppColors.textPrimary,
        unfocusedTextColor = AppColors.textPrimary,
        cursorColor = AppColors.accent,
        focusedBorderColor = AppColors.accent,
        unfocusedBorderColor = AppColors.border,
        focusedLabelColor = AppColors.accent,
        unfocusedLabelColor = AppColors.textSecondary,
        focusedContainerColor = AppColors.cardBgAlt,
        unfocusedContainerColor = AppColors.cardBgAlt
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Editar Perfil", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppColors.accent)
                }
            }

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Usuario") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Teléfono") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Edad") }, singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = weightKg, onValueChange = { weightKg = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Peso (kg)") }, singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            OutlinedTextField(
                value = heightCm, onValueChange = { heightCm = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Altura (cm)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            // Goal selector
            Text("Objetivo", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("LOSE_WEIGHT" to "Bajar", "GAIN_MUSCLE" to "Músculo", "MAINTENANCE" to "Mantener", "RECOMP" to "Recomp").forEach { (value, label) ->
                    FilterChip(
                        selected = goal == value,
                        onClick = { goal = value },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.accent,
                            selectedLabelColor = AppColors.textOnAccent
                        )
                    )
                }
            }

            // Body Type selector with visual cards
            Text("Tipo de Cuerpo", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondary)
            BodyTypeSelector(selected = bodyType, gender = gender, onSelect = { bodyType = it })

            // Gender selector
            Text("Género", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("M" to "Masculino", "F" to "Femenino").forEach { (value, label) ->
                    FilterChip(
                        selected = gender == value,
                        onClick = { gender = value },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.accent,
                            selectedLabelColor = AppColors.textOnAccent
                        )
                    )
                }
            }

            OutlinedTextField(
                value = bio, onValueChange = { bio = it },
                label = { Text("Bio") },
                minLines = 2, maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isUpdating,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.textSecondary
                    )
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        onSave(
                            username.ifBlank { null },
                            age.toIntOrNull(),
                            heightCm.toFloatOrNull(),
                            weightKg.toFloatOrNull(),
                            goal,
                            bodyType.ifBlank { null },
                            gender,
                            bio.ifBlank { null },
                            phone.ifBlank { null }
                        )
                    },
                    enabled = !isUpdating,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.accent,
                        contentColor = AppColors.textOnAccent
                    )
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppColors.textOnAccent)
                    } else {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyTypeSelector(selected: String, gender: String, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val bodyTypes = listOf(
        Triple("ECTOMORPH", "Ectomorfo", "Delgado y alto, metabolismo rápido, dificultad para ganar peso"),
        Triple("MESOMORPH", "Mesomorfo", "Atlético y musculoso, facilidad para ganar músculo"),
        Triple("ENDOMORPH", "Endomorfo", "Complexión sólida, tendencia a acumular grasa")
    )
    val imageMap = mapOf(
        "ECTOMORPH" to if (gender == "F") R.drawable.body_ectomorph_female else R.drawable.body_ectomorph_male,
        "MESOMORPH" to if (gender == "F") R.drawable.body_mesomorph_female else R.drawable.body_mesomorph_male,
        "ENDOMORPH" to if (gender == "F") R.drawable.body_endomorph_female else R.drawable.body_endomorph_male
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        bodyTypes.forEach { (value, label, desc) ->
            val isSelected = selected == value
            val imageRes = imageMap[value]!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(value) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) AppColors.accentMuted else AppColors.cardBgAlt
                ),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AppColors.accent) else null
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.cardBgSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(imageRes),
                            contentDescription = label,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                        Text(desc, fontSize = 11.sp, color = AppColors.textSecondary, maxLines = 2)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
