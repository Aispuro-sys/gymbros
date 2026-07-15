import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, StyleSheet, RefreshControl } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Card, Loading, SectionTitle } from '../components/UI';

export default function OverviewScreen({ navigation }) {
  const { user, theme } = useAuth();
  const c = getColors(theme);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      const [macros, routines, supplements] = await Promise.all([
        apiCall('/macros/today').catch(() => null),
        apiCall('/routines').catch(() => ({ routines: [] })),
        apiCall('/supplements').catch(() => ({ supplements: [] })),
      ]);
      setData({ macros, routines: routines.routines || [], supplements: supplements.supplements || [] });
    } catch (e) { console.error(e); }
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  const onRefresh = () => { setRefreshing(true); load(); };

  if (loading) return <Loading />;

  const today = new Date().toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' });
  const cal = data?.macros?.calories || 0;
  const protein = data?.macros?.protein_g || 0;

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.bg }} contentContainerStyle={{ padding: 16 }}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.accentBlue} />}>
      <Text style={[styles.title, { color: c.text }]}>Resumen</Text>
      <Text style={[styles.date, { color: c.text2 }]}>{today}</Text>

      <View style={styles.statsRow}>
        <Card style={{ flex: 1, marginRight: 8, alignItems: 'center' }}>
          <Text style={[styles.statValue, { color: c.text }]}>{cal}</Text>
          <Text style={[styles.statLabel, { color: c.text2 }]}>Calorías</Text>
        </Card>
        <Card style={{ flex: 1, marginRight: 8, alignItems: 'center' }}>
          <Text style={[styles.statValue, { color: c.text }]}>{protein}g</Text>
          <Text style={[styles.statLabel, { color: c.text2 }]}>Proteína</Text>
        </Card>
        <Card style={{ flex: 1, alignItems: 'center' }}>
          <Text style={[styles.statValue, { color: c.text }]}>{data?.routines?.length || 0}</Text>
          <Text style={[styles.statLabel, { color: c.text2 }]}>Rutinas</Text>
        </Card>
      </View>

      <Card>
        <SectionTitle title="Rutina reciente" action="Ver todas" onAction={() => navigation.navigate('Routines')} />
        {data?.routines?.[0] ? (
          <View>
            <Text style={{ color: c.text, fontSize: 15, fontWeight: '600' }}>{data.routines[0].name}</Text>
            <Text style={{ color: c.text2, fontSize: 13, marginTop: 4 }}>{data.routines[0].exercises?.length || 0} ejercicios</Text>
          </View>
        ) : <Text style={{ color: c.text2, fontSize: 14 }}>No hay rutinas. Crea una desde Rutinas.</Text>}
      </Card>

      <Card>
        <SectionTitle title="Macros de hoy" action="Registrar" onAction={() => navigation.navigate('Nutrition')} />
        <Text style={{ color: c.text2, fontSize: 14 }}>{cal} calorías · {protein}g proteína</Text>
      </Card>

      <Card>
        <SectionTitle title="Suplementos de hoy" action="Gestionar" onAction={() => navigation.navigate('Supplements')} />
        {data?.supplements?.length > 0 ? data.supplements.map(s => (
          <View key={s.id} style={{ flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 6 }}>
            <Text style={{ color: c.text, fontSize: 14 }}>{s.name}</Text>
            <Text style={{ color: c.text2, fontSize: 13 }}>{s.dosage}</Text>
          </View>
        )) : <Text style={{ color: c.text2, fontSize: 14 }}>No hay suplementos registrados.</Text>}
      </Card>

      <Card>
        <SectionTitle title="IA Coach" action="Abrir" onAction={() => navigation.navigate('AICoach')} />
        <Text style={{ color: c.text2, fontSize: 14 }}>Genera rutinas y planes nutricionales personalizados con IA.</Text>
      </Card>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  title: { fontSize: 28, fontWeight: '800', marginBottom: 2 },
  date: { fontSize: 14, marginBottom: 16, textTransform: 'capitalize' },
  statsRow: { flexDirection: 'row', marginBottom: 12 },
  statValue: { fontSize: 24, fontWeight: '700' },
  statLabel: { fontSize: 12, marginTop: 2 },
});
