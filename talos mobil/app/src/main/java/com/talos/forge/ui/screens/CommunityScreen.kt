package com.talos.forge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.ui.CommunityViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner

@Composable
fun CommunityScreen(viewModel: CommunityViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newPostContent by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadFeed() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Post")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Comunidad", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            error?.let { ErrorText(it) }

            if (isLoading && posts.isEmpty()) {
                LoadingSpinner()
                return@Column
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(posts) { post ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(post.username ?: "Usuario", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(post.content, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(post.created_at, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (post.replies.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                post.replies.forEach { reply ->
                                    Text("  ↳ ${reply.username}: ${reply.content}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Publicación") },
            text = {
                OutlinedTextField(
                    value = newPostContent,
                    onValueChange = { newPostContent = it },
                    label = { Text("¿Qué quieres compartir?") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    maxLines = 5
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPostContent.isNotBlank()) {
                        viewModel.createPost(newPostContent)
                        newPostContent = ""
                        showAddDialog = false
                    }
                }) { Text("Publicar") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }
}
