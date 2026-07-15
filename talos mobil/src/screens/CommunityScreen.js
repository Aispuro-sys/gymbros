import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, TextInput, StyleSheet, RefreshControl, Image, Alert } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getColors } from '../theme/colors';
import { apiCall } from '../api/client';
import { Card, Button, Loading, EmptyState } from '../components/UI';

export default function CommunityScreen() {
  const { user, theme } = useAuth();
  const c = getColors(theme);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [postText, setPostText] = useState('');
  const [userSearch, setUserSearch] = useState('');
  const [userResults, setUserResults] = useState([]);

  const load = useCallback(async () => {
    try {
      const data = await apiCall('/community/feed');
      setPosts(data.posts || []);
    } catch (e) { console.error(e); }
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  const onRefresh = () => { setRefreshing(true); load(); };

  const post = async () => {
    if (!postText.trim()) return;
    try { await apiCall('/community/posts', 'POST', { content: postText }); setPostText(''); load(); }
    catch (e) { Alert.alert('Error', e.message); }
  };

  const searchUsers = async (query) => {
    setUserSearch(query);
    if (!query.trim()) { setUserResults([]); return; }
    try {
      const data = await apiCall(`/community/search-users?q=${encodeURIComponent(query)}`);
      setUserResults(data.users || []);
    } catch (e) { console.error(e); }
  };

  const reactToPost = async (postId, emoji) => {
    try { await apiCall('/community/react', 'POST', { post_id: postId, emoji }); load(); }
    catch (e) { console.error(e); }
  };

  if (loading) return <Loading />;

  const roleLabels = { NORMAL: 'Usuario', MODERATOR: 'Moderador', ATHLETE: 'Atleta', ADMIN: 'Admin' };

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.bg }} contentContainerStyle={{ padding: 16 }}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.accentBlue} />}>
      <Text style={[styles.title, { color: c.text }]}>Comunidad</Text>
      <Text style={{ color: c.text2, fontSize: 14, marginBottom: 16 }}>Comparte, motiva y conecta</Text>

      <Card>
        <Text style={{ color: c.text, fontSize: 14, fontWeight: '600', marginBottom: 8 }}>Buscar usuarios</Text>
        <TextInput style={[styles.search, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="Buscar por username..." placeholderTextColor={c.text3} value={userSearch} onChangeText={searchUsers} />
        {userResults.map(u => (
          <View key={u.id} style={{ flexDirection: 'row', alignItems: 'center', paddingVertical: 8, borderTopWidth: 1, borderTopColor: c.border }}>
            <View style={[styles.avatar, { backgroundColor: c.accentBlue }]}><Text style={{ color: '#fff', fontWeight: '700' }}>{u.username[0]?.toUpperCase()}</Text></View>
            <Text style={{ color: c.text, fontSize: 14 }}>{u.username}</Text>
          </View>
        ))}
      </Card>

      <Card>
        <TextInput style={[styles.postInput, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }]} placeholder="¿Qué estás logrando hoy?" placeholderTextColor={c.text3} value={postText} onChangeText={setPostText} multiline />
        <Button title="Publicar" onPress={post} style={{ marginTop: 8 }} />
      </Card>

      {posts.length === 0 ? (
        <EmptyState icon="💬" title="No hay publicaciones" subtitle="Sé el primero en publicar" />
      ) : (
        posts.map(p => (
          <Card key={p.id}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 8 }}>
              <View style={[styles.avatar, { backgroundColor: c.accentBlue }]}>
                <Text style={{ color: '#fff', fontWeight: '700' }}>{p.user?.username?.[0]?.toUpperCase() || 'U'}</Text>
              </View>
              <View>
                <Text style={{ color: c.text, fontSize: 14, fontWeight: '600' }}>{p.user?.username}</Text>
                <Text style={{ color: c.text3, fontSize: 11 }}>{roleLabels[p.user?.role] || 'Usuario'} · {new Date(p.created_at).toLocaleDateString('es-ES')}</Text>
              </View>
            </View>
            <Text style={{ color: c.text, fontSize: 14 }}>{p.content}</Text>
            {p.media_url && <Image source={{ uri: p.media_url }} style={styles.media} resizeMode="cover" />}
            <View style={{ flexDirection: 'row', gap: 12, marginTop: 8 }}>
              {['🔥', '💪', '❤️', '👏'].map(emoji => (
                <TouchableOpacity key={emoji} onPress={() => reactToPost(p.id, emoji)}>
                  <Text style={{ fontSize: 20 }}>{emoji}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </Card>
        ))
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  title: { fontSize: 28, fontWeight: '800', marginBottom: 2 },
  search: { height: 44, borderRadius: 10, paddingHorizontal: 14, fontSize: 15, borderWidth: 1 },
  postInput: { minHeight: 80, borderRadius: 10, paddingHorizontal: 14, paddingVertical: 10, fontSize: 15, borderWidth: 1 },
  avatar: { width: 32, height: 32, borderRadius: 16, justifyContent: 'center', alignItems: 'center' },
  media: { width: '100%', height: 200, borderRadius: 10, marginTop: 8 },
});
