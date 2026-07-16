package com.talos.forge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.ui.ProfileViewModel
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.components.SectionCard

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onLogout: () -> Unit) {
    val user by viewModel.user.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    if (user == null) {
        LoadingSpinner()
        return
    }

    val u = user!!

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Perfil", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        // Avatar + name
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(u.username.take(2).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(u.username, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(u.email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (u.role != "NORMAL") {
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(onClick = {}, label = { Text(u.role) })
                }
            }
        }

        // Stats
        SectionCard(title = "Datos Personales") {
            ProfileRow("Edad", u.age?.toString() ?: "—")
            ProfileRow("Peso", u.weight_kg?.let { "${it}kg" } ?: "—")
            ProfileRow("Altura", u.height_cm?.let { "${it}cm" } ?: "—")
            ProfileRow("Teléfono", u.phone ?: "—")
            ProfileRow("Objetivo", u.goal ?: "—")
            ProfileRow("Género", if (u.gender == "M") "Masculino" else "Femenino")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout
        Button(
            onClick = { viewModel.logout(); onLogout() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
