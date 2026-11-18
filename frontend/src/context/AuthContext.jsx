import { createContext, useContext, useMemo, useState } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);
function readSavedUser() {
  try { return JSON.parse(sessionStorage.getItem('echo.user') || 'null'); }
  catch { sessionStorage.removeItem('echo.user'); sessionStorage.removeItem('echo.token'); return null; }
}
export function AuthProvider({ children }) {
  const [user, setUser] = useState(readSavedUser);
  function accept(data) {
    sessionStorage.setItem('echo.token', data.token);
    sessionStorage.setItem('echo.user', JSON.stringify(data.user));
    setUser(data.user);
  }
  async function login(credentials) { const { data } = await api.post('/auth/login', credentials); accept(data); return data.user; }
  async function register(form) { const { data } = await api.post('/auth/register', form); accept(data); return data.user; }
  function logout() { sessionStorage.removeItem('echo.token'); sessionStorage.removeItem('echo.user'); setUser(null); }
  function updateUser(profile) { const updated = { ...user, ...profile }; sessionStorage.setItem('echo.user', JSON.stringify(updated)); setUser(updated); }
  const value = useMemo(() => ({ user, login, register, logout, updateUser }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export const useAuth = () => useContext(AuthContext);
