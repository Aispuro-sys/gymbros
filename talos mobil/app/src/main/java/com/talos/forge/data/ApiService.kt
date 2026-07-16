package com.talos.forge.data

import com.talos.forge.data.models.*
import retrofit2.http.*

interface ApiService {

    // ===== Auth =====
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("auth/me")
    suspend fun getMe(): UserResponse

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

    @POST("shopping-list/{listId}/share")
    suspend fun shareShoppingList(@Path("listId") listId: String): ShareResponse

    @DELETE("shopping-list/{listId}")
    suspend fun deleteShoppingList(@Path("listId") listId: String): MessageResponse

    @POST("recipes/shopping-list")
    suspend fun generateShoppingList(@Body request: RecipeIdsRequest): ShoppingListAggregationResponse

    // ===== AI =====
    @POST("ai/recommend-recipes")
    suspend fun recommendRecipes(@Body request: Map<String, Any> = emptyMap()): AIRecipeRecommendation

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
}

data class ShoppingListResponse(val list: ShoppingList?)
