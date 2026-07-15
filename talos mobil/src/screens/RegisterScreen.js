import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { Button } from '../components/UI';

export default function RegisterScreen({ navigation }) {
  const { register } = useAuth();
  const { theme } = useAuth();
  const c = getColors(theme);
  const [form, setForm] = useState({
    username: '', email: '', password: '', age: '', height_cm: '', weight_kg: '', goal: 'MAINTENANCE', gender: 'M',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const set = (key, val) => setForm(prev => ({ ...prev, [key]: val }));

  const handleRegister = async () => {
    setError('');
    if (!form.username || !form.email || !form.password) {
      setError('Usuario, correo y contraseña son obligatorios');
      return;
    }
    setLoading(true);
    try {
      await register({
        username: form.username,
        email: form.email,
        password: form.password,
        age: form.age ? parseInt(form.age) : null,
        height_cm: form.height_cm ? parseFloat(form.height_cm) : null,
        weight_kg: form.weight_kg ? parseFloat(form.weight_kg) : null,
        goal: form.goal,
        gender: form.gender,
      });
    } catch (e) {
      setError(e.message);
    }
    setLoading(false);
  };

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1, backgroundColor: c.bg }}>
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={[styles.title, { color: c.text }]}>Crear cuenta</Text>
        <Text style={[styles.subtitle, { color: c.text2 }]}>Empieza tu transformación</Text>

        {error ? <Text style={styles.error}>{error}</Text> : null}

        <View style={styles.form}>
          <Text style={[styles.label, { color: c.text2 }]}>Usuario</Text>
          <TextInput style={inputStyle(c)} placeholder="bro_lifter" placeholderTextColor={c.text3} value={form.username} onChangeText={v => set('username', v)} />
          <Text style={[styles.label, { color: c.text2 }]}>Correo</Text>
          <TextInput style={inputStyle(c)} placeholder="tu@email.com" placeholderTextColor={c.text3} value={form.email} onChangeText={v => set('email', v)} keyboardType="email-address" autoCapitalize="none" />
          <Text style={[styles.label, { color: c.text2 }]}>Contraseña</Text>
          <TextInput style={inputStyle(c)} placeholder="Mín. 6 caracteres" placeholderTextColor={c.text3} value={form.password} onChangeText={v => set('password', v)} secureTextEntry />

          <View style={styles.row}>
            <View style={{ flex: 1, marginRight: 8 }}>
              <Text style={[styles.label, { color: c.text2 }]}>Edad</Text>
              <TextInput style={inputStyle(c)} placeholder="24" placeholderTextColor={c.text3} value={form.age} onChangeText={v => set('age', v)} keyboardType="numeric" />
            </View>
            <View style={{ flex: 1 }}>
              <Text style={[styles.label, { color: c.text2 }]}>Altura (cm)</Text>
              <TextInput style={inputStyle(c)} placeholder="178" placeholderTextColor={c.text3} value={form.height_cm} onChangeText={v => set('height_cm', v)} keyboardType="numeric" />
            </View>
          </View>

          <View style={styles.row}>
            <View style={{ flex: 1, marginRight: 8 }}>
              <Text style={[styles.label, { color: c.text2 }]}>Peso (kg)</Text>
              <TextInput style={inputStyle(c)} placeholder="85" placeholderTextColor={c.text3} value={form.weight_kg} onChangeText={v => set('weight_kg', v)} keyboardType="numeric" />
            </View>
            <View style={{ flex: 1 }}>
              <Text style={[styles.label, { color: c.text2 }]}>Objetivo</Text>
              <View style={styles.pickerRow}>
                {['MAINTENANCE', 'BULKING', 'CUTTING'].map(g => (
                  <TouchableOpacity key={g} onPress={() => set('goal', g)} style={[styles.pickerBtn, { backgroundColor: form.goal === g ? c.accentBlue : c.surface2, borderColor: c.border }]}>
                    <Text style={{ color: form.goal === g ? '#fff' : c.text2, fontSize: 12 }}>{g === 'MAINTENANCE' ? 'Mantener' : g === 'BULKING' ? 'Volumen' : 'Definición'}</Text>
                  </TouchableOpacity>
                ))}
              </View>
            </View>
          </View>

          <Text style={[styles.label, { color: c.text2 }]}>Género</Text>
          <View style={styles.pickerRow}>
            <TouchableOpacity onPress={() => set('gender', 'M')} style={[styles.pickerBtn, { flex: 1, backgroundColor: form.gender === 'M' ? c.accentBlue : c.surface2, borderColor: c.border }]}>
              <Text style={{ color: form.gender === 'M' ? '#fff' : c.text2 }}>Masculino</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => set('gender', 'F')} style={[styles.pickerBtn, { flex: 1, backgroundColor: form.gender === 'F' ? c.accentBlue : c.surface2, borderColor: c.border }]}>
              <Text style={{ color: form.gender === 'F' ? '#fff' : c.text2 }}>Femenino</Text>
            </TouchableOpacity>
          </View>

          <Button title="Crear cuenta" onPress={handleRegister} loading={loading} style={{ marginTop: 16 }} />
        </View>

        <TouchableOpacity onPress={() => navigation.navigate('Login')} style={styles.switch}>
          <Text style={{ color: c.text2 }}>¿Ya tienes cuenta? </Text>
          <Text style={{ color: c.accentBlue, fontWeight: '600' }}>Inicia sesión</Text>
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const inputStyle = (c) => ({ height: 48, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1, marginBottom: 12, backgroundColor: c.surface2, color: c.text, borderColor: c.border });

const styles = StyleSheet.create({
  container: { flexGrow: 1, padding: 24 },
  title: { fontSize: 28, fontWeight: '800', marginBottom: 4 },
  subtitle: { fontSize: 15, marginBottom: 20, color: '#a0a0a0' },
  error: { backgroundColor: 'rgba(255,68,68,0.1)', color: '#ff4444', padding: 12, borderRadius: 8, marginBottom: 16, fontSize: 14 },
  form: { marginBottom: 16 },
  label: { fontSize: 13, fontWeight: '500', marginBottom: 6 },
  row: { flexDirection: 'row' },
  pickerRow: { flexDirection: 'row', gap: 8, marginBottom: 12 },
  pickerBtn: { paddingVertical: 10, paddingHorizontal: 12, borderRadius: 8, borderWidth: 1, alignItems: 'center' },
  switch: { flexDirection: 'row', justifyContent: 'center' },
});
