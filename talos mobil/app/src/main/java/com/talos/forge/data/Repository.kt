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

    suspend fun updateProfile(request: ProfileUpdateRequest): User = api.updateProfile(request).user

    suspend fun checkUsername(username: String): Boolean = api.checkUsername(username).available

    suspend fun checkEmail(email: String): Boolean = api.checkEmail(email).available

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

    // Exercise Dataset
    suspend fun searchExercises(
        query: String? = null, category: String? = null,
        equipment: String? = null, target: String? = null, limit: Int? = null
    ): List<ExerciseDataset> = api.searchExercises(query, category, equipment, target, limit).exercises

    suspend fun getExercise(id: String): ExerciseDataset = api.getExercise(id).exercise

    suspend fun getCategories(): List<CategoryOption> = api.getCategories().categories
    suspend fun getEquipmentTypes(): List<CategoryOption> = api.getEquipmentTypes().equipment
    suspend fun getTargets(): List<CategoryOption> = api.getTargets().targets

    // AI Routines
    suspend fun generateWeeklyPlan(daysPerWeek: Int = 4, equipment: String = "all", notes: String? = null, muscleGroups: List<String> = emptyList()): WeeklyPlanResponse =
        api.generateWeeklyPlan(WeeklyPlanRequest(daysPerWeek, equipment, notes, muscleGroups))

    suspend fun generateRoutine(focus: String, equipment: String = "all", notes: String? = null): AIRoutineResponse =
        api.generateRoutine(mapOf("focus" to focus, "equipment" to equipment, "notes" to (notes ?: "")))

    suspend fun getRoutinesWithGifs(): List<Routine> = api.getRoutinesWithGifs().routines

    // Meals
    suspend fun getMeals(): List<Meal> = api.getMeals().meals
    suspend fun createMeal(request: MealRequest): Meal = api.createMeal(request).meal
    suspend fun deleteMeal(id: String) = api.deleteMeal(id)
    suspend fun confirmMeal(id: String): Meal = api.confirmMeal(id).meal

    // Macros
    suspend fun getMacros(): MacrosLog? = api.getMacros().logs.firstOrNull()
    suspend fun saveMacros(log: MacrosLog): MacrosLog = api.saveMacros(log).log
    suspend fun getWeeklyNutritionSummary(): WeeklyNutritionSummary = api.getWeeklyNutritionSummary()

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
    suspend fun addShoppingItem(listId: String, name: String, quantity: String?): ShoppingList? =
        api.addShoppingItem(listId, AddItemRequest(name, quantity)).list
    suspend fun deleteShoppingItem(listId: String, itemId: String): ShoppingList? =
        api.deleteShoppingItem(listId, itemId).list
    suspend fun shareShoppingList(listId: String): ShareResponse =
        api.shareShoppingList(listId)
    suspend fun deleteShoppingList(listId: String) = api.deleteShoppingList(listId)
    suspend fun generateShoppingList(recipeIds: List<String>): ShoppingListAggregationResponse =
        api.generateShoppingList(RecipeIdsRequest(recipeIds))

    // AI
    suspend fun recommendRecipes(): List<Recipe> = api.recommendRecipes().recipes
    suspend fun generateNutritionPlan(): NutritionPlan = api.generateNutritionPlan().plan
    suspend fun analyzeFoodPhoto(base64Image: String): FoodAnalysis = api.analyzeFoodPhoto(AnalyzeFoodRequest(base64Image)).analysis
    suspend fun analyzeSupplementPhoto(base64Image: String): SupplementAnalysis = api.analyzeSupplementPhoto(AnalyzeSupplementRequest(base64Image)).analysis

    // Community
    suspend fun getCommunityFeed(): List<CommunityPost> = api.getCommunityFeed().posts
    suspend fun createCommunityPost(content: String, mediaUrl: String? = null, mediaType: String = "TEXT"): CommunityPost =
        api.createCommunityPost(CommunityPostRequest(content, mediaUrl, mediaType)).post
    suspend fun replyToPost(postId: String, content: String, mediaUrl: String? = null, mediaType: String = "TEXT"): CommunityReply =
        api.replyToPost(postId, CommunityPostRequest(content, mediaUrl, mediaType)).reply
    suspend fun reactToPost(postId: String, emoji: String) =
        api.reactToPost(postId, CommunityReactRequest(emoji))
    suspend fun deletePost(postId: String) = api.deletePost(postId)
    suspend fun searchUsers(query: String): List<CommunityUser> = api.searchUsers(query).users
    suspend fun getUserProfile(userId: String): CommunityUserProfileResponse = api.getUserProfile(userId)

    // Teams
    suspend fun getTeams(): List<Team> = api.getTeams().teams
    suspend fun createTeam(name: String): Team = api.createTeam(CreateTeamRequest(name)).team
    suspend fun joinTeam(inviteCode: String): Team = api.joinTeam(JoinTeamRequest(inviteCode)).team
    suspend fun getTeamDetail(id: String): Team = api.getTeamDetail(id).team
    suspend fun shareRoutine(teamId: String, routineId: String) =
        api.shareRoutine(teamId, ShareRoutineRequest(routineId))
    suspend fun copyRoutine(teamId: String, routineId: String): Routine =
        api.copyRoutine(teamId, routineId).routine
    suspend fun createTeamPost(teamId: String, content: String): TeamPost =
        api.createTeamPost(teamId, TeamPostRequest(content)).post
    suspend fun leaveTeam(teamId: String) = api.leaveTeam(teamId)

    // Progress Photos
    suspend fun getProgressPhotos(): List<ProgressPhoto> = api.getProgressPhotos().photos
    suspend fun uploadProgressPhoto(photoUrl: String, weight: Float? = null): ProgressPhoto =
        api.uploadProgressPhoto(UploadProgressPhotoRequest(photoUrl, weight)).photo
    suspend fun deleteProgressPhoto(id: String) = api.deleteProgressPhoto(id)
    suspend fun updateProgressPhoto(id: String, weight: Float?): ProgressPhoto =
        api.updateProgressPhoto(id, UpdateProgressPhotoRequest(weight)).photo
}
