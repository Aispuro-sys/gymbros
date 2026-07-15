import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, StyleSheet, ActivityIndicator } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Card, Button, Loading } from '../components/UI';

export default function AICoachScreen() {
  const { user, theme } = useAuth();
  const c = getColors(theme);
  const [days, setDays] = useState('4');
  const [equipment, setEquipment] = useState('all');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [nutritionResult, setNutritionResult] = useState(null);
  const [nutritionLoading, setNutritionLoading] = useState(false);

  const generateWeeklyPlan = async () => {
    setLoading(true);
    setResult(null);
    try {
      const data = await apiCall('/ai/weekly-plan', 'POST', { days: parseInt(days), equipment, notes });
      setResult(data);
    } catch (e) { setResult({ error: e.message }); }
    setLoading(false);
  };

  const generateNutritionPlan = async () => {
    setNutritionLoading(true);
    try {
      const data = await apiCall('/ai/nutrition-plan', 'POST', {});
      setNutritionResult(data);
    } catch (e) { setNutritionResult({ error: e.message }); }
    setNutritionLoading(false);
  };

  const inputStyle = { height: 48, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1, backgroundColor: c.surface2, color: c.text, borderColor: c.border, marginBottom: 12 };

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.bg }} contentContainerStyle={{ padding: 16 }}>
      <Text style={[styles.title, { color: c.text }]}>IA Coach</Text>
      <Text style={[styles.subtitle, { color: c.text2 }]}>Rutinas y planes personalizados con IA</Text>

      <Card>
        <Text style={[styles.cardTitle, { color: c.text }]}>Generar plan semanal</Text>
        <Text style={{ color: c.text2, fontSize: 13, marginBottom: 12 }}>La IA creará un plan completo de la semana.</Text>

        <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Días/semana</Text>
        <View style={styles.pickerRow}>
          {['3', '4', '5', '6'].map(d => (
            <TouchableOpacity key={d} onPress={() => setDays(d)} style={[styles.pickerBtn, { backgroundColor: days === d ? c.accentBlue : c.surface2, borderColor: c.border }]}>
              <Text style={{ color: days === d ? '#fff' : c.text2 }}>{d}</Text>
            </TouchableOpacity>
          ))}
        </View>

        <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Equipamiento</Text>
        <View style={styles.pickerRow}>
          {['all', 'barbell', 'dumbbell', 'body weight', 'machine', 'cable'].map(eq => (
            <TouchableOpacity key={eq} onPress={() => setEquipment(eq)} style={[styles.pickerBtn, { backgroundColor: equipment === eq ? c.accentBlue : c.surface2, borderColor: c.border }]}>
              <Text style={{ color: equipment === eq ? '#fff' : c.text2, fontSize: 11 }}>{eq === 'all' ? 'Todo' : eq === 'barbell' ? 'Barra' : eq === 'dumbbell' ? 'Mancuernas' : eq === 'body weight' ? 'Peso corp.' : eq === 'machine' ? 'Máquinas' : 'Cables'}</Text>
            </TouchableOpacity>
          ))}
        </View>

        <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Notas (opcional)</Text>
        <TextInput style={inputStyle} placeholder="ej. Evitar ejercicios que duelan la rodilla" placeholderTextColor={c.text3} value={notes} onChangeText={setNotes} />

        <Button title="Generar plan semanal" onPress={generateWeeklyPlan} loading={loading} />
      </Card>

      {result && (
        <Card>
          {result.error ? (
            <Text style={{ color: c.danger }}>{result.error}</Text>
          ) : (
            <View>
              <Text style={{ color: c.text, fontWeight: '700', fontSize: 16, marginBottom: 8 }}>✅ Plan generado</Text>
              {result.routines?.map((r, i) => (
                <View key={i} style={{ paddingVertical: 8, borderTopWidth: i > 0 ? 1 : 0, borderTopColor: c.border }}>
                  <Text style={{ color: c.text, fontWeight: '600' }}>{r.name}</Text>
                  <Text style={{ color: c.text2, fontSize: 13 }}>{r.exercises?.length || 0} ejercicios</Text>
                </View>
              ))}
            </View>
          )}
        </Card>
      )}

      <Card>
        <Text style={[styles.cardTitle, { color: c.text }]}>Plan nutricional + Comidas</Text>
        <Text style={{ color: c.text2, fontSize: 13, marginBottom: 12 }}>Calcula tus macros objetivo y recibe recomendaciones.</Text>
        <Button title="Generar plan nutricional" onPress={generateNutritionPlan} loading={nutritionLoading} variant="secondary" />
      </Card>

      {nutritionResult && (
        <Card>
          {nutritionResult.error ? (
            <Text style={{ color: c.danger }}>{nutritionResult.error}</Text>
          ) : (
            <View>
              <Text style={{ color: c.text, fontWeight: '700', fontSize: 16, marginBottom: 8 }}>✅ Plan nutricional</Text>
              {nutritionResult.macros && (
                <View style={{ flexDirection: 'row', gap: 12, marginBottom: 12 }}>
                  <Text style={{ color: c.text }}>{nutritionResult.macros.calories} cal</Text>
                  <Text style={{ color: c.text }}>{nutritionResult.macros.protein_g}g prot</Text>
                  <Text style={{ color: c.text }}>{nutritionResult.macros.carbs_g}g carbs</Text>
                </View>
              )}
              {nutritionResult.meals?.map((m, i) => (
                <View key={i} style={{ paddingVertical: 6, borderTopWidth: i > 0 ? 1 : 0, borderTopColor: c.border }}>
                  <Text style={{ color: c.text, fontWeight: '600' }}>{m.name}</Text>
                  <Text style={{ color: c.text2, fontSize: 13 }}>{m.calories} cal · {m.protein_g}g prot</Text>
                </View>
              ))}
            </View>
          )}
        </Card>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  title: { fontSize: 28, fontWeight: '800', marginBottom: 2 },
  subtitle: { fontSize: 14, marginBottom: 16, color: '#a0a0a0' },
  cardTitle: { fontSize: 16, fontWeight: '700', marginBottom: 8 },
  pickerRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 12 },
  pickerBtn: { paddingVertical: 8, paddingHorizontal: 12, borderRadius: 8, borderWidth: 1 },
});
