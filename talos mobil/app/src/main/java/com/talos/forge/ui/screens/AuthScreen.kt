package com.talos.forge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.R
import com.talos.forge.ui.AuthViewModel
import com.talos.forge.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()

    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("M") }

    var usernameStatus by remember { mutableStateOf<FieldStatus>(FieldStatus.Idle) }
    var emailStatus by remember { mutableStateOf<FieldStatus>(FieldStatus.Idle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_black),
            contentDescription = "Talos Forge Logo",
            modifier = Modifier.size(130.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isLogin) "Bienvenido" else "Crear Cuenta",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = AppColors.textPrimary
        )
        Text(
            text = if (isLogin) "Inicia sesión para continuar" else "Completa tus datos",
            fontSize = 14.sp,
            color = AppColors.textSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Tab toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.cardBgAlt)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLogin) AppColors.accent else Color.Transparent)
                    .clickable { isLogin = true }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Iniciar Sesión",
                    fontSize = 13.sp,
                    fontWeight = if (isLogin) FontWeight.Bold else FontWeight.Normal,
                    color = if (isLogin) AppColors.textOnAccent else AppColors.textSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isLogin) AppColors.accent else Color.Transparent)
                    .clickable { isLogin = false }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Registrarse",
                    fontSize = 13.sp,
                    fontWeight = if (!isLogin) FontWeight.Bold else FontWeight.Normal,
                    color = if (!isLogin) AppColors.textOnAccent else AppColors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fields
        if (!isLogin) {
            ValidatedField(
                value = username,
                onValueChange = { newValue ->
                    username = newValue
                    usernameStatus = if (newValue.length >= 3) FieldStatus.Checking else FieldStatus.Idle
                    if (newValue.length >= 3) {
                        scope.launch {
                            try {
                                val available = withContext(Dispatchers.IO) {
                                    viewModel.repository.checkUsername(newValue)
                                }
                                usernameStatus = if (available) FieldStatus.Available else FieldStatus.Taken
                            } catch (e: Exception) {
                                usernameStatus = FieldStatus.Idle
                            }
                        }
                    }
                },
                label = "Usuario",
                status = usernameStatus
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (!isLogin) {
            ValidatedField(
                value = email,
                onValueChange = { newValue ->
                    email = newValue
                    val isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(newValue).matches()
                    emailStatus = if (isValidEmail) FieldStatus.Checking else FieldStatus.Idle
                    if (isValidEmail) {
                        scope.launch {
                            try {
                                val available = withContext(Dispatchers.IO) {
                                    viewModel.repository.checkEmail(newValue)
                                }
                                emailStatus = if (available) FieldStatus.Available else FieldStatus.Taken
                            } catch (e: Exception) {
                                emailStatus = FieldStatus.Idle
                            }
                        }
                    }
                },
                label = "Email",
                keyboardType = KeyboardType.Email,
                status = emailStatus
            )
        } else {
            AuthField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        AuthField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        AnimatedVisibility(
            visible = !isLogin,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    "Género",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GenderChip(
                        label = "Masculino",
                        selected = gender == "M",
                        onClick = { gender = "M" },
                        modifier = Modifier.weight(1f)
                    )
                    GenderChip(
                        label = "Femenino",
                        selected = gender == "F",
                        onClick = { gender = "F" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppColors.danger.copy(alpha = 0.08f)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(14.dp),
                    fontSize = 13.sp,
                    color = AppColors.danger
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val canSubmit = if (isLogin) {
            email.isNotBlank() && password.isNotBlank()
        } else {
            email.isNotBlank() && password.isNotBlank() && username.isNotBlank() &&
                    usernameStatus == FieldStatus.Available &&
                    emailStatus == FieldStatus.Available
        }

        Button(
            onClick = {
                if (isLogin) {
                    viewModel.login(email, password)
                } else {
                    viewModel.register(
                        username = username,
                        email = email,
                        password = password,
                        gender = gender
                    )
                }
            },
            enabled = !isLoading && canSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.accent,
                disabledContainerColor = AppColors.accent.copy(alpha = 0.2f),
                contentColor = AppColors.textOnAccent,
                disabledContentColor = AppColors.textOnAccent.copy(alpha = 0.4f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = AppColors.textOnAccent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = if (isLogin) "Iniciar Sesión" else "Crear Cuenta",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!canSubmit && !isLoading) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isLogin) {
                    "Completa email y contraseña"
                } else {
                    when {
                        username.isBlank() -> "Ingresa un usuario (mín. 3 caracteres)"
                        usernameStatus != FieldStatus.Available -> "Verifica tu usuario"
                        email.isBlank() -> "Ingresa tu email"
                        emailStatus != FieldStatus.Available -> "Verifica tu email"
                        password.isBlank() -> "Ingresa una contraseña"
                        else -> "Completa todos los campos"
                    }
                },
                fontSize = 11.sp,
                color = AppColors.textTertiary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isLogin) "¿No tienes cuenta? " else "¿Ya tienes cuenta? ",
                fontSize = 13.sp,
                color = AppColors.textSecondary
            )
            Text(
                text = if (isLogin) "Regístrate" else "Inicia sesión",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.accent,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isLogin = !isLogin }
                    .padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

enum class FieldStatus { Idle, Checking, Available, Taken }

@Composable
private fun ValidatedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    status: FieldStatus,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val borderColor = when (status) {
        FieldStatus.Available -> AppColors.success
        FieldStatus.Taken -> AppColors.danger
        FieldStatus.Checking -> AppColors.warning
        FieldStatus.Idle -> AppColors.border
    }
    val statusText = when (status) {
        FieldStatus.Available -> "Disponible"
        FieldStatus.Taken -> "Ya en uso"
        FieldStatus.Checking -> "Verificando..."
        FieldStatus.Idle -> ""
    }
    val statusColor = when (status) {
        FieldStatus.Available -> AppColors.success
        FieldStatus.Taken -> AppColors.danger
        FieldStatus.Checking -> AppColors.warning
        FieldStatus.Idle -> Color.Transparent
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp, color = AppColors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.textPrimary,
                unfocusedTextColor = AppColors.textPrimary,
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedLabelColor = borderColor,
                unfocusedLabelColor = AppColors.textSecondary,
                cursorColor = AppColors.accent,
                focusedContainerColor = AppColors.cardBg,
                unfocusedContainerColor = AppColors.cardBg
            ),
            trailingIcon = {
                when (status) {
                    FieldStatus.Available -> Text("✓", color = AppColors.success, fontWeight = FontWeight.Bold)
                    FieldStatus.Taken -> Text("✗", color = AppColors.danger, fontWeight = FontWeight.Bold)
                    FieldStatus.Checking -> CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp), color = AppColors.warning)
                    FieldStatus.Idle -> {}
                }
            }
        )
        if (status != FieldStatus.Idle) {
            Text(
                text = statusText,
                fontSize = 11.sp,
                color = statusColor,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun GenderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AppColors.accent else AppColors.cardBgAlt)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) AppColors.textOnAccent else AppColors.textSecondary
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp, color = AppColors.textSecondary) },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.textPrimary,
            unfocusedTextColor = AppColors.textPrimary,
            focusedBorderColor = AppColors.accent,
            unfocusedBorderColor = AppColors.border,
            focusedLabelColor = AppColors.accent,
            unfocusedLabelColor = AppColors.textSecondary,
            cursorColor = AppColors.accent,
            focusedContainerColor = AppColors.cardBg,
            unfocusedContainerColor = AppColors.cardBg
        )
    )
}
