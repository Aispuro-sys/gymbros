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
data class AvailabilityResponse(val available: Boolean)

data class ProfileUpdateRequest(
    val username: String? = null,
    val age: Int? = null,
    val height_cm: Float? = null,
    val weight_kg: Float? = null,
    val goal: String? = null,
    val body_type: String? = null,
    val gender: String? = null,
    val bio: String? = null,
    val profile_photo: String? = null,
    val phone: String? = null
)

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
    val order_index: Int = 0,
    val exercise_dataset_id: String? = null,
    val gif_url: String? = null,
    val image: String? = null
)
data class ExerciseRequest(
    val name: String,
    val sets: Int = 3,
    val reps: String = "8-12",
    val rest_seconds: Int = 90,
    val exercise_dataset_id: String? = null
)

// ===== Exercise Dataset (search from backend) =====
data class ExerciseDataset(
    val id: String,
    val name: String,
    val category: String? = null,
    val category_es: String? = null,
    val body_part: String? = null,
    val equipment: String? = null,
    val equipment_es: String? = null,
    val target: String? = null,
    val target_es: String? = null,
    val muscle_group: String? = null,
    val muscle_group_es: String? = null,
    val secondary_muscles: List<String>? = null,
    val secondary_muscles_es: List<String>? = null,
    val image: String? = null,
    val gif_url: String? = null,
    val instructions: Map<String, String>? = null,
    val instruction_steps: Map<String, List<String>>? = null
) {
    val instructionsEs: String? get() = instructions?.get("es")
    val instructionStepsEs: List<String>? get() = instruction_steps?.get("es")
}
data class ExercisesSearchResponse(val exercises: List<ExerciseDataset>, val total: Int)
data class ExerciseDetailResponse(val exercise: ExerciseDataset)
data class CategoriesResponse(val categories: List<CategoryOption>)
data class EquipmentResponse(val equipment: List<CategoryOption>)
data class TargetsResponse(val targets: List<CategoryOption>)
data class CategoryOption(val value: String, val label: String)

// ===== AI Weekly Plan =====
data class WeeklyPlanRequest(
    val days_per_week: Int = 4,
    val equipment: String = "all",
    val notes: String? = null,
    val muscle_groups: List<String> = emptyList()
)
data class WeeklyPlanResponse(
    val plan_name: String? = null,
    val days: List<Routine> = emptyList(),
    val ai_notes: String? = null,
    val ai_powered: Boolean = false
)
data class AIRoutineResponse(
    val routine: Routine,
    val ai_notes: String? = null,
    val ai_powered: Boolean = false
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
    val replies: List<CommunityReply> = emptyList(),
    val reactions: List<CommunityReaction> = emptyList()
)
data class CommunityUser(val id: String, val username: String, val profile_photo: String? = null, val role: String? = null, val bio: String? = null)
data class CommunityReaction(val id: String, val emoji: String, val user_id: String)
data class CommunityReply(
    val id: String,
    val post_id: String,
    val user_id: String,
    val content: String,
    val media_url: String? = null,
    val created_at: String,
    val user: CommunityUser? = null
)
data class CommunityPostRequest(val content: String, val media_url: String? = null, val media_type: String = "TEXT")
data class CommunityPostsResponse(val posts: List<CommunityPost>)
data class CommunityPostResponse(val post: CommunityPost)
data class CommunityReplyResponse(val reply: CommunityReply)
data class CommunityReactRequest(val emoji: String)
data class CommunitySearchResponse(val users: List<CommunityUser>)
data class CommunityUserProfile(
    val id: String,
    val username: String,
    val profile_photo: String? = null,
    val role: String? = null,
    val bio: String? = null,
    val age: Int? = null,
    val height_cm: Int? = null,
    val weight_kg: Int? = null,
    val goal: String? = null,
    val body_type: String? = null,
    val gender: String? = null,
    val created_at: String? = null,
    val post_count: Int = 0,
    val routine_count: Int = 0
)
data class CommunityUserProfileResponse(
    val user: CommunityUserProfile,
    val posts: List<CommunityPost> = emptyList(),
    val routines: List<CommunityProfileRoutine> = emptyList()
)
data class CommunityProfileRoutine(
    val id: String,
    val name: String,
    val day_of_week: Int? = null,
    val ai_generated: Boolean = false,
    val _count: CommunityProfileRoutineCount? = null
)
data class CommunityProfileRoutineCount(val exercises: Int = 0)

// ===== AI =====
data class AIRecipeRecommendation(val recipes: List<Recipe>)

data class AnalyzeFoodRequest(val image: String)
data class FoodAnalysisResponse(
    val analysis: FoodAnalysis,
    val ai_powered: Boolean = false
)
data class FoodAnalysis(
    @com.google.gson.annotations.SerializedName("food_name") val name: String = "",
    val estimated_portion: String? = null,
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0,
    val fiber_g: Double? = null,
    val sugar_g: Double? = null,
    val sodium_mg: Double? = null,
    val confidence: String = "",
    val notes: String? = null
)

data class AnalyzeSupplementRequest(val image: String)
data class SupplementAnalysisResponse(
    val analysis: SupplementAnalysis,
    val ai_powered: Boolean = false
)
data class SupplementAnalysis(
    val name: String = "",
    val brand: String? = null,
    val serving_size: String? = null,
    val servings_per_container: Int? = null,
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0,
    val key_ingredients: List<String> = emptyList(),
    val dose_per_serving: String? = null,
    val category: String = "Otro",
    val usage_instructions: String? = null,
    val warnings: String? = null,
    val confidence: String = "",
    val notes: String? = null
)

