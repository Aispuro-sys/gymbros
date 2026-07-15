import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Card, Button, Loading } from '../components/UI';

export default function ProfileScreen() {
  const { user, logout, updateUser, theme, toggleTheme } = useAuth();
  const c = getColors(theme);
  const [form, setForm] = useState({
    username: '', email: '', phone: '', age: '', height_cm: '', weight_kg: '', goal: 'MAINTENANCE', gender: 'M', body_type: '',
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (user) {
      setForm({
        username: user.username || '',
        email: user.email || '',
        phone: user.phone || '',
        age: user.age?.toString() || '',
        height_cm: user.height_cm?.toString() || '',
        weight_kg: user.weight_kg?.toString() || '',
        goal: user.goal || 'MAINTENANCE',
        gender: user.gender || 'M',
        body_type: user.body_type || '',
      });
    }
  }, [user]);

  const save = async () => {
    setSaving(true);
    try {
      const data = await apiCall('/auth/profile', 'PUT', {
        username: form.username,
        phone: form.phone,
        age: form.age ? parseInt(form.age) : null,
        height_cm: form.height_cm ? parseFloat(form.height_cm) : null,
        weight_kg: form.weight_kg ? parseFloat(form.weight_kg) : null,
        goal: form.goal,
        gender: form.gender,
        body_type: form.body_type || null,
      });
      updateUser(data.user);
      Alert.alert('✅', 'Perfil guardado');
    } catch (e) { Alert.alert('Error', e.message); }
    setSaving(false);
  };

  const saveBodyType = async () => {
    try {
      const data = await apiCall('/auth/profile', 'PUT', { body_type: form.body_type });
      updateUser(data.user);
      Alert.alert('✅', 'Tipo de cuerpo guardado');
    } catch (e) { Alert.alert('Error', e.message); }
  };

  if (!user) return <Loading />;

  const bmi = (form.height_cm && form.weight_kg) ? (parseFloat(form.weight_kg) / Math.pow(parseFloat(form.height_cm) / 100, 2)).toFixed(1) : null;

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.bg }} contentContainerStyle={{ padding: 16 }}>
      <View style={styles.header}>
        <Text style={[styles.title, { color: c.text }]}>Perfil</Text>
        <View style={{ flexDirection: 'row', gap: 8 }}>
          <TouchableOpacity onPress={toggleTheme} style={[styles.themeBtn, { backgroundColor: c.surface2, borderColor: c.border }]}>
            <Text style={{ fontSize: 18 }}>{theme === 'dark' ? '☀️' : '🌙'}</Text>
          </TouchableOpacity>
          <Button title="Salir" onPress={logout} variant="danger" style={{ width: 80 }} />
        </View>
      </View>

      <View style={styles.avatarSection}>
        <View style={[styles.avatar, { backgroundColor: c.accentBlue }]}>
          <Text style={{ color: '#fff', fontSize: 32, fontWeight: '800' }}>{user.username?.[0]?.toUpperCase()}</Text>
        </View>
        <Text style={{ color: c.text, fontSize: 18, fontWeight: '700', marginTop: 8 }}>{user.username}</Text>
        <Text style={{ color: c.text2, fontSize: 14 }}>{user.email}</Text>
      </View>

      <Card>
        <Text style={{ color: c.text, fontSize: 16, fontWeight: '700', marginBottom: 12 }}>Datos personales</Text>
        <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Usuario</Text>
        <TextInput style={inputS(c)} value={form.username} onChangeText={v => setForm({ ...form, username: v })} />
        <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Correo</Text>
        <TextInput style={[inputS(c), { opacity: 0.5 }]} value={form.email} editable={false} />
        <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Teléfono (WhatsApp)</Text>
        <TextInput style={inputS(c)} placeholder="+52 614 123 4567" placeholderTextColor={c.text3} value={form.phone} onChangeText={v => setForm({ ...form, phone: v })} keyboardType="phone-pad" />
        <View style={styles.row}>
          <View style={{ flex: 1, marginRight: 8 }}>
            <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Edad</Text>
            <TextInput style={inputS(c)} value={form.age} onChangeText={v => setForm({ ...form, age: v })} keyboardType="numeric" />
          </View>
          <View style={{ flex: 1 }}>
            <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Altura (cm)</Text>
            <TextInput style={inputS(c)} value={form.height_cm} onChangeText={v => setForm({ ...form, height_cm: v })} keyboardType="numeric" />
          </View>
        </View>
        <View style={styles.row}>
          <View style={{ flex: 1, marginRight: 8 }}>
            <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Peso (kg)</Text>
            <TextInput style={inputS(c)} value={form.weight_kg} onChangeText={v => setForm({ ...form, weight_kg: v })} keyboardType="numeric" />
          </View>
          <View style={{ flex: 1 }}>
            <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Objetivo</Text>
            <View style={styles.pickerRow}>
              {['MAINTENANCE', 'BULKING', 'CUTTING'].map(g => (
                <TouchableOpacity key={g} onPress={() => setForm({ ...form, goal: g })} style={[styles.pickerBtn, { backgroundColor: form.goal === g ? c.accentBlue : c.surface2, borderColor: c.border }]}>
                  <Text style={{ color: form.goal === g ? '#fff' : c.text2, fontSize: 11 }}>{g === 'MAINTENANCE' ? 'Mantener' : g === 'BULKING' ? 'Volumen' : 'Definición'}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>
        </View>
        <Button title="Guardar cambios" onPress={save} loading={saving} style={{ marginTop: 8 }} />
      </Card>

      <Card>
        <Text style={{ color: c.text, fontSize: 16, fontWeight: '700', marginBottom: 8 }}>Tipo de cuerpo</Text>
        <View style={styles.pickerRow}>
          {['ECTOMORPH', 'MESOMORPH', 'ENDOMORPH'].map(t => (
            <TouchableOpacity key={t} onPress={() => setForm({ ...form, body_type: t })} style={[styles.pickerBtn, { flex: 1, backgroundColor: form.body_type === t ? c.accentBlue : c.surface2, borderColor: c.border }]}>
              <Text style={{ color: form.body_type === t ? '#fff' : c.text2, fontSize: 12 }}>{t === 'ECTOMORPH' ? 'Ectomorfo' : t === 'MESOMORPH' ? 'Mesomorfo' : 'Endomorfo'}</Text>
            </TouchableOpacity>
          ))}
        </View>
        <Button title="Guardar tipo de cuerpo" onPress={saveBodyType} variant="secondary" style={{ marginTop: 8 }} />
      </Card>

      {bmi && (
        <Card>
          <Text style={{ color: c.text, fontSize: 16, fontWeight: '700', marginBottom: 8 }}>Tu IMC</Text>
          <Text style={{ color: c.text, fontSize: 28, fontWeight: '800' }}>{bmi}</Text>
          <Text style={{ color: c.text2, fontSize: 13, marginTop: 4 }}>
            {bmi < 18.5 ? 'Bajo peso' : bmi < 25 ? 'Peso normal' : bmi < 30 ? 'Sobrepeso' : 'Obesidad'}
          </Text>
        </Card>
      )}
    </ScrollView>
  );
}

const inputS = (c) => ({ height: 44, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1, marginBottom: 12, backgroundColor: c.surface2, color: c.text, borderColor: c.border });

const styles = StyleSheet.create({
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  title: { fontSize: 28, fontWeight: '800' },
  themeBtn: { width: 44, height: 44, borderRadius: 10, borderWidth: 1, justifyContent: 'center', alignItems: 'center' },
  avatarSection: { alignItems: 'center', marginBottom: 20 },
  avatar: { width: 80, height: 80, borderRadius: 40, justifyContent: 'center', alignItems: 'center' },
  row: { flexDirection: 'row' },
  pickerRow: { flexDirection: 'row', gap: 8, marginBottom: 12 },
  pickerBtn: { paddingVertical: 8, paddingHorizontal: 10, borderRadius: 8, borderWidth: 1, alignItems: 'center' },
});
