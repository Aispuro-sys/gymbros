import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { Button } from '../components/UI';

export default function LoginScreen({ navigation }) {
  const { login } = useAuth();
  const { theme } = useAuth();
  const c = getColors(theme);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    setError('');
    setLoading(true);
    try {
      await login(email, password);
    } catch (e) {
      setError(e.message);
    }
    setLoading(false);
  };

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1, backgroundColor: c.bg }}>
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.brand}>
          <View style={[styles.logo, { backgroundColor: c.accentBlue }]}>
            <Text style={styles.logoText}>TF</Text>
          </View>
          <Text style={[styles.title, { color: c.text }]}>Talos Forge</Text>
          <Text style={[styles.subtitle, { color: c.text2 }]}>Your Progress — Inicia sesión</Text>
        </View>

        {error ? <Text style={styles.error}>{error}</Text> : null}

        <View style={styles.form}>
          <Text style={[styles.label, { color: c.text2 }]}>Correo</Text>
          <TextInput
            style={[styles.input, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]}
            placeholder="tu@email.com"
            placeholderTextColor={c.text3}
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
          />
          <Text style={[styles.label, { color: c.text2 }]}>Contraseña</Text>
          <TextInput
            style={[styles.input, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]}
            placeholder="••••••••"
            placeholderTextColor={c.text3}
            value={password}
            onChangeText={setPassword}
            secureTextEntry
          />
          <Button title="Iniciar sesión" onPress={handleLogin} loading={loading} style={{ marginTop: 8 }} />
        </View>

        <TouchableOpacity onPress={() => navigation.navigate('Register')} style={styles.switch}>
          <Text style={{ color: c.text2 }}>¿No tienes cuenta? </Text>
          <Text style={{ color: c.accentBlue, fontWeight: '600' }}>Regístrate</Text>
        </TouchableOpacity>

        <Text style={[styles.demo, { color: c.text3 }]}>Demo: admin@gymbros.com / admin123</Text>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, justifyContent: 'center', padding: 24 },
  brand: { alignItems: 'center', marginBottom: 32 },
  logo: { width: 64, height: 64, borderRadius: 32, justifyContent: 'center', alignItems: 'center', marginBottom: 12 },
  logoText: { color: '#fff', fontSize: 24, fontWeight: '800' },
  title: { fontSize: 28, fontWeight: '800', marginBottom: 4 },
  subtitle: { fontSize: 15 },
  error: { backgroundColor: 'rgba(255,68,68,0.1)', color: '#ff4444', padding: 12, borderRadius: 8, marginBottom: 16, fontSize: 14 },
  form: { marginBottom: 16 },
  label: { fontSize: 13, fontWeight: '500', marginBottom: 6 },
  input: { height: 48, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1, marginBottom: 12 },
  switch: { flexDirection: 'row', justifyContent: 'center', marginBottom: 8 },
  demo: { textAlign: 'center', fontSize: 12 },
});
