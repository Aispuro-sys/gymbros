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
import androidx.compose.ui.graphics.Brush
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

    // Validation states
    var usernameStatus by remember { mutableStateOf<FieldStatus>(FieldStatus.Idle) }
    var emailStatus by remember { mutableStateOf<FieldStatus>(FieldStatus.Idle) }

    val gradientColors = listOf(
        Color(0xFF000014),
        Color(0xFF141414),
        Color(0xFF282828)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // Logo libre sobre el gradiente
            Image(
                painter = painterResource(id = R.drawable.logo_white),
                contentDescription = "Talos Forge Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tarjeta semi-transparente con el formulario
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.10f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLogin) "Bienvenido" else "Crear Cuenta",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isLogin) "Inicia sesión para continuar" else "Completa tus datos",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tab toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLogin) Color.White else Color.Transparent)
                                .clickable { isLogin = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Iniciar Sesión",
                                fontSize = 13.sp,
                                fontWeight = if (isLogin) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLogin) Color(0xFF000014) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isLogin) Color.White else Color.Transparent)
                                .clickable { isLogin = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Registrarse",
                                fontSize = 13.sp,
                                fontWeight = if (!isLogin) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isLogin) Color(0xFF000014) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campos
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
                        Spacer(modifier = Modifier.height(12.dp))
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

                    Spacer(modifier = Modifier.height(12.dp))

                    AuthField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        keyboardType = KeyboardType.Password,
                        isPassword = true
                    )

                    // Gender selector (only for register)
                    AnimatedVisibility(
                        visible = !isLogin,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Género",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.6f),
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

                    // Error
                    error?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE53935).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón
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
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color(0xFF000014),
                            disabledContentColor = Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFF000014),
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
                        Spacer(modifier = Modifier.height(8.dp))
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
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle login/register
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLogin) "¿No tienes cuenta? " else "¿Ya tienes cuenta? ",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = if (isLogin) "Regístrate" else "Inicia sesión",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isLogin = !isLogin }
                        .padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
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
        FieldStatus.Available -> Color(0xFF4CAF50)
        FieldStatus.Taken -> Color(0xFFE53935)
        FieldStatus.Checking -> Color(0xFFFFA726)
        FieldStatus.Idle -> Color.White.copy(alpha = 0.3f)
    }
    val statusText = when (status) {
        FieldStatus.Available -> "Disponible"
        FieldStatus.Taken -> "Ya en uso"
        FieldStatus.Checking -> "Verificando..."
        FieldStatus.Idle -> ""
    }
    val statusColor = when (status) {
        FieldStatus.Available -> Color(0xFF4CAF50)
        FieldStatus.Taken -> Color(0xFFE53935)
        FieldStatus.Checking -> Color(0xFFFFA726)
        FieldStatus.Idle -> Color.Transparent
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedLabelColor = borderColor,
                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
            ),
            trailingIcon = {
                when (status) {
                    FieldStatus.Available -> Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    FieldStatus.Taken -> Text("✗", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    FieldStatus.Checking -> CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp), color = Color(0xFFFFA726))
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
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color(0xFF000014) else Color.White.copy(alpha = 0.7f)
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
        label = { Text(label, fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f)) },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
            cursorColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
        )
    )
}
