import AsyncStorage from '@react-native-async-storage/async-storage';

// CAMBIA ESTA URL por la de tu backend
const API_URL = 'http://localhost:3000/api';

let authToken = null;

export async function initToken() {
  try {
    authToken = await AsyncStorage.getItem('talos_token');
  } catch (e) {
    authToken = null;
  }
  return authToken;
}

export function setToken(token) {
  authToken = token;
  if (token) {
    AsyncStorage.setItem('talos_token', token);
  } else {
    AsyncStorage.removeItem('talos_token');
  }
}

export function getToken() {
  return authToken;
}

export async function apiCall(endpoint, method = 'GET', body = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

  try {
    const res = await fetch(`${API_URL}${endpoint}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : null,
    });
    const text = await res.text();
    let data;
    try { data = JSON.parse(text); } catch { data = { error: text }; }
    if (!res.ok) throw new Error(data.error || `Error ${res.status}`);
    return data;
  } catch (err) {
    if (err.message.includes('Network request failed')) {
      throw new Error('No se pudo conectar al servidor. Verifica tu conexión y la URL del API.');
    }
    throw err;
  }
}

export { API_URL };
