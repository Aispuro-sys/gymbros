package com.talos.forge.data.models

// ===== Auth =====
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val age: Int? = null,
    val height_cm: Float? = null,
    val weight_kg: Float? = null,
    val goal: String = "MAINTENANCE",
    val gender: String = "M"
)
data class AuthResponse(val token: String, val user: User)
data class UserResponse(val user: User)

// ===== User =====
data class User(
    val id: String,
    val username: String,
    val email: String,
    val phone: String? = null,
    val age: Int? = null,
    val height_cm: Float? = null,
    val weight_kg: Float? = null,
    val goal: String? = null,
    val body_type: String? = null,
    val gender: String? = "M",
    val role: String = "NORMAL",
    val bio: String? = null,
    val profile_photo: String? = null
)

// ===== Routine =====
data class Routine(
    val id: String,
    val user_id: String,
    val name: String,
    val ai_generated: Boolean = false,
    val ai_prompt: String? = null,
    val day_of_week: Int? = null,
    val exercises: List<Exercise> = emptyList()
)
data class RoutineRequest(val name: String, val day_of_week: Int? = null)
data class RoutinesResponse(val routines: List<Routine>)
data class RoutineResponse(val routine: Routine)

// ===== Exercise =====
data class Exercise(
    val id: String,
    val routine_id: String,
    val name: String,
    val sets: Int = 3,
    val reps: String = "8-12",
    val rest_seconds: Int = 90,
    val order_index: Int = 0
)
data class ExerciseRequest(
    val name: String,
    val sets: Int = 3,
    val reps: String = "8-12",
    val rest_seconds: Int = 90
)

// ===== Nutrition =====
data class Meal(
    val id: String,
    val user_id: String,
    val name: String,
    val meal_type: String = "SNACK",
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0,
    val photo_url: String? = null,
    val confirmed: Boolean = false,
    val date: String? = null
)
data class MealRequest(
    val name: String,
    val meal_type: String = "SNACK",
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0
)
data class MealsResponse(val meals: List<Meal>, val totals: MealTotals? = null)
data class MealTotals(val calories: Int = 0, val protein_g: Int = 0, val carbs_g: Int = 0, val fats_g: Int = 0)
data class MealResponse(val meal: Meal)

// ===== Macros =====
data class MacrosLog(
    val id: String? = null,
    val date: String? = null,
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0
)
data class MacrosLogsResponse(val logs: List<MacrosLog>)
data class MacrosLogResponse(val log: MacrosLog)

// ===== Supplements =====
data class Supplement(
    val id: String,
    val name: String,
    val dosage: String,
    val time_of_day: String = "MORNING",
    val is_medication: Boolean = false
)
data class SupplementRequest(
    val name: String,
    val dosage: String,
    val time_of_day: String = "MORNING",
    val is_medication: Boolean = false
)
data class SupplementsResponse(val supplements: List<Supplement>)
data class SupplementResponse(val supplement: Supplement)

// ===== Recipes =====
data class Recipe(
    val id: String,
    val name: String,
    val description: String? = null,
    val image_url: String? = null,
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0,
    val prep_time_min: Int = 0,
    val servings: Int = 1,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val meal_type: String = "ANY",
    val diet_tags: List<String> = emptyList()
)
data class RecipeRequest(
    val name: String,
    val description: String? = null,
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0,
    val prep_time_min: Int = 0,
    val servings: Int = 1,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val meal_type: String = "ANY"
)
data class RecipesResponse(val recipes: List<Recipe>)
data class RecipeResponse(val recipe: Recipe)

// ===== Shopping List =====
data class ShoppingList(
    val id: String,
    val name: String = "Lista de Supermercado",
    val items: List<ShoppingListItem> = emptyList(),
    val share_token: String? = null
)
data class ShoppingListItem(
    val id: String,
    val name: String,
    val quantity: String? = null,
    val checked: Boolean = false,
    val recipe_names: List<String> = emptyList()
)
data class ShoppingListSaveRequest(
    val items: List<ShoppingListSaveItem>,
    val name: String = "Lista de Supermercado"
)
data class ShoppingListSaveItem(
    val name: String,
    val quantity: String? = null,
    val checked: Boolean = false,
    val recipe_names: List<String> = emptyList()
)
data class ShareResponse(val shareToken: String, val shareUrl: String)
data class ShoppingItemResponse(val item: ShoppingListItem)

// ===== Community =====
data class CommunityPost(
    val id: String,
    val user_id: String,
    val content: String,
    val media_url: String? = null,
    val media_type: String = "TEXT",
    val created_at: String,
    val user: CommunityUser? = null,
    val replies: List<CommunityReply> = emptyList()
)
data class CommunityUser(val id: String, val username: String, val profile_photo: String? = null, val role: String? = null)
data class CommunityReply(
    val id: String,
    val post_id: String,
    val user_id: String,
    val content: String,
    val created_at: String,
    val user: CommunityUser? = null
)
data class CommunityPostRequest(val content: String)
data class CommunityPostsResponse(val posts: List<CommunityPost>)
data class CommunityPostResponse(val post: CommunityPost)
data class CommunityReplyResponse(val reply: CommunityReply)

// ===== AI =====
data class AIRecipeRecommendation(val recipes: List<Recipe>)

// ===== Generic =====
data class MessageResponse(val message: String)
data class ShoppingListFromMealsResponse(
    val ingredients: List<ShoppingIngredient>,
    val recipes: List<Recipe> = emptyList(),
    val meals: List<Meal> = emptyList()
)
data class ShoppingIngredient(
    val name: String,
    val count: Int = 1,
    val recipes: List<String> = emptyList(),
    val quantity: String = ""
)
data class ShoppingListAggregationResponse(
    val ingredients: List<ShoppingIngredient>,
    val recipes: List<Recipe> = emptyList(),
    val recipeCount: Int = 0,
    val recipeNames: List<String> = emptyList()
)

// ===== Request bodies =====
data class ToggleRequest(val checked: Boolean)
data class RecipeIdsRequest(val recipeIds: List<String>)
