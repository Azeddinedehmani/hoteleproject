import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import authService from '../services/authService';

// ─── Création du contexte ───────────────────────────────────────────────────
const AuthContext = createContext(null);

// ─── Provider ──────────────────────────────────────────────────────────────
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);       // { id, name, email, role }
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true); // vérification initiale du token

  // Au montage : restaurer la session depuis localStorage, puis vérifier côté serveur
  useEffect(() => {
    const storedToken = localStorage.getItem('hotel_token');
    const storedUser  = localStorage.getItem('hotel_user');

    if (!storedToken || !storedUser) {
      setLoading(false);
      return;
    }

    let parsedUser = null;
    try {
      parsedUser = JSON.parse(storedUser);
    } catch {
      // JSON corrompu → nettoyage immédiat
      localStorage.removeItem('hotel_token');
      localStorage.removeItem('hotel_user');
      setLoading(false);
      return;
    }

    // Injecter le token en mémoire pour que la requête me() soit authentifiée
    authService.setAuthHeader(storedToken);

    // Vérification live du token côté serveur :
    // si le token est expiré ou révoqué, le backend répond 401 → logout propre.
    authService.me()
      .then((freshUser) => {
        // Le backend peut avoir mis à jour le rôle ou le clientId depuis la dernière session
        const merged = { ...parsedUser, ...freshUser };
        localStorage.setItem('hotel_user', JSON.stringify(merged));
        setToken(storedToken);
        setUser(merged);
      })
      .catch(() => {
        // Token invalide / expiré → on purge la session et on redirige vers /login
        localStorage.removeItem('hotel_token');
        localStorage.removeItem('hotel_user');
        authService.removeAuthHeader();
        setToken(null);
        setUser(null);
        if (!['/login', '/register'].includes(window.location.pathname)) {
          window.location.href = '/login';
        }
      })
      .finally(() => setLoading(false));
  }, []); // eslint-disable-line

  // ── Login ─────────────────────────────────────────────────────────────────
  const login = useCallback(async (credentials) => {
    const data = await authService.login(credentials);
    // data = { token, user: { id, name, email, role } }
    localStorage.setItem('hotel_token', data.token);
    localStorage.setItem('hotel_user',  JSON.stringify(data.user));
    authService.setAuthHeader(data.token);
    setToken(data.token);
    setUser(data.user);
    return data.user;
  }, []);

  // ── Register ──────────────────────────────────────────────────────────────
  const register = useCallback(async (userData) => {
    const data = await authService.register(userData);
    localStorage.setItem('hotel_token', data.token);
    localStorage.setItem('hotel_user',  JSON.stringify(data.user));
    authService.setAuthHeader(data.token);
    setToken(data.token);
    setUser(data.user);
    return data.user;
  }, []);

  // ── Logout ────────────────────────────────────────────────────────────────
  const logout = useCallback(() => {
    localStorage.removeItem('hotel_token');
    localStorage.removeItem('hotel_user');
    authService.removeAuthHeader();
    setToken(null);
    setUser(null);
  }, []);

  // ── Helpers rôles ─────────────────────────────────────────────────────────
  const isAdmin     = user?.role === 'admin';
  const isReceptionist = user?.role === 'receptionist';
  const isClient    = user?.role === 'client';
  const isAuthenticated = !!token;

  const value = {
    user,
    token,
    loading,
    isAuthenticated,
    isAdmin,
    isReceptionist,
    isClient,
    login,
    register,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// ─── Hook personnalisé ──────────────────────────────────────────────────────
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth doit être utilisé dans un <AuthProvider>');
  }
  return context;
};

export default AuthContext;