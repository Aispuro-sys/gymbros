import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { Loading } from '../components/UI';

import LoginScreen from '../screens/LoginScreen';
import RegisterScreen from '../screens/RegisterScreen';
import OverviewScreen from '../screens/OverviewScreen';
import AICoachScreen from '../screens/AICoachScreen';
import RoutinesScreen from '../screens/RoutinesScreen';
import ExercisesScreen from '../screens/ExercisesScreen';
import NutritionScreen from '../screens/NutritionScreen';
import SupplementsScreen from '../screens/SupplementsScreen';
import TeamsScreen from '../screens/TeamsScreen';
import RecipesScreen from '../screens/RecipesScreen';
import ShoppingListScreen from '../screens/ShoppingListScreen';
import CommunityScreen from '../screens/CommunityScreen';
import ProfileScreen from '../screens/ProfileScreen';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

function MainTabs() {
  const { theme } = useAuth();
  const c = getColors(theme);

  return (
    <Tab.Navigator
      screenOptions={{
        tabBarActiveTintColor: c.accentBlue,
        tabBarInactiveTintColor: c.text3,
        tabBarStyle: { backgroundColor: c.bg2, borderTopColor: c.border, borderTopWidth: 1 },
        headerShown: false,
      }}
    >
      <Tab.Screen name="Overview" component={OverviewScreen}
        options={{ tabBarIcon: ({ color, size }) => <Ionicons name="grid-outline" color={color} size={size} />, tabBarLabel: 'Inicio' }}
      />
      <Tab.Screen name="Routines" component={RoutinesScreen}
        options={{ tabBarIcon: ({ color, size }) => <Ionicons name="barbell-outline" color={color} size={size} />, tabBarLabel: 'Rutinas' }}
      />
      <Tab.Screen name="Nutrition" component={NutritionScreen}
        options={{ tabBarIcon: ({ color, size }) => <Ionicons name="nutrition-outline" color={color} size={size} />, tabBarLabel: 'Nutri' }}
      />
      <Tab.Screen name="Shopping" component={ShoppingListScreen}
        options={{ tabBarIcon: ({ color, size }) => <Ionicons name="cart-outline" color={color} size={size} />, tabBarLabel: 'Super' }}
      />
      <Tab.Screen name="Recipes" component={RecipesScreen}
        options={{ tabBarIcon: ({ color, size }) => <Ionicons name="restaurant-outline" color={color} size={size} />, tabBarLabel: 'Recetas' }}
      />
      <Tab.Screen name="Community" component={CommunityScreen}
        options={{ tabBarIcon: ({ color, size }) => <Ionicons name="chatbubbles-outline" color={color} size={size} />, tabBarLabel: 'Comunidad' }}
      />
      <Tab.Screen name="Profile" component={ProfileScreen}
        options={{ tabBarIcon: ({ color, size }) => <Ionicons name="person-outline" color={color} size={size} />, tabBarLabel: 'Perfil' }}
      />
    </Tab.Navigator>
  );
}

export default function AppNavigator() {
  const { user, loading } = useAuth();
  const { theme } = useAuth();
  const c = getColors(theme);

  if (loading) return <Loading text="Iniciando..." />;

  return (
    <NavigationContainer theme={{ colors: { background: c.bg, card: c.bg2, text: c.text, border: c.border, primary: c.accentBlue } }}>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        {!user ? (
          <>
            <Stack.Screen name="Login" component={LoginScreen} />
            <Stack.Screen name="Register" component={RegisterScreen} />
          </>
        ) : (
          <>
            <Stack.Screen name="Main" component={MainTabs} />
            <Stack.Screen name="AICoach" component={AICoachScreen} options={{ headerShown: true, title: 'IA Coach', headerStyle: { backgroundColor: c.bg2 }, headerTintColor: c.text }} />
            <Stack.Screen name="Exercises" component={ExercisesScreen} options={{ headerShown: true, title: 'Ejercicios', headerStyle: { backgroundColor: c.bg2 }, headerTintColor: c.text }} />
            <Stack.Screen name="Supplements" component={SupplementsScreen} options={{ headerShown: true, title: 'Suplementos', headerStyle: { backgroundColor: c.bg2 }, headerTintColor: c.text }} />
            <Stack.Screen name="Teams" component={TeamsScreen} options={{ headerShown: true, title: 'Equipos', headerStyle: { backgroundColor: c.bg2 }, headerTintColor: c.text }} />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
