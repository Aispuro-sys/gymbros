package com.talos.forge.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.ui.ShoppingViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.components.EmptyState
import com.talos.forge.ui.theme.AppColors

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val shoppingList by viewModel.shoppingList.collectAsState()
    val error by viewModel.error.collectAsState()
    val shareUrl by viewModel.shareUrl.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("") }

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
                    text = { Text("Compartir") },
                    containerColor = AppColors.accent,
                    contentColor = AppColors.textOnAccent
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            error?.let { ErrorText(it) }

            if (isLoading && shoppingList == null) {
                LoadingSpinner()
                return@Column
            }

            // Add item bar - always visible
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Agregar producto...", color = AppColors.textSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.accent,
                            focusedBorderColor = AppColors.accent,
                            unfocusedBorderColor = AppColors.border,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(
                        value = newItemQty,
                        onValueChange = { newItemQty = it },
                        label = { Text("Cant", color = AppColors.textSecondary) },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            cursorColor = AppColors.accent,
                            focusedBorderColor = AppColors.accent,
                            unfocusedBorderColor = AppColors.border,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            if (newItemName.isNotBlank()) {
                                viewModel.addItem(newItemName.trim(), newItemQty.ifBlank { null })
                                newItemName = ""
                                newItemQty = ""
                            }
                        },
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.accent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = AppColors.textOnAccent, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (shoppingList == null || items.isEmpty()) {
                EmptyState(
                    icon = "🛒",
                    title = "Lista vacía",
                    subtitle = "Agrega productos arriba o genera con IA"
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.generateFromAI() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.accent)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar con IA")
                }
                return@Column
            }

            // Progress card
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
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(shoppingList!!.name, color = AppColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("$checkedCount/$totalCount", color = AppColors.textSecondary, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = AppColors.accent,
                            trackColor = AppColors.cardBgAlt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.generateFromAI() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.accent)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("IA")
                }
                OutlinedButton(
                    onClick = { viewModel.clearList() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.danger.copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = AppColors.danger.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items list
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.checked,
                                onCheckedChange = { viewModel.toggleItem(item.id, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AppColors.accent,
                                    uncheckedColor = AppColors.textSecondary,
                                    checkmarkColor = AppColors.textOnAccent
                                )
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.name,
                                    fontSize = 15.sp,
                                    color = if (item.checked) AppColors.textSecondary else AppColors.textPrimary,
                                    textDecoration = if (item.checked) TextDecoration.LineThrough else null
                                )
                                if (item.recipe_names.size > 1) {
                                    Text("En ${item.recipe_names.size} recetas", fontSize = 11.sp, color = AppColors.textSecondary)
                                }
                            }
                            item.quantity?.let {
                                Text(it, fontSize = 12.sp, color = AppColors.textSecondary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { viewModel.deleteItem(item.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = AppColors.textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareDialog && shareUrl != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false; viewModel.clearShareUrl() },
            containerColor = AppColors.cardBg,
            title = { Text("🔗 Compartir Lista", color = AppColors.textPrimary) },
            text = {
                Column {
                    Text("Comparte este enlace:", color = AppColors.textSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(shareUrl!!, fontSize = 13.sp, color = AppColors.textPrimary)
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
                }) { Text("Copiar", color = AppColors.accent) }
            },
            dismissButton = { TextButton(onClick = { showShareDialog = false; viewModel.clearShareUrl() }) { Text("Cerrar", color = AppColors.textSecondary) } }
        )
    }
}
