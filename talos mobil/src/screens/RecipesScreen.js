import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, RefreshControl, Image, Modal, TextInput, Alert } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Card, Button, Loading, EmptyState, Badge } from '../components/UI';

export default function RecipesScreen() {
  const { theme } = useAuth();
  const c = getColors(theme);
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [mealFilter, setMealFilter] = useState('ANY');
  const [selected, setSelected] = useState(null);
  const [showModal, setShowModal] = useState(false);

  const load = useCallback(async () => {
    try {
      const params = new URLSearchParams();
      if (search) params.set('search', search);
      if (mealFilter !== 'ANY') params.set('meal_type', mealFilter);
      const data = await apiCall(`/recipes?${params.toString()}`);
      setRecipes(data.recipes || []);
    } catch (e) { console.error(e); }
    setLoading(false);
    setRefreshing(false);
  }, [search, mealFilter]);

  useEffect(() => { load(); }, [load]);

  const onRefresh = () => { setRefreshing(true); load(); };

  const openRecipe = (r) => { setSelected(r); setShowModal(true); };

  const addToMeals = async (id) => {
    try { await apiCall('/meals', 'POST', { recipe_id: id }); Alert.alert('✅', 'Agregada a comidas de hoy'); }
    catch (e) { Alert.alert('Error', e.message); }
  };

  if (loading) return <Loading />;

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <ScrollView contentContainerStyle={{ padding: 16 }}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.accentBlue} />}>
        <Text style={[styles.title, { color: c.text }]}>Recetas Fitness</Text>
        <Text style={{ color: c.text2, fontSize: 13, marginBottom: 12 }}>{recipes.length} recetas</Text>

        <TextInput style={[styles.search, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Buscar receta..." placeholderTextColor={c.text3} value={search} onChangeText={setSearch} />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginBottom: 12 }}>
          {['ANY', 'BREAKFAST', 'LUNCH', 'DINNER', 'SNACK', 'POST_WORKOUT'].map(t => (
            <TouchableOpacity key={t} onPress={() => setMealFilter(t)} style={[styles.filterBtn, { backgroundColor: mealFilter === t ? c.accentBlue : c.surface2 }]}>
              <Text style={{ color: mealFilter === t ? '#fff' : c.text2, fontSize: 12 }}>{t === 'ANY' ? 'Todas' : t === 'BREAKFAST' ? 'Desayuno' : t === 'LUNCH' ? 'Comida' : t === 'DINNER' ? 'Cena' : t === 'SNACK' ? 'Snack' : 'Post-entreno'}</Text>
            </TouchableOpacity>
          ))}
        </ScrollView>

        {recipes.length === 0 ? (
          <EmptyState icon="🥗" title="No se encontraron recetas" />
        ) : (
          recipes.map(r => (
            <TouchableOpacity key={r.id} onPress={() => openRecipe(r)} activeOpacity={0.7}>
              <Card>
                <View style={{ flexDirection: 'row', gap: 12 }}>
                  {r.image_url && <Image source={{ uri: r.image_url }} style={styles.recipeImg} resizeMode="cover" />}
                  <View style={{ flex: 1 }}>
                    <Text style={{ color: c.text, fontSize: 15, fontWeight: '600' }}>{r.name}</Text>
                    <Text style={{ color: c.text2, fontSize: 13, marginTop: 2 }}>{r.calories} cal · {r.protein_g}g prot</Text>
                    <Text style={{ color: c.text3, fontSize: 12, marginTop: 2 }}>{r.prep_time_min} min · {r.servings} porciones</Text>
                  </View>
                </View>
              </Card>
            </TouchableOpacity>
          ))
        )}
      </ScrollView>

      <Modal visible={showModal} animationType="slide">
        <View style={{ flex: 1, backgroundColor: c.bg }}>
          <ScrollView contentContainerStyle={{ padding: 16 }}>
            <View style={styles.modalHeader}>
              <Text style={{ color: c.text, fontSize: 22, fontWeight: '700', flex: 1 }}>{selected?.name}</Text>
              <TouchableOpacity onPress={() => setShowModal(false)}>
                <Text style={{ color: c.text2, fontSize: 28 }}>×</Text>
              </TouchableOpacity>
            </View>
            {selected?.image_url && <Image source={{ uri: selected.image_url }} style={styles.detailImg} resizeMode="cover" />}
            <Text style={{ color: c.text2, fontSize: 14, marginBottom: 16 }}>{selected?.calories} cal · {selected?.protein_g}g prot · {selected?.prep_time_min} min</Text>

            <Text style={{ color: c.text, fontSize: 16, fontWeight: '700', marginBottom: 8 }}>Ingredientes</Text>
            {selected?.ingredients?.map((ing, i) => (
              <Text key={i} style={{ color: c.text2, fontSize: 14, paddingVertical: 3 }}>• {ing}</Text>
            ))}

            <Text style={{ color: c.text, fontSize: 16, fontWeight: '700', marginTop: 16, marginBottom: 8 }}>Instrucciones</Text>
            {selected?.instructions?.map((ins, i) => (
              <Text key={i} style={{ color: c.text2, fontSize: 14, paddingVertical: 3 }}>{i + 1}. {ins}</Text>
            ))}

            <Button title="Agregar a comidas de hoy" onPress={() => addToMeals(selected?.id)} style={{ marginTop: 20 }} />
          </ScrollView>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  title: { fontSize: 28, fontWeight: '800', marginBottom: 2 },
  search: { height: 44, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1, marginBottom: 8 },
  filterBtn: { paddingVertical: 6, paddingHorizontal: 12, borderRadius: 8, marginRight: 8 },
  recipeImg: { width: 60, height: 60, borderRadius: 8 },
  detailImg: { width: '100%', height: 200, borderRadius: 12, marginBottom: 16 },
  modalHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 16 },
});
