package com.talos.forge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.talos.forge.data.models.Recipe
import com.talos.forge.ui.RecipesViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.theme.AppColors

@Composable
fun RecipesScreen(viewModel: RecipesViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedRecipe by viewModel.selectedRecipe.collectAsState()
    val mealTypeFilter by viewModel.mealTypeFilter.collectAsState()
    val shoppingMessage by viewModel.shoppingListMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadRecipes() }

    LaunchedEffect(shoppingMessage) {
        if (shoppingMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearShoppingMessage()
        }
    }

    selectedRecipe?.let { recipe ->
        RecipeDetailDialog(
            recipe = recipe,
            onAddToShopping = {
                viewModel.addToShoppingList(recipe.id)
            },
            onDismiss = { viewModel.clearSelectedRecipe() }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearch(it) },
            label = { Text("Buscar receta...", color = AppColors.textSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.textPrimary,
                unfocusedTextColor = AppColors.textPrimary,
                focusedBorderColor = AppColors.accent,
                unfocusedBorderColor = AppColors.border,
                cursorColor = AppColors.accent,
                focusedContainerColor = AppColors.cardBg,
                unfocusedContainerColor = AppColors.cardBg
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Meal type filter chips
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(listOf("ANY" to "Todas", "BREAKFAST" to "Desayuno", "LUNCH" to "Comida", "DINNER" to "Cena", "SNACK" to "Snack")) { (value, label) ->
                FilterChip(
                    selected = mealTypeFilter == value,
                    onClick = { viewModel.updateMealTypeFilter(value) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { viewModel.recommendWithAI() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.accent)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AppColors.accent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Recomendar con IA")
        }

        Spacer(modifier = Modifier.height(8.dp))

        shoppingMessage?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.success.copy(alpha = 0.15f))
            ) {
                Text(it, modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = AppColors.success)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        error?.let { ErrorText(it) }

        if (isLoading && recipes.isEmpty()) {
            LoadingSpinner()
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(recipes) { recipe ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectRecipe(recipe) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        recipe.image_url?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = recipe.name,
                                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            recipe.description?.let {
                                Text(it, fontSize = 12.sp, color = AppColors.textSecondary, maxLines = 2)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${recipe.calories} cal · ${recipe.protein_g}g prot · ${recipe.prep_time_min} min",
                                fontSize = 12.sp,
                                color = AppColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Toca para ver preparación", fontSize = 10.sp, color = AppColors.accent)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailDialog(
    recipe: Recipe,
    onAddToShopping: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.textSecondary.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(recipe.name, color = AppColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = AppColors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            recipe.image_url?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = recipe.name,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Macros row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroChip("${recipe.calories}", "Cal")
                MacroChip("${recipe.protein_g}g", "Prot")
                MacroChip("${recipe.carbs_g}g", "Carbos")
                MacroChip("${recipe.fats_g}g", "Grasas")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("⏱ ${recipe.prep_time_min} min", fontSize = 13.sp, color = AppColors.textSecondary)
                Text("🍽 ${recipe.servings} porciones", fontSize = 13.sp, color = AppColors.textSecondary)
            }

            if (recipe.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Ingredientes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                recipe.ingredients.forEach { ingredient ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("•", color = AppColors.accent, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(ingredient, fontSize = 14.sp, color = AppColors.textPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Preparación", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            if (recipe.instructions.isNotEmpty()) {
                recipe.instructions.forEachIndexed { idx, step ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Box(
                            modifier = Modifier.size(26.dp).clip(RoundedCornerShape(8.dp))
                                .background(AppColors.accent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.accentLight)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(step, fontSize = 14.sp, color = AppColors.textPrimary, modifier = Modifier.weight(1f))
                    }
                }
            } else {
                Text("No hay instrucciones de preparación disponibles para esta receta.", fontSize = 13.sp, color = AppColors.textSecondary)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddToShopping,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar a lista de super", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MacroChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Text(label, fontSize = 10.sp, color = AppColors.textSecondary)
    }
}
