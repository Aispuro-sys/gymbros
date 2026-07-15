export const colors = {
  dark: {
    bg: '#000000',
    bg2: '#0a0a0a',
    surface: 'rgba(255,255,255,0.03)',
    surface2: 'rgba(255,255,255,0.06)',
    surface3: 'rgba(255,255,255,0.09)',
    border: 'rgba(255,255,255,0.08)',
    border2: 'rgba(255,255,255,0.14)',
    text: '#ffffff',
    text2: '#a0a0a0',
    text3: '#606060',
    accent: '#ffffff',
    accentBlue: '#1976d2',
    danger: '#ff4444',
    radius: 10,
    radiusSm: 6,
  },
  light: {
    bg: '#f7f7f8',
    bg2: '#ffffff',
    surface: 'rgba(0,0,0,0.025)',
    surface2: 'rgba(0,0,0,0.05)',
    surface3: 'rgba(0,0,0,0.08)',
    border: 'rgba(0,0,0,0.08)',
    border2: 'rgba(0,0,0,0.15)',
    text: '#111111',
    text2: '#555555',
    text3: '#999999',
    accent: '#111111',
    accentBlue: '#1976d2',
    danger: '#d32f2f',
    radius: 10,
    radiusSm: 6,
  },
};

export function getColors(theme) {
  return colors[theme] || colors.dark;
}
