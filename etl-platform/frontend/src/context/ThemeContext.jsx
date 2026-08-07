import { createContext, useContext, useEffect, useState, useCallback } from 'react';

const ThemeContext = createContext(null);

export const ACCENTS = [
  { key: 'indigo', label: '靛蓝', color: '#6366f1' },
  { key: 'blue', label: '海蓝', color: '#2563eb' },
  { key: 'cyan', label: '青绿', color: '#0891b2' },
  { key: 'violet', label: '紫罗兰', color: '#7c3aed' },
  { key: 'emerald', label: '翡翠', color: '#059669' },
  { key: 'rose', label: '玫瑰', color: '#e11d48' },
  { key: 'amber', label: '琥珀', color: '#d97706' },
];

const THEME_STORAGE_KEY = 'etl_theme';
const ACCENT_STORAGE_KEY = 'etl_accent';

function getInitialTheme() {
  try {
    const saved = localStorage.getItem(THEME_STORAGE_KEY);
    if (saved === 'light' || saved === 'dark') return saved;
  } catch {}
  return 'light';
}

function getInitialAccent() {
  try {
    const saved = localStorage.getItem(ACCENT_STORAGE_KEY);
    if (saved && ACCENTS.some(a => a.key === saved)) return saved;
  } catch {}
  return 'indigo';
}

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(getInitialTheme);
  const [accent, setAccent] = useState(getInitialAccent);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    document.documentElement.setAttribute('data-accent', accent);
    try {
      localStorage.setItem(THEME_STORAGE_KEY, theme);
      localStorage.setItem(ACCENT_STORAGE_KEY, accent);
    } catch {}
  }, [theme, accent]);

  const toggleTheme = useCallback(() => {
    setTheme(t => (t === 'light' ? 'dark' : 'light'));
  }, []);

  return (
    <ThemeContext.Provider value={{ theme, accent, setTheme, setAccent, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be used within ThemeProvider');
  return ctx;
}
