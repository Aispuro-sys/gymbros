import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TextInput, TouchableOpacity, StyleSheet, RefreshControl, Image } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Loading, EmptyState } from '../components/UI';

export default function ExercisesScreen() {
  const { theme } = useAuth();
  const c = getColors(theme);
  const [exercises, setExercises] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [categories, setCategories] = useState([]);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      const params = new URLSearchParams();
      if (search) params.set('search', search);
      if (category) params.set('category', category);
      const data = await apiCall(`/exercises?${params.toString()}`);
      setExercises(data.exercises || []);
      if (data.categories) setCategories(data.categories);
    } catch (e) { console.error(e); }
    setLoading(false);
    setRefreshing(false);
  }, [search, category]);

  useEffect(() => { load(); }, [load]);

  const onRefresh = () => { setRefreshing(true); load(); };

  if (loading) return <Loading />;

  const renderItem = ({ item }) => (
    <View style={[styles.card, { backgroundColor: c.bg2, borderColor: c.border }]}>
      {item.gif_url && <Image source={{ uri: item.gif_url }} style={styles.gif} resizeMode="contain" />}
      <View style={{ flex: 1 }}>
        <Text style={{ color: c.text, fontSize: 15, fontWeight: '600' }}>{item.name}</Text>
        <Text style={{ color: c.text2, fontSize: 13, marginTop: 2 }}>{item.category} · {item.equipment}</Text>
      </View>
    </View>
  );

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <View style={{ padding: 16 }}>
        <TextInput style={[styles.searchInput, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Buscar ejercicio..." placeholderTextColor={c.text3} value={search} onChangeText={setSearch} />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginTop: 8 }}>
          <TouchableOpacity onPress={() => setCategory('')} style={[styles.catBtn, { backgroundColor: !category ? c.accentBlue : c.surface2 }]}>
            <Text style={{ color: !category ? '#fff' : c.text2, fontSize: 12 }}>Todas</Text>
          </TouchableOpacity>
          {categories.map(cat => (
            <TouchableOpacity key={cat} onPress={() => setCategory(cat)} style={[styles.catBtn, { backgroundColor: category === cat ? c.accentBlue : c.surface2 }]}>
              <Text style={{ color: category === cat ? '#fff' : c.text2, fontSize: 12 }}>{cat}</Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>
      <FlatList
        data={exercises}
        keyExtractor={item => item.id?.toString() || item.name}
        renderItem={renderItem}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 16 }}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.accentBlue} />}
        ListEmptyComponent={<EmptyState icon="💪" title="No se encontraron ejercicios" />}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  card: { flexDirection: 'row', alignItems: 'center', padding: 12, borderRadius: 10, borderWidth: 1, marginBottom: 8, gap: 12 },
  gif: { width: 60, height: 60, borderRadius: 8 },
  searchInput: { height: 44, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1 },
  catBtn: { paddingVertical: 6, paddingHorizontal: 12, borderRadius: 8, marginRight: 8 },
});
