import React from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, StyleSheet } from 'react-native';
import { getColors } from '../theme/colors';
import { useAuth } from '../context/AuthContext';

export function Card({ children, style, onPress }) {
  const { theme } = useAuth();
  const c = getColors(theme);
  const content = <View style={[styles.card, { backgroundColor: c.bg2, borderColor: c.border }, style]}>{children}</View>;
  return onPress ? <TouchableOpacity onPress={onPress} activeOpacity={0.7}>{content}</TouchableOpacity> : content;
}

export function Button({ title, onPress, variant = 'primary', style, textStyle, loading, disabled, icon }) {
  const { theme } = useAuth();
  const c = getColors(theme);
  const isPrimary = variant === 'primary';
  const isDanger = variant === 'danger';

  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled || loading}
      activeOpacity={0.7}
      style={[
        styles.button,
        {
          backgroundColor: isPrimary ? c.accent : isDanger ? c.danger : c.surface2,
          borderWidth: isPrimary ? 0 : 1,
          borderColor: c.border,
          opacity: (disabled || loading) ? 0.5 : 1,
        },
        style,
      ]}
    >
      {loading ? (
        <ActivityIndicator color={isPrimary ? c.bg : c.text} size="small" />
      ) : (
        <View style={styles.buttonContent}>
          {icon}
          <Text style={[styles.buttonText, { color: isPrimary ? c.bg : c.text }, textStyle]}>{title}</Text>
        </View>
      )}
    </TouchableOpacity>
  );
}

export function Input({ label, value, onChangeText, placeholder, secureTextEntry, keyboardType, autoCapitalize, multiline, style }) {
  const { theme } = useAuth();
  const c = getColors(theme);
  return (
    <View style={styles.inputContainer}>
      {label && <Text style={[styles.label, { color: c.text2 }]}>{label}</Text>}
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={c.text3}
        secureTextEntry={secureTextEntry}
        keyboardType={keyboardType}
        autoCapitalize={autoCapitalize}
        multiline={multiline}
        style={[styles.input, { backgroundColor: c.surface2, color: c.text, borderColor: c.border }, style]}
      />
    </View>
  );
}

export function Loading({ text = 'Cargando...' }) {
  const { theme } = useAuth();
  const c = getColors(theme);
  return (
    <View style={styles.loadingContainer}>
      <ActivityIndicator size="large" color={c.accentBlue} />
      <Text style={[styles.loadingText, { color: c.text2 }]}>{text}</Text>
    </View>
  );
}

export function EmptyState({ icon = '📋', title, subtitle }) {
  const { theme } = useAuth();
  const c = getColors(theme);
  return (
    <View style={styles.emptyContainer}>
      <Text style={styles.emptyIcon}>{icon}</Text>
      <Text style={[styles.emptyTitle, { color: c.text }]}>{title}</Text>
      {subtitle && <Text style={[styles.emptySubtitle, { color: c.text2 }]}>{subtitle}</Text>}
    </View>
  );
}

export function Badge({ text, color }) {
  const { theme } = useAuth();
  const c = getColors(theme);
  return (
    <View style={[styles.badge, { backgroundColor: color || c.surface3 }]}>
      <Text style={[styles.badgeText, { color: c.text }]}>{text}</Text>
    </View>
  );
}

export function SectionTitle({ title, action, onAction }) {
  const { theme } = useAuth();
  const c = getColors(theme);
  return (
    <View style={styles.sectionHeader}>
      <Text style={[styles.sectionTitleText, { color: c.text }]}>{title}</Text>
      {action && (
        <TouchableOpacity onPress={onAction}>
          <Text style={{ color: c.accentBlue, fontSize: 13 }}>{action}</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 10,
    padding: 16,
    borderWidth: 1,
    marginBottom: 12,
  },
  button: {
    height: 48,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
  },
  buttonContent: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  buttonText: {
    fontSize: 15,
    fontWeight: '600',
  },
  inputContainer: {
    marginBottom: 12,
  },
  label: {
    fontSize: 13,
    fontWeight: '500',
    marginBottom: 6,
  },
  input: {
    height: 48,
    borderRadius: 10,
    paddingHorizontal: 14,
    fontSize: 15,
    borderWidth: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 40,
  },
  loadingText: {
    marginTop: 12,
    fontSize: 14,
  },
  emptyContainer: {
    alignItems: 'center',
    paddingVertical: 40,
  },
  emptyIcon: {
    fontSize: 48,
    marginBottom: 12,
  },
  emptyTitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 4,
  },
  emptySubtitle: {
    fontSize: 14,
    textAlign: 'center',
  },
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '600',
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  sectionTitleText: {
    fontSize: 16,
    fontWeight: '700',
  },
});