data class NutritionPlanResponse(
    val plan: NutritionPlan,
    val ai_powered: Boolean = false
)
data class NutritionPlan(
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0,
    val meals: List<NutritionPlanMeal> = emptyList(),
    val notes: String? = null
)
data class NutritionPlanMeal(
    val meal: String = "",
    val meal_type: String = "SNACK",
    val numeric: NutritionPlanMealNum? = null
)
data class NutritionPlanMealNum(
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0
)

data class WeeklyNutritionSummary(
    val totalMeals: Int = 0,
    val avgCalories: Int = 0,
    val avgProtein: Int = 0,
    val avgCarbs: Int = 0,
    val avgFats: Int = 0,
    val totalUnconfirmed: Int = 0,
    val days: List<WeeklyNutritionDay> = emptyList()
)
data class WeeklyNutritionDay(
    val date: String = "",
    val calories: Int = 0,
    val protein_g: Int = 0,
    val carbs_g: Int = 0,
    val fats_g: Int = 0,
    val meals_total: Int = 0,
    val meals_unconfirmed: Int = 0
)

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
data class AddItemRequest(val name: String, val quantity: String? = null)
data class RecipeIdsRequest(val recipeIds: List<String>)

// ===== Teams =====
data class TeamMember(
    val id: String,
    val user_id: String,
    val role: String,
    val user: TeamUser? = null
)
data class TeamUser(
    val id: String,
    val username: String,
    val goal: String? = null,
    val body_type: String? = null
)
data class Team(
    val id: String,
    val name: String,
    val admin_id: String,
    val invite_code: String,
    val role: String? = null,
    val members: List<TeamMember> = emptyList(),
    val shared_routines: List<SharedRoutine> = emptyList(),
    val posts: List<TeamPost> = emptyList()
)
data class SharedRoutine(
    val id: String,
    val team_id: String,
    val routine_id: String,
    val shared_by: String,
    val shared_at: String,
    val routine: Routine? = null,
    val user: TeamUser? = null
)
data class TeamPost(
    val id: String,
    val team_id: String,
    val user_id: String,
    val content: String,
    val post_type: String? = null,
    val routine_id: String? = null,
    val created_at: String,
    val user: TeamUser? = null
)
data class TeamsResponse(val teams: List<Team>)
data class TeamResponse(val team: Team, val role: String? = null)
data class CreateTeamRequest(val name: String)
data class JoinTeamRequest(val invite_code: String)
data class ShareRoutineRequest(val routine_id: String)
data class TeamPostRequest(val content: String)
data class TeamPostResponse(val post: TeamPost)

// ===== Progress Photos / Body Logs =====
data class ProgressPhoto(
    val id: String,
    val photo_url: String? = null,
    val weight_logged: Float? = null,
    val waist_cm: Float? = null,
    val chest_cm: Float? = null,
    val hip_cm: Float? = null,
    val arm_cm: Float? = null,
    val leg_cm: Float? = null,
    val body_fat_pct: Float? = null,
    val note: String? = null,
    val date: String,
    val created_at: String
)
data class ProgressPhotosResponse(val photos: List<ProgressPhoto> = emptyList())
data class UploadProgressPhotoRequest(
    val photo_url: String? = null,
    val weight_logged: Float? = null,
    val waist_cm: Float? = null,
    val chest_cm: Float? = null,
    val hip_cm: Float? = null,
    val arm_cm: Float? = null,
    val leg_cm: Float? = null,
    val body_fat_pct: Float? = null,
    val note: String? = null
)
data class ProgressPhotoResponse(val photo: ProgressPhoto)
data class UpdateProgressPhotoRequest(
    val weight_logged: Float? = null,
    val waist_cm: Float? = null,
    val chest_cm: Float? = null,
    val hip_cm: Float? = null,
    val arm_cm: Float? = null,
    val leg_cm: Float? = null,
    val body_fat_pct: Float? = null,
    val note: String? = null
)

// ===== Tracking Stats =====
data class TrackingStats(
    val streak: Int = 0,
    val longest_streak: Int = 0,
    val workouts_this_week: Int = 0,
    val total_workout_days: Int = 0,
    val weekly_counts: List<WeeklyCount> = emptyList(),
    val meals_this_week: Int = 0,
    val total_meals: Int = 0,
    val rest_days: Int = 0
)
data class WeeklyCount(
    val week_start: String = "",
    val count: Int = 0
)
data class TrackingStatsResponse(
    val streak: Int = 0,
    val longest_streak: Int = 0,
    val workouts_this_week: Int = 0,
    val total_workout_days: Int = 0,
    val weekly_counts: List<WeeklyCount> = emptyList(),
    val meals_this_week: Int = 0,
    val total_meals: Int = 0,
    val rest_days: Int = 0
)

// ===== Workout Logs =====
data class WorkoutLog(
    val id: String,
    val user_id: String = "",
    val date: String,
    val type: String = "WORKOUT",
    val duration_min: Int? = null,
    val intensity: String? = null,
    val notes: String? = null,
    val created_at: String = ""
)
data class WorkoutLogsResponse(val logs: List<WorkoutLog> = emptyList())
data class WorkoutLogResponse(val log: WorkoutLog)
data class WorkoutLogRequest(
    val type: String,
    val duration_min: Int? = null,
    val intensity: String? = null,
    val notes: String? = null,
    val date: String? = null
)
