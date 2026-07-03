import { createContext, useContext, useState, useCallback } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback(async (email, password) => {
    const res = await api.login(email, password);
    persist(res);
    return res;
  }, []);

  const register = useCallback(async (email, password) => {
    const res = await api.register(email, password);
    persist(res);
    return res;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  function persist(authResponse) {
    const { token, userId, email, role } = authResponse;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify({ userId, email, role }));
    setUser({ userId, email, role });
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
