package com.talos.forge.data

import com.talos.forge.data.models.*
import retrofit2.http.*

interface ApiService {

    // ===== Auth =====
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("auth/check-username")
    suspend fun checkUsername(@Query("username") username: String): AvailabilityResponse

    @GET("auth/check-email")
    suspend fun checkEmail(@Query("email") email: String): AvailabilityResponse

    @GET("auth/me")
    suspend fun getMe(): UserResponse

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): UserResponse

    // ===== Routines =====
    @GET("routines")
    suspend fun getRoutines(): RoutinesResponse

    @POST("routines")
    suspend fun createRoutine(@Body request: RoutineRequest): RoutineResponse

    @DELETE("routines/{id}")
    suspend fun deleteRoutine(@Path("id") id: String): MessageResponse

    // ===== Exercises =====
    @POST("routines/{routineId}/exercises")
    suspend fun addExercise(
        @Path("routineId") routineId: String,
        @Body request: ExerciseRequest
    ): RoutineResponse

    @DELETE("routines/{routineId}/exercises/{exerciseId}")
    suspend fun deleteExercise(
        @Path("routineId") routineId: String,
        @Path("exerciseId") exerciseId: String
    ): MessageResponse

    // ===== Exercise Dataset =====
    @GET("exercises")
    suspend fun searchExercises(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null,
        @Query("equipment") equipment: String? = null,
        @Query("target") target: String? = null,
        @Query("limit") limit: Int? = null
    ): ExercisesSearchResponse

    @GET("exercises/{id}")
    suspend fun getExercise(@Path("id") id: String): ExerciseDetailResponse

    @GET("exercises/categories")
    suspend fun getCategories(): CategoriesResponse

    @GET("exercises/equipment")
    suspend fun getEquipmentTypes(): EquipmentResponse

    @GET("exercises/targets")
    suspend fun getTargets(): TargetsResponse

    // ===== AI Routines =====
    @POST("ai/generate-weekly")
    suspend fun generateWeeklyPlan(@Body request: WeeklyPlanRequest): WeeklyPlanResponse

    @POST("ai/generate-routine")
    suspend fun generateRoutine(@Body request: Map<String, Any> = emptyMap()): AIRoutineResponse

    @GET("ai/routines-with-gifs")
    suspend fun getRoutinesWithGifs(): RoutinesResponse

    // ===== Nutrition / Meals =====
    @GET("meals")
    suspend fun getMeals(): MealsResponse

    @POST("meals")
    suspend fun createMeal(@Body request: MealRequest): MealResponse

    @DELETE("meals/{id}")
    suspend fun deleteMeal(@Path("id") id: String): MessageResponse

    @PUT("meals/{id}/confirm")
    suspend fun confirmMeal(@Path("id") id: String, @Body body: Map<String, String> = emptyMap()): MealResponse

    // ===== Macros =====
    @GET("macros")
    suspend fun getMacros(): MacrosLogsResponse

    @POST("macros")
    suspend fun saveMacros(@Body request: MacrosLog): MacrosLogResponse

    @GET("macros/weekly-summary")
    suspend fun getWeeklyNutritionSummary(): WeeklyNutritionSummary

    // ===== Supplements =====
    @GET("supplements")
    suspend fun getSupplements(): SupplementsResponse

    @POST("supplements")
    suspend fun createSupplement(@Body request: SupplementRequest): SupplementResponse

    @DELETE("supplements/{id}")
    suspend fun deleteSupplement(@Path("id") id: String): MessageResponse

    // ===== Recipes =====
    @GET("recipes")
    suspend fun getRecipes(
        @Query("search") search: String? = null,
        @Query("meal_type") mealType: String? = null
    ): RecipesResponse

    @POST("recipes")
    suspend fun createRecipe(@Body request: RecipeRequest): RecipeResponse

    @GET("recipes/{id}")
    suspend fun getRecipe(@Path("id") id: String): RecipeResponse

    // ===== Shopping List =====
    @GET("shopping-list")
    suspend fun getShoppingList(): ShoppingListResponse

    @POST("shopping-list")
    suspend fun saveShoppingList(@Body request: ShoppingListSaveRequest): ShoppingListResponse

    @PUT("shopping-list/{listId}/items/{itemId}")
    suspend fun toggleShoppingItem(
        @Path("listId") listId: String,
        @Path("itemId") itemId: String,
        @Body request: ToggleRequest
    ): ShoppingItemResponse

    @POST("shopping-list/{listId}/items")
    suspend fun addShoppingItem(
        @Path("listId") listId: String,
        @Body request: AddItemRequest
    ): ShoppingListResponse

    @DELETE("shopping-list/{listId}/items/{itemId}")
    suspend fun deleteShoppingItem(
        @Path("listId") listId: String,
        @Path("itemId") itemId: String
    ): ShoppingListResponse

    @POST("shopping-list/{listId}/share")
    suspend fun shareShoppingList(@Path("listId") listId: String): ShareResponse

    @DELETE("shopping-list/{listId}")
    suspend fun deleteShoppingList(@Path("listId") listId: String): MessageResponse

    @POST("recipes/shopping-list")
    suspend fun generateShoppingList(@Body request: RecipeIdsRequest): ShoppingListAggregationResponse

    // ===== AI =====
    @POST("ai/recommend-recipes")
    suspend fun recommendRecipes(@Body request: Map<String, Any> = emptyMap()): AIRecipeRecommendation

    @POST("ai/nutrition-plan")
    suspend fun generateNutritionPlan(@Body request: Map<String, Any> = emptyMap()): NutritionPlanResponse

    @POST("ai/analyze-food")
    suspend fun analyzeFoodPhoto(@Body request: AnalyzeFoodRequest): FoodAnalysisResponse

    @POST("ai/analyze-supplement")
    suspend fun analyzeSupplementPhoto(@Body request: AnalyzeSupplementRequest): SupplementAnalysisResponse

    // ===== Community =====
    @GET("community/feed")
    suspend fun getCommunityFeed(): CommunityPostsResponse

    @POST("community/posts")
    suspend fun createCommunityPost(@Body request: CommunityPostRequest): CommunityPostResponse

    @POST("community/posts/{postId}/replies")
    suspend fun replyToPost(
        @Path("postId") postId: String,
        @Body request: CommunityPostRequest
    ): CommunityReplyResponse

    @POST("community/posts/{postId}/react")
    suspend fun reactToPost(
        @Path("postId") postId: String,
        @Body request: CommunityReactRequest
    ): MessageResponse

    @DELETE("community/posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: String): MessageResponse

    @GET("community/users/search")
    suspend fun searchUsers(@Query("q") query: String): CommunitySearchResponse

    @GET("community/profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): CommunityUserProfileResponse

    // ===== Teams =====
    @GET("teams")
    suspend fun getTeams(): TeamsResponse

    @POST("teams")
    suspend fun createTeam(@Body request: CreateTeamRequest): TeamResponse

    @POST("teams/join")
    suspend fun joinTeam(@Body request: JoinTeamRequest): TeamResponse

    @GET("teams/{id}")
    suspend fun getTeamDetail(@Path("id") id: String): TeamResponse

    @POST("teams/{id}/share-routine")
    suspend fun shareRoutine(
        @Path("id") teamId: String,
        @Body request: ShareRoutineRequest
    ): MessageResponse

    @POST("teams/{id}/copy-routine/{routineId}")
    suspend fun copyRoutine(
        @Path("id") teamId: String,
        @Path("routineId") routineId: String
    ): RoutineResponse

    @POST("teams/{id}/posts")
    suspend fun createTeamPost(
        @Path("id") teamId: String,
        @Body request: TeamPostRequest
    ): TeamPostResponse

    @DELETE("teams/{id}/leave")
    suspend fun leaveTeam(@Path("id") teamId: String): MessageResponse

    // ===== Progress Photos / Body Logs =====
    @GET("progress")
    suspend fun getProgressPhotos(): ProgressPhotosResponse

    @GET("progress/stats")
    suspend fun getTrackingStats(): TrackingStatsResponse

    @POST("progress")
    suspend fun uploadProgressPhoto(@Body request: UploadProgressPhotoRequest): ProgressPhotoResponse

    @DELETE("progress/{id}")
    suspend fun deleteProgressPhoto(@Path("id") id: String): MessageResponse

    @PUT("progress/{id}")
    suspend fun updateProgressPhoto(@Path("id") id: String, @Body request: UpdateProgressPhotoRequest): ProgressPhotoResponse

    // ===== Workout Logs =====
    @GET("progress/workouts")
    suspend fun getWorkoutLogs(): WorkoutLogsResponse

    @POST("progress/workouts")
    suspend fun createWorkoutLog(@Body request: WorkoutLogRequest): WorkoutLogResponse

    @DELETE("progress/workouts/{id}")
    suspend fun deleteWorkoutLog(@Path("id") id: String): MessageResponse
}

data class ShoppingListResponse(val list: ShoppingList?)
