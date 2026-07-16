package com.talos.forge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.ui.ShoppingViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.components.EmptyState

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val shoppingList by viewModel.shoppingList.collectAsState()
    val error by viewModel.error.collectAsState()
    val shareUrl by viewModel.shareUrl.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadShoppingList() }

    val items = shoppingList?.items ?: emptyList()
    val checkedCount = items.count { it.checked }
    val totalCount = items.size
    val progress = if (totalCount > 0) (checkedCount * 100 / totalCount) else 0

    Scaffold(
        floatingActionButton = {
            if (shoppingList != null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.shareList(); showShareDialog = true },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Share") },
                    text = { Text("Compartir") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Lista de Supermercado", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            error?.let { ErrorText(it) }

            if (isLoading && shoppingList == null) {
                LoadingSpinner()
                return@Column
            }

            if (shoppingList == null || items.isEmpty()) {
                EmptyState(
                    icon = "🛒",
                    title = "Lista vacía",
                    subtitle = "Genera una lista desde tus comidas o con IA"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.generateFromMeals() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("🍽️ Comidas") }
                    OutlinedButton(
                        onClick = { viewModel.generateFromAI() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("🤖 IA") }
                }
                return@Column
            }

            // Progress card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(shoppingList!!.name, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                        Text("$checkedCount/$totalCount", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress / 100f,
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = androidx.compose.ui.graphics.Color.White,
                        trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { viewModel.generateFromMeals() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("🍽️ Comidas") }
                OutlinedButton(onClick = { viewModel.generateFromAI() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("🤖 IA") }
                OutlinedButton(onClick = { viewModel.clearList() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("🗑️") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items list
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(items) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { viewModel.toggleItem(item.id, it) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name,
                                fontSize = 15.sp,
                                textDecoration = if (item.checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                            if (item.recipe_names.size > 1) {
                                Text("En ${item.recipe_names.size} recetas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        item.quantity?.let {
                            Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.checked) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✅")
                        }
                    }
                }
            }
        }
    }

    if (showShareDialog && shareUrl != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false; viewModel.clearShareUrl() },
            title = { Text("🔗 Compartir Lista") },
            text = {
                Column {
                    Text("Comparte este enlace:")
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(shareUrl!!, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                val context = androidx.compose.ui.platform.LocalContext.current
                TextButton(onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Share URL", shareUrl!!))
                    showShareDialog = false
                    viewModel.clearShareUrl()
                }) { Text("Copiar") }
            },
            dismissButton = { TextButton(onClick = { showShareDialog = false; viewModel.clearShareUrl() }) { Text("Cerrar") } }
        )
    }
}
