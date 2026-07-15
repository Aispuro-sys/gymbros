import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, RefreshControl, Modal, TextInput, Alert, FlatList } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Card, Button, Loading, EmptyState } from '../components/UI';

export default function TeamsScreen() {
  const { theme } = useAuth();
  const c = getColors(theme);
  const [teams, setTeams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showCreate, setShowCreate] = useState(false);
  const [showJoin, setShowJoin] = useState(false);
  const [teamName, setTeamName] = useState('');
  const [joinCode, setJoinCode] = useState('');

  const load = useCallback(async () => {
    try {
      const data = await apiCall('/teams');
      setTeams(data.teams || []);
    } catch (e) { console.error(e); }
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  const onRefresh = () => { setRefreshing(true); load(); };

  const createTeam = async () => {
    if (!teamName.trim()) return;
    try { await apiCall('/teams', 'POST', { name: teamName }); setTeamName(''); setShowCreate(false); load(); }
    catch (e) { Alert.alert('Error', e.message); }
  };

  const joinTeam = async () => {
    if (!joinCode.trim()) return;
    try { await apiCall('/teams/join', 'POST', { invite_code: joinCode }); setJoinCode(''); setShowJoin(false); load(); }
    catch (e) { Alert.alert('Error', e.message); }
  };

  if (loading) return <Loading />;

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.bg }} contentContainerStyle={{ padding: 16 }}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.accentBlue} />}>
      <View style={styles.header}>
        <Text style={[styles.title, { color: c.text }]}>Equipos</Text>
        <View style={{ flexDirection: 'row', gap: 8 }}>
          <Button title="Unirse" onPress={() => setShowJoin(true)} variant="secondary" style={{ width: 90 }} />
          <Button title="+ Crear" onPress={() => setShowCreate(true)} style={{ width: 90 }} />
        </View>
      </View>

      {teams.length === 0 ? (
        <EmptyState icon="👥" title="No estás en ningún equipo" subtitle="Crea o únete a un equipo" />
      ) : (
        teams.map(t => (
          <Card key={t.id}>
            <Text style={{ color: c.text, fontSize: 16, fontWeight: '600' }}>{t.name}</Text>
            <Text style={{ color: c.text2, fontSize: 13, marginTop: 4 }}>{t.members?.length || 0} miembros</Text>
            <Text style={{ color: c.text3, fontSize: 12, marginTop: 4 }}>Código: {t.invite_code}</Text>
          </Card>
        ))
      )}

      <Modal visible={showCreate} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalContent, { backgroundColor: c.bg2 }]}>
            <Text style={[styles.modalTitle, { color: c.text }]}>Crear equipo</Text>
            <TextInput style={[styles.input, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Nombre del equipo" placeholderTextColor={c.text3} value={teamName} onChangeText={setTeamName} />
            <View style={{ flexDirection: 'row', gap: 12 }}>
              <Button title="Cancelar" onPress={() => setShowCreate(false)} variant="secondary" style={{ flex: 1 }} />
              <Button title="Crear" onPress={createTeam} style={{ flex: 1 }} />
            </View>
          </View>
        </View>
      </Modal>

      <Modal visible={showJoin} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalContent, { backgroundColor: c.bg2 }]}>
            <Text style={[styles.modalTitle, { color: c.text }]}>Unirse a equipo</Text>
            <TextInput style={[styles.input, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Código de invitación" placeholderTextColor={c.text3} value={joinCode} onChangeText={setJoinCode} autoCapitalize="none" />
            <View style={{ flexDirection: 'row', gap: 12 }}>
              <Button title="Cancelar" onPress={() => setShowJoin(false)} variant="secondary" style={{ flex: 1 }} />
              <Button title="Unirse" onPress={joinTeam} style={{ flex: 1 }} />
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
  input: { height: 48, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1, marginBottom: 16 },
});
