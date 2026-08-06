import { createContext, useContext, useState, useCallback } from 'react';

const AuthContext = createContext(null);

const VALID_USERS = {
  admin: 'admin123',
  operator: 'operator123',
  viewer: 'viewer123',
};

const ROLE_LABELS = {
  admin: '系统管理员',
  operator: '操作员',
  viewer: '观察者',
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('etl_auth');
      return saved ? JSON.parse(saved) : null;
    } catch { return null; }
  });

  const login = useCallback((username, password) => {
    if (VALID_USERS[username] && VALID_USERS[username] === password) {
      const userData = { username, role: username, roleLabel: ROLE_LABELS[username], loggedAt: Date.now() };
      localStorage.setItem('etl_token', `nexus_${Date.now()}_${username}`);
      localStorage.setItem('etl_auth', JSON.stringify(userData));
      setUser(userData);
      return { success: true };
    }
    return { success: false, message: '用户名或密码错误' };
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('etl_token');
    localStorage.removeItem('etl_auth');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
