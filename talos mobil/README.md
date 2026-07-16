# Talos Forge — Android App

App Android nativa (Kotlin + Jetpack Compose) para Talos Forge — Your Progress.

## Requisitos
- Android Studio (Hedgehog o superior)
- JDK 17
- Android SDK 34 (minSdk 24)
- Kotlin 2.0+

## Configuración

1. Abre Android Studio → **Open** → selecciona la carpeta `talos mobil`
2. Espera a que Gradle sincronice
3. Cambia la URL del backend en `ApiClient.kt`:
   - **Emulador Android**: `http://10.0.2.2:3000/api/` (apunta al localhost de tu PC)
   - **Dispositivo físico**: `http://<IP_DE_TU_PC>:3000/api/`
   - **Producción**: `https://tu-dominio.com/api/`
4. Ejecuta con **Run ▶**

## Arquitectura

```
app/src/main/java/com/talos/forge/
├── TalosApp.kt              # Application class (init SessionManager + Retrofit)
├── MainActivity.kt          # Entry point + Navigation + Auth gate
├── data/
│   ├── ApiClient.kt         # Retrofit setup + OkHttp + interceptors
│   ├── ApiService.kt        # Retrofit interface (all endpoints)
│   ├── Repository.kt        # Data repository wrapping API calls
│   ├── SessionManager.kt    # SharedPreferences token storage
│   ├── AuthInterceptor.kt   # Adds Bearer token to all requests
│   └── models/Models.kt     # All data classes
├── ui/
│   ├── ViewModelFactory.kt  # ViewModel provider factory
│   ├── AuthViewModel.kt     # Login/Register state
│   ├── DashboardViewModel.kt
│   ├── RoutinesViewModel.kt
│   ├── NutritionViewModel.kt
│   ├── SupplementsViewModel.kt
│   ├── RecipesViewModel.kt
│   ├── ShoppingViewModel.kt
│   ├── CommunityViewModel.kt
│   ├── ProfileViewModel.kt
│   ├── theme/               # Colors + Theme (Material3, dark mode)
│   ├── components/          # Reusable Compose components
│   ├── navigation/Screen.kt # Bottom nav routes
│   └── screens/             # All Compose screens
│       ├── AuthScreen.kt
│       ├── DashboardScreen.kt
│       ├── RoutinesScreen.kt
│       ├── NutritionScreen.kt
│       ├── SupplementsScreen.kt
│       ├── RecipesScreen.kt
│       ├── ShoppingScreen.kt
│       ├── CommunityScreen.kt
│       └── ProfileScreen.kt
```

## Features

- **Auth**: Login / Registro con persistencia de token
- **Dashboard**: Macros, comidas y rutinas resumidas
- **Rutinas**: CRUD de rutinas y ejercicios
- **Nutrición**: Registro de comidas, macros del día
- **Suplementos**: CRUD de suplementos/medicamentos
- **Recetas**: Búsqueda + recomendación con IA
- **Lista de Super**: Generar desde comidas/IA, marcar items, compartir link
- **Comunidad**: Feed de publicaciones
- **Perfil**: Datos del usuario, logout

## Stack
- Kotlin + Jetpack Compose
- Material 3 (Material You)
- MVVM + Repository pattern
- Retrofit + Gson + OkHttp
- Coil (image loading)
- Coroutines + StateFlow
- Navigation Compose
