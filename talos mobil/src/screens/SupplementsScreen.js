import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, RefreshControl, Modal, TextInput, Alert } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Card, Button, Loading, EmptyState } from '../components/UI';

export default function SupplementsScreen() {
  const { theme } = useAuth();
  const c = getColors(theme);
  const [supplements, setSupplements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ name: '', dosage: '', time_of_day: 'MORNING', is_medication: false });

  const load = useCallback(async () => {
    try {
      const data = await apiCall('/supplements');
      setSupplements(data.supplements || []);
    } catch (e) { console.error(e); }
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  const onRefresh = () => { setRefreshing(true); load(); };

  const addSupplement = async () => {
    if (!form.name.trim()) { Alert.alert('Error', 'Nombre requerido'); return; }
    try {
      await apiCall('/supplements', 'POST', form);
      setForm({ name: '', dosage: '', time_of_day: 'MORNING', is_medication: false });
      setShowModal(false);
      load();
    } catch (e) { Alert.alert('Error', e.message); }
  };

  const deleteSupplement = async (id) => {
    try { await apiCall(`/supplements/${id}`, 'DELETE'); load(); } catch (e) { Alert.alert('Error', e.message); }
  };

  if (loading) return <Loading />;

  const timeLabels = { MORNING: 'Mañana', AFTERNOON: 'Tarde', EVENING: 'Noche', NIGHT: 'Antes de dormir' };

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.bg }} contentContainerStyle={{ padding: 16 }}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.accentBlue} />}>
      <View style={styles.header}>
        <Text style={[styles.title, { color: c.text }]}>Suplementos</Text>
        <Button title="+ Agregar" onPress={() => setShowModal(true)} style={{ width: 120 }} />
      </View>

      {supplements.length === 0 ? (
        <EmptyState icon="💊" title="No hay suplementos" subtitle="Agrega tus suplementos y medicamentos" />
      ) : (
        supplements.map(s => (
          <Card key={s.id}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
              <View style={{ flex: 1 }}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                  <Text style={{ color: c.text, fontSize: 15, fontWeight: '600' }}>{s.name}</Text>
                  {s.is_medication && <View style={{ backgroundColor: c.danger, paddingHorizontal: 6, paddingVertical: 2, borderRadius: 4 }}><Text style={{ color: '#fff', fontSize: 10 }}>Med</Text></View>}
                </View>
                <Text style={{ color: c.text2, fontSize: 13, marginTop: 2 }}>{s.dosage} · {timeLabels[s.time_of_day] || s.time_of_day}</Text>
              </View>
              <TouchableOpacity onPress={() => deleteSupplement(s.id)}>
                <Text style={{ color: c.danger, fontSize: 20 }}>×</Text>
              </TouchableOpacity>
            </View>
          </Card>
        ))
      )}

      <Modal visible={showModal} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalContent, { backgroundColor: c.bg2 }]}>
            <Text style={[styles.modalTitle, { color: c.text }]}>Agregar suplemento</Text>
            <TextInput style={[styles.input, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Nombre" placeholderTextColor={c.text3} value={form.name} onChangeText={v => setForm({ ...form, name: v })} />
            <TextInput style={[styles.input, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Dosis (ej. 5g, 1 cápsula)" placeholderTextColor={c.text3} value={form.dosage} onChangeText={v => setForm({ ...form, dosage: v })} />
            <Text style={{ color: c.text2, fontSize: 13, marginBottom: 6 }}>Horario</Text>
            <View style={styles.pickerRow}>
              {['MORNING', 'AFTERNOON', 'EVENING', 'NIGHT'].map(t => (
                <TouchableOpacity key={t} onPress={() => setForm({ ...form, time_of_day: t })} style={[styles.pickerBtn, { backgroundColor: form.time_of_day === t ? c.accentBlue : c.surface2, borderColor: c.border }]}>
                  <Text style={{ color: form.time_of_day === t ? '#fff' : c.text2, fontSize: 12 }}>{timeLabels[t]}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <TouchableOpacity onPress={() => setForm({ ...form, is_medication: !form.is_medication })} style={{ flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 16 }}>
              <View style={{ width: 20, height: 20, borderRadius: 4, borderWidth: 2, borderColor: c.border, backgroundColor: form.is_medication ? c.accentBlue : 'transparent', justifyContent: 'center', alignItems: 'center' }}>
                {form.is_medication && <Text style={{ color: '#fff', fontSize: 12 }}>✓</Text>}
              </View>
              <Text style={{ color: c.text2 }}>Es medicamento</Text>
            </TouchableOpacity>
            <View style={{ flexDirection: 'row', gap: 12 }}>
              <Button title="Cancelar" onPress={() => setShowModal(false)} variant="secondary" style={{ flex: 1 }} />
              <Button title="Agregar" onPress={addSupplement} style={{ flex: 1 }} />
            </View>
          </View>
        </View>
      </Modal>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  title: { fontSize: 28, fontWeight: '800' },
  modalOverlay: { flex: 1, justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)', padding: 24 },
  modalContent: { borderRadius: 16, padding: 20 },
  modalTitle: { fontSize: 20, fontWeight: '700', marginBottom: 16 },
  input: { height: 48, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1, marginBottom: 12 },
  pickerRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 12 },
  pickerBtn: { paddingVertical: 8, paddingHorizontal: 12, borderRadius: 8, borderWidth: 1 },
});
