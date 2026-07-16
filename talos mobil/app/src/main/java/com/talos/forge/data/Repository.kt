package com.talos.forge.data

import com.talos.forge.data.models.*

class Repository(private val api: ApiService) {

    suspend fun login(email: String, password: String): AuthResponse =
        api.login(LoginRequest(email, password))

    suspend fun register(
        username: String, email: String, password: String,
        phone: String? = null, age: Int? = null,
        heightCm: Float? = null, weightKg: Float? = null,
        goal: String = "MAINTENANCE", gender: String = "M"
    ): AuthResponse = api.register(
        RegisterRequest(username, email, password, phone, age, heightCm, weightKg, goal, gender)
    )

    suspend fun getMe(): User = api.getMe().user

    // Routines
    suspend fun getRoutines(): List<Routine> = api.getRoutines().routines
    suspend fun createRoutine(name: String, dayOfWeek: Int? = null): Routine =
        api.createRoutine(RoutineRequest(name, dayOfWeek)).routine
    suspend fun deleteRoutine(id: String) = api.deleteRoutine(id)

    // Exercises
    suspend fun addExercise(routineId: String, request: ExerciseRequest): Routine =
        api.addExercise(routineId, request).routine
    suspend fun deleteExercise(routineId: String, exerciseId: String) =
        api.deleteExercise(routineId, exerciseId)

    // Meals
    suspend fun getMeals(): List<Meal> = api.getMeals().meals
    suspend fun createMeal(request: MealRequest): Meal = api.createMeal(request).meal
    suspend fun deleteMeal(id: String) = api.deleteMeal(id)
    suspend fun confirmMeal(id: String): Meal = api.confirmMeal(id).meal

    // Macros
    suspend fun getMacros(): MacrosLog? = api.getMacros().logs.firstOrNull()
    suspend fun saveMacros(log: MacrosLog): MacrosLog = api.saveMacros(log).log

    // Supplements
    suspend fun getSupplements(): List<Supplement> = api.getSupplements().supplements
    suspend fun createSupplement(request: SupplementRequest): Supplement =
        api.createSupplement(request).supplement
    suspend fun deleteSupplement(id: String) = api.deleteSupplement(id)

    // Recipes
    suspend fun getRecipes(search: String? = null, mealType: String? = null): List<Recipe> =
        api.getRecipes(search, mealType).recipes
    suspend fun createRecipe(request: RecipeRequest): Recipe = api.createRecipe(request).recipe
    suspend fun getRecipe(id: String): Recipe = api.getRecipe(id).recipe

    // Shopping List
    suspend fun getShoppingList(): ShoppingList? = api.getShoppingList().list
    suspend fun saveShoppingList(items: List<ShoppingListSaveItem>): ShoppingList? =
        api.saveShoppingList(ShoppingListSaveRequest(items)).list
    suspend fun toggleShoppingItem(listId: String, itemId: String, checked: Boolean): ShoppingListItem =
        api.toggleShoppingItem(listId, itemId, ToggleRequest(checked)).item
    suspend fun shareShoppingList(listId: String): ShareResponse =
        api.shareShoppingList(listId)
    suspend fun deleteShoppingList(listId: String) = api.deleteShoppingList(listId)
    suspend fun generateShoppingList(recipeIds: List<String>): ShoppingListAggregationResponse =
        api.generateShoppingList(RecipeIdsRequest(recipeIds))

    // AI
    suspend fun recommendRecipes(): List<Recipe> = api.recommendRecipes().recipes

    // Community
    suspend fun getCommunityFeed(): List<CommunityPost> = api.getCommunityFeed().posts
    suspend fun createCommunityPost(content: String): CommunityPost =
        api.createCommunityPost(CommunityPostRequest(content)).post
    suspend fun replyToPost(postId: String, content: String): CommunityReply =
        api.replyToPost(postId, CommunityPostRequest(content)).reply
}
