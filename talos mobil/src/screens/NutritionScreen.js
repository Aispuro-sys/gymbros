import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, RefreshControl, Modal, TextInput, Alert } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Card, Button, Loading, EmptyState } from '../components/UI';

export default function NutritionScreen() {
  const { theme } = useAuth();
  const c = getColors(theme);
  const [meals, setMeals] = useState([]);
  const [macros, setMacros] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ name: '', calories: '', protein_g: '', carbs_g: '', fats_g: '', meal_type: 'SNACK' });

  const load = useCallback(async () => {
    try {
      const [mealsData, macrosData] = await Promise.all([
        apiCall('/meals/today').catch(() => ({ meals: [] })),
        apiCall('/macros/today').catch(() => null),
      ]);
      setMeals(mealsData.meals || []);
      setMacros(macrosData);
    } catch (e) { console.error(e); }
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  const onRefresh = () => { setRefreshing(true); load(); };

  const addMeal = async () => {
    if (!form.name.trim()) { Alert.alert('Error', 'Nombre requerido'); return; }
    try {
      await apiCall('/meals', 'POST', {
        name: form.name,
        calories: parseInt(form.calories) || 0,
        protein_g: parseInt(form.protein_g) || 0,
        carbs_g: parseInt(form.carbs_g) || 0,
        fats_g: parseInt(form.fats_g) || 0,
        meal_type: form.meal_type,
      });
      setForm({ name: '', calories: '', protein_g: '', carbs_g: '', fats_g: '', meal_type: 'SNACK' });
      setShowModal(false);
      load();
    } catch (e) { Alert.alert('Error', e.message); }
  };

  const deleteMeal = async (id) => {
    try { await apiCall(`/meals/${id}`, 'DELETE'); load(); } catch (e) { Alert.alert('Error', e.message); }
  };

  if (loading) return <Loading />;

  const totalCal = meals.reduce((s, m) => s + (m.calories || 0), 0);
  const totalProtein = meals.reduce((s, m) => s + (m.protein_g || 0), 0);

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.bg }} contentContainerStyle={{ padding: 16 }}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.accentBlue} />}>
      <View style={styles.header}>
        <Text style={[styles.title, { color: c.text }]}>Nutrición</Text>
        <Button title="+ Registrar" onPress={() => setShowModal(true)} style={{ width: 120 }} />
      </View>

      <View style={styles.statsRow}>
        <Card style={{ flex: 1, marginRight: 8, alignItems: 'center' }}>
          <Text style={{ color: c.text, fontSize: 22, fontWeight: '700' }}>{totalCal}</Text>
          <Text style={{ color: c.text2, fontSize: 12 }}>Calorías</Text>
        </Card>
        <Card style={{ flex: 1, marginRight: 8, alignItems: 'center' }}>
          <Text style={{ color: c.text, fontSize: 22, fontWeight: '700' }}>{totalProtein}g</Text>
          <Text style={{ color: c.text2, fontSize: 12 }}>Proteína</Text>
        </Card>
        <Card style={{ flex: 1, alignItems: 'center' }}>
          <Text style={{ color: c.text, fontSize: 22, fontWeight: '700' }}>{meals.length}</Text>
          <Text style={{ color: c.text2, fontSize: 12 }}>Comidas</Text>
        </Card>
      </View>

      <Text style={[styles.sectionTitle, { color: c.text }]}>Comidas de hoy</Text>
      {meals.length === 0 ? (
        <EmptyState icon="🍽️" title="No hay comidas registradas" />
      ) : (
        meals.map(m => (
          <Card key={m.id}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
              <View style={{ flex: 1 }}>
                <Text style={{ color: c.text, fontSize: 15, fontWeight: '600' }}>{m.name}</Text>
                <Text style={{ color: c.text2, fontSize: 13, marginTop: 2 }}>{m.calories} cal · {m.protein_g}g prot · {m.meal_type}</Text>
              </View>
              <TouchableOpacity onPress={() => deleteMeal(m.id)}>
                <Text style={{ color: c.danger, fontSize: 20 }}>×</Text>
              </TouchableOpacity>
            </View>
          </Card>
        ))
      )}

      <Modal visible={showModal} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <ScrollView style={[styles.modalContent, { backgroundColor: c.bg2 }]}>
            <Text style={[styles.modalTitle, { color: c.text }]}>Registrar comida</Text>
            <TextInput style={[styles.input, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Nombre" placeholderTextColor={c.text3} value={form.name} onChangeText={v => setForm({ ...form, name: v })} />
            <View style={styles.row}>
              <TextInput style={[styles.input, { flex: 1, marginRight: 8, backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Calorías" placeholderTextColor={c.text3} value={form.calories} onChangeText={v => setForm({ ...form, calories: v })} keyboardType="numeric" />
              <TextInput style={[styles.input, { flex: 1, backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Proteína (g)" placeholderTextColor={c.text3} value={form.protein_g} onChangeText={v => setForm({ ...form, protein_g: v })} keyboardType="numeric" />
            </View>
            <View style={styles.row}>
              <TextInput style={[styles.input, { flex: 1, marginRight: 8, backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Carbs (g)" placeholderTextColor={c.text3} value={form.carbs_g} onChangeText={v => setForm({ ...form, carbs_g: v })} keyboardType="numeric" />
              <TextInput style={[styles.input, { flex: 1, backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Grasas (g)" placeholderTextColor={c.text3} value={form.fats_g} onChangeText={v => setForm({ ...form, fats_g: v })} keyboardType="numeric" />
            </View>
            <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Tipo</Text>
            <View style={styles.pickerRow}>
              {['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK', 'POST_WORKOUT'].map(t => (
                <TouchableOpacity key={t} onPress={() => setForm({ ...form, meal_type: t })} style={[styles.pickerBtn, { backgroundColor: form.meal_type === t ? c.accentBlue : c.surface2, borderColor: c.border }]}>
                  <Text style={{ color: form.meal_type === t ? '#fff' : c.text2, fontSize: 11 }}>{t === 'BREAKFAST' ? 'Desayuno' : t === 'LUNCH' ? 'Comida' : t === 'DINNER' ? 'Cena' : t === 'SNACK' ? 'Snack' : 'Post-entreno'}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <View style={{ flexDirection: 'row', gap: 12, marginTop: 8 }}>
              <Button title="Cancelar" onPress={() => setShowModal(false)} variant="secondary" style={{ flex: 1 }} />
              <Button title="Agregar" onPress={addMeal} style={{ flex: 1 }} />
            </View>
          </ScrollView>
        </View>
      </Modal>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  title: { fontSize: 28, fontWeight: '800' },
  statsRow: { flexDirection: 'row', marginBottom: 16 },
  sectionTitle: { fontSize: 16, fontWeight: '700', marginBottom: 8 },
  modalOverlay: { flex: 1, justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)', padding: 24 },
  modalContent: { borderRadius: 16, padding: 20 },
  modalTitle: { fontSize: 20, fontWeight: '700', marginBottom: 16 },
  input: { height: 48, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1, marginBottom: 12 },
  row: { flexDirection: 'row' },
  pickerRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 12 },
  pickerBtn: { paddingVertical: 8, paddingHorizontal: 10, borderRadius: 8, borderWidth: 1 },
});
