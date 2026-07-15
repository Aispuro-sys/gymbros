# Talos Mobil — React Native (Expo)

App móvil de Talos Forge — Your Progress.
Port completo de la app web a React Native con Expo para iOS y Android.

## Requisitos
- Node.js 18+
- Expo CLI (`npm install -g expo-cli`)
- Android Studio (para emulador Android) o Xcode (para iOS)

## Instalación
```bash
cd "talos mobil"
npm install
```

## Configurar API
Edita `src/api/client.js` y cambia `API_URL` por la URL de tu backend:
```js
const API_URL = 'http://TU_IP_LOCAL:3000/api';
// o tu URL de producción:
// const API_URL = 'https://talos-forge.vercel.app/api';
```

## Ejecutar
```bash
npx expo start
```
- Presiona `a` para Android
- Presiona `i` para iOS
- Escanea el QR con Expo Go en tu teléfono

## Pantallas
- Login / Registro
- Resumen (Dashboard)
- IA Coach
- Rutinas
- Ejercicios
- Nutrición
- Suplementos
- Equipos
- Recetas
- Lista de Supermercado
- Comunidad
- Perfil
