import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, FlatList, TouchableOpacity, StyleSheet, RefreshControl, Modal, TextInput } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Card, Button, Loading, EmptyState } from '../components/UI';

export default function RoutinesScreen() {
  const { theme } = useAuth();
  const c = getColors(theme);
  const [routines, setRoutines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [newName, setNewName] = useState('');

  const load = useCallback(async () => {
    try {
      const data = await apiCall('/routines');
      setRoutines(data.routines || []);
    } catch (e) { console.error(e); }
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  const onRefresh = () => { setRefreshing(true); load(); };

  const createRoutine = async () => {
    if (!newName.trim()) return;
    try {
      await apiCall('/routines', 'POST', { name: newName });
      setNewName('');
      setShowModal(false);
      load();
    } catch (e) { alert(e.message); }
  };

  const deleteRoutine = async (id) => {
    try {
      await apiCall(`/routines/${id}`, 'DELETE');
      load();
    } catch (e) { alert(e.message); }
  };

  if (loading) return <Loading />;

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.bg }} contentContainerStyle={{ padding: 16 }}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.accentBlue} />}>
      <View style={styles.header}>
        <Text style={[styles.title, { color: c.text }]}>Rutinas</Text>
        <Button title="+ Nueva" onPress={() => setShowModal(true)} style={{ width: 100 }} />
      </View>

      {routines.length === 0 ? (
        <EmptyState icon="🏋️" title="No hay rutinas" subtitle="Crea tu primera rutina" />
      ) : (
        routines.map(r => (
          <Card key={r.id}>
            <View style={styles.routineHeader}>
              <View style={{ flex: 1 }}>
                <Text style={{ color: c.text, fontSize: 16, fontWeight: '600' }}>{r.name}</Text>
                <Text style={{ color: c.text2, fontSize: 13, marginTop: 2 }}>{r.exercises?.length || 0} ejercicios</Text>
              </View>
              <TouchableOpacity onPress={() => deleteRoutine(r.id)}>
                <Text style={{ color: c.danger, fontSize: 20 }}>×</Text>
              </TouchableOpacity>
            </View>
            {r.exercises?.map((ex, i) => (
              <View key={i} style={{ flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 6, borderTopWidth: 1, borderTopColor: c.border, marginTop: 4 }}>
                <Text style={{ color: c.text, fontSize: 14 }}>{ex.name}</Text>
                <Text style={{ color: c.text2, fontSize: 13 }}>{ex.sets}x{ex.reps}</Text>
              </View>
            ))}
          </Card>
        ))
      )}

      <Modal visible={showModal} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalContent, { backgroundColor: c.bg2 }]}>
            <Text style={[styles.modalTitle, { color: c.text }]}>Nueva rutina</Text>
            <TextInput style={[styles.modalInput, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Nombre de la rutina" placeholderTextColor={c.text3} value={newName} onChangeText={setNewName} />
            <View style={{ flexDirection: 'row', gap: 12 }}>
              <Button title="Cancelar" onPress={() => setShowModal(false)} variant="secondary" style={{ flex: 1 }} />
              <Button title="Crear" onPress={createRoutine} style={{ flex: 1 }} />
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
  routineHeader: { flexDirection: 'row', alignItems: 'center' },
  modalOverlay: { flex: 1, justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)', padding: 24 },
  modalContent: { borderRadius: 16, padding: 20 },
  modalTitle: { fontSize: 20, fontWeight: '700', marginBottom: 16 },
  modalInput: { height: 48, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1, marginBottom: 16 },
});
