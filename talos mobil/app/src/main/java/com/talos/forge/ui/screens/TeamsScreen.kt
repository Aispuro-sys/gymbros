package com.talos.forge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.data.models.Routine
import com.talos.forge.data.models.Team
import com.talos.forge.ui.TeamsViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(viewModel: TeamsViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTeams()
    }

    LaunchedEffect(error) {
        if (error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    if (isLoading && teams.isEmpty() && selectedTeam == null) {
        LoadingSpinner()
        return
    }

    if (selectedTeam != null) {
        TeamDetailScreen(
            team = selectedTeam!!,
            viewModel = viewModel,
            onBack = { viewModel.clearSelectedTeam() }
        )
    } else {
        TeamsListScreen(
            teams = teams,
            viewModel = viewModel,
            error = error
        )
    }
}

@Composable
private fun TeamsListScreen(
    teams: List<Team>,
    viewModel: TeamsViewModel,
    error: String?
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick = { showJoinDialog = true },
                    containerColor = Color(0xFF3D3D3D),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "Join")
                }
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1A1A1A)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            error?.let { ErrorText(it) }

            if (teams.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sin equipos", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Crea un equipo o únete con un código", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(teams) { team ->
                        TeamCard(team = team, onClick = { viewModel.loadTeamDetail(team.id) })
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTeamDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createTeam(name)
                showCreateDialog = false
            }
        )
    }

    if (showJoinDialog) {
        JoinTeamDialog(
            onDismiss = { showJoinDialog = false },
            onJoin = { code ->
                viewModel.joinTeam(code)
                showJoinDialog = false
            }
        )
    }
}

@Composable
private fun TeamCard(team: Team, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFA0F03C).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    team.name.take(2).uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB8F56A)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(team.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${team.members.size} miembros${if (team.role == "ADMIN") " · Admin" else ""}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamDetailScreen(
    team: Team,
    viewModel: TeamsViewModel,
    onBack: () -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    var showShareDialog by remember { mutableStateOf(false) }
    var postText by remember { mutableStateOf("") }
    var showLeaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(team.id) { viewModel.loadRoutines() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(team.name, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { showLeaveDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Leave", tint = Color(0xFFE53935))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Invite code card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFB8F56A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Código de invitación", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(team.invite_code, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Members
            item {
                Text("Miembros (${team.members.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            items(team.members) { member ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF5C6BC0).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (member.user?.username ?: "?").take(1).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7986CB)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(member.user?.username ?: "Usuario", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        if (member.role == "ADMIN") {
                            Text("Admin", fontSize = 11.sp, color = Color(0xFFB8F56A))
                        }
                    }
                }
            }

            // Shared routines
            if (team.shared_routines.isNotEmpty()) {
                item {
                    Text("Rutinas compartidas (${team.shared_routines.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 8.dp))
                }
                items(team.shared_routines) { shared ->
                    SharedRoutineCard(
                        shared = shared,
                        onCopy = { viewModel.copyRoutine(team.id, shared.routine_id) }
                    )
                }
            }

            // Feed
            item {
                Text("Actividad del equipo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("Escribe algo al equipo...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        if (postText.isNotBlank()) {
                            IconButton(onClick = {
                                viewModel.createPost(team.id, postText)
                                postText = ""
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
            items(team.posts) { post ->
                TeamPostCard(post = post)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showShareDialog) {
        ShareRoutineDialog(
            routines = routines,
            onDismiss = { showShareDialog = false },
            onShare = { routineId ->
                viewModel.shareRoutine(team.id, routineId)
                showShareDialog = false
            }
        )
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = Color(0xFF1F1F1F),
            title = { Text("Salir del equipo", color = Color.White) },
            text = { Text("¿Seguro que quieres salir de \"${team.name}\"?", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = { showLeaveDialog = false; viewModel.leaveTeam(team.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancelar", color = Color.White.copy(alpha = 0.7f)) }
            }
        )
    }
}

@Composable
private fun SharedRoutineCard(
    shared: com.talos.forge.data.models.SharedRoutine,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282828))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF5C6BC0).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFF7986CB), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(shared.routine?.name ?: "Rutina", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "Compartido por ${shared.user?.username ?: "?"} · ${shared.routine?.exercises?.size ?: 0} ej",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF66BB6A))
            ) {
                Text("Copiar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TeamPostCard(post: com.talos.forge.data.models.TeamPost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFA0F03C).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (post.user?.username ?: "?").take(1).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB8F56A)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(post.user?.username ?: "Usuario", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (post.post_type == "ROUTINE") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFF7986CB), modifier = Modifier.size(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.content, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun CreateTeamDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF282828),
        title = { Text("Crear equipo", color = Color.White) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del equipo") },
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

@Composable
private fun JoinTeamDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF282828),
        title = { Text("Unirse a equipo", color = Color.White) },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase() },
                label = { Text("Código (GYM-XXXXXX)") },
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
            TextButton(onClick = { if (code.isNotBlank()) onJoin(code) }) {
                Text("Unirse", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.White.copy(alpha = 0.7f)) }
        }
    )
}

@Composable
private fun ShareRoutineDialog(
    routines: List<Routine>,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF282828),
        title = { Text("Compartir rutina", color = Color.White) },
        text = {
            if (routines.isEmpty()) {
                Text("No tienes rutinas para compartir", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
            } else {
                Column {
                    routines.forEach { routine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onShare(routine.id) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFF7986CB), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(routine.name, fontSize = 14.sp, color = Color.White, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar", color = Color.White.copy(alpha = 0.7f)) }
        }
    )
}
