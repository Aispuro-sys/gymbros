import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { initToken, setToken, apiCall } from '../api/client';
import AsyncStorage from '@react-native-async-storage/async-storage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [theme, setTheme] = useState('dark');

  useEffect(() => {
    (async () => {
      const token = await initToken();
      const savedTheme = await AsyncStorage.getItem('talos_theme') || 'dark';
      setTheme(savedTheme);
      if (token) {
        try {
          const data = await apiCall('/auth/me');
          setUser(data.user);
        } catch (e) {
          setToken(null);
        }
      }
      setLoading(false);
    })();
  }, []);

  const login = useCallback(async (email, password) => {
    const data = await apiCall('/auth/login', 'POST', { email, password });
    setToken(data.token);
    setUser(data.user);
    return data;
  }, []);

  const register = useCallback(async (userData) => {
    const data = await apiCall('/auth/register', 'POST', userData);
    setToken(data.token);
    setUser(data.user);
    return data;
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  const updateUser = useCallback((updated) => {
    setUser(prev => ({ ...prev, ...updated }));
  }, []);

  const toggleTheme = useCallback(() => {
    setTheme(prev => {
      const next = prev === 'dark' ? 'light' : 'dark';
      AsyncStorage.setItem('talos_theme', next);
      return next;
    });
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, theme, login, register, logout, updateUser, toggleTheme }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
