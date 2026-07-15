import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, RefreshControl, Alert, Share, Clipboard } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall, API_URL as apiBaseUrl } from '../api/client';
import { Card, Button, Loading, EmptyState } from '../components/UI';

export default function ShoppingListScreen() {
  const { theme } = useAuth();
  const c = getColors(theme);
  const [list, setList] = useState(null);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      const data = await apiCall('/shopping-list');
      setList(data.list);
      if (data.list?.items) {
        setItems(data.list.items.map(item => ({
          id: item.id,
          name: item.name,
          quantity: item.quantity,
          checked: item.checked,
          recipe_names: item.recipe_names || [],
        })));
      }
    } catch (e) { console.error(e); }
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  const onRefresh = () => { setRefreshing(true); load(); };

  const toggleItem = async (id, checked) => {
    try {
      await apiCall(`/shopping-list/${list.id}/items/${id}`, 'PUT', { checked: !checked });
      setItems(prev => prev.map(i => i.id === id ? { ...i, checked: !checked } : i));
    } catch (e) { Alert.alert('Error', e.message); }
  };

  const generateFromMeals = async () => {
    try {
      const data = await apiCall('/recipes/shopping-list-from-meals', 'POST', {});
      if (data.message) { Alert.alert('Aviso', 'No hay comidas registradas hoy'); return; }
      await apiCall('/shopping-list', 'POST', {
        items: (data.ingredients || []).map(item => ({
          name: item.name, quantity: item.quantity || null, checked: false, recipe_names: item.recipes || [],
        })),
      });
      load();
    } catch (e) { Alert.alert('Error', e.message); }
  };

  const generateFromAI = async () => {
    try {
      const data = await apiCall('/ai/recommend-recipes', 'POST', {});
      if (data.recipes?.length > 0) {
        const ids = data.recipes.map(r => r.id);
        const ingData = await apiCall('/recipes/shopping-list', 'POST', { recipeIds: ids });
        await apiCall('/shopping-list', 'POST', {
          items: (ingData.ingredients || []).map(item => ({
            name: item.name, quantity: item.quantity || null, checked: false, recipe_names: item.recipes || [],
          })),
        });
        load();
      } else { Alert.alert('Aviso', 'No hay recetas recomendadas'); }
    } catch (e) { Alert.alert('Error', e.message); }
  };

  const shareList = async () => {
    if (!list) { Alert.alert('Aviso', 'No hay lista para compartir'); return; }
    try {
      const shareRes = await apiCall(`/shopping-list/${list.id}/share`, 'POST', {});
      const url = `${apiBaseUrl}${shareRes.shareUrl}`;
      await Share.share({ title: 'Lista de Supermercado - Talos Forge', message: `Ayúdame con las compras 🛒\n${url}` });
    } catch (e) { Alert.alert('Error', e.message); }
  };

  const clearList = async () => {
    Alert.alert('Confirmar', '¿Eliminar toda la lista?', [
      { text: 'Cancelar', style: 'cancel' },
      { text: 'Eliminar', style: 'destructive', onPress: async () => {
        try { await apiCall('/shopping-list', 'POST', { items: [] }); load(); } catch (e) { Alert.alert('Error', e.message); }
      }},
    ]);
  };

  if (loading) return <Loading />;

  const totalItems = items.length;
  const checkedItems = items.filter(i => i.checked).length;
  const progress = totalItems > 0 ? Math.round((checkedItems / totalItems) * 100) : 0;

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.bg }} contentContainerStyle={{ padding: 16 }}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.accentBlue} />}>
      <Text style={[styles.title, { color: c.text }]}>🛒 Lista de Super</Text>
      <Text style={{ color: c.text2, fontSize: 14, marginBottom: 16 }}>Tu lista de compras en vivo</Text>

      {totalItems > 0 ? (
        <>
          <Card style={{ backgroundColor: c.accentBlue, borderColor: 'transparent' }}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
              <View style={styles.progressCircle}>
                <Text style={{ color: '#fff', fontSize: 14, fontWeight: '700' }}>{progress}%</Text>
              </View>
              <View style={{ flex: 1 }}>
                <Text style={{ color: '#fff', fontSize: 16, fontWeight: '700' }}>{list?.name || 'Lista'}</Text>
                <Text style={{ color: 'rgba(255,255,255,0.8)', fontSize: 13 }}>{checkedItems} de {totalItems} comprados</Text>
              </View>
            </View>
          </Card>

          <View style={styles.actionsRow}>
            <Button title="🍽️ Comidas" onPress={generateFromMeals} variant="secondary" style={{ flex: 1 }} />
            <Button title="🤖 IA" onPress={generateFromAI} variant="secondary" style={{ flex: 1 }} />
          </View>
          <View style={styles.actionsRow}>
            <Button title="🔗 Compartir" onPress={shareList} variant="secondary" style={{ flex: 1 }} />
            <Button title="🗑️ Limpiar" onPress={clearList} variant="danger" style={{ flex: 1 }} />
          </View>

          <Card>
            <Text style={{ color: c.text, fontSize: 16, fontWeight: '700', marginBottom: 12 }}>🛍️ Ingredientes</Text>
            {items.map((item, idx) => (
              <TouchableOpacity key={item.id || idx} onPress={() => toggleItem(item.id, item.checked)} style={styles.itemRow}>
                <View style={[styles.checkbox, { borderColor: c.border, backgroundColor: item.checked ? c.accentBlue : 'transparent' }]}>
                  {item.checked && <Text style={{ color: '#fff', fontSize: 14 }}>✓</Text>}
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={{ color: item.checked ? c.text3 : c.text, fontSize: 15, textDecorationLine: item.checked ? 'line-through' : 'none' }}>{item.name}</Text>
                  {item.recipe_names?.length > 1 && <Text style={{ color: c.text3, fontSize: 12 }}>📦 En {item.recipe_names.length} receta(s)</Text>}
                </View>
                {item.quantity && <Text style={{ color: c.text2, fontSize: 13, backgroundColor: c.surface, paddingHorizontal: 8, paddingVertical: 3, borderRadius: 6 }}>{item.quantity}</Text>}
                {item.checked && <Text style={{ fontSize: 16 }}>✅</Text>}
              </TouchableOpacity>
            ))}
          </Card>
        </>
      ) : (
        <Card style={{ alignItems: 'center', paddingVertical: 40 }}>
          <Text style={{ fontSize: 48, marginBottom: 12 }}>🛒</Text>
          <Text style={{ color: c.text, fontSize: 18, fontWeight: '700', marginBottom: 8 }}>Lista de Supermercado</Text>
          <Text style={{ color: c.text2, fontSize: 14, textAlign: 'center', marginBottom: 20 }}>Crea tu lista de compras usando tus comidas de hoy o dejando que la IA te recomiende</Text>
          <Button title="🍽️ Desde comidas de hoy" onPress={generateFromMeals} style={{ marginBottom: 10, width: 260 }} />
          <Button title="🤖 Recomendar con IA" onPress={generateFromAI} variant="secondary" style={{ width: 260 }} />
        </Card>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  title: { fontSize: 28, fontWeight: '800', marginBottom: 2 },
  progressCircle: { width: 56, height: 56, borderRadius: 28, backgroundColor: 'rgba(255,255,255,0.15)', justifyContent: 'center', alignItems: 'center' },
  actionsRow: { flexDirection: 'row', gap: 8, marginBottom: 8 },
  itemRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: 'rgba(255,255,255,0.05)' },
  checkbox: { width: 22, height: 22, borderRadius: 6, borderWidth: 2, justifyContent: 'center', alignItems: 'center' },
});
