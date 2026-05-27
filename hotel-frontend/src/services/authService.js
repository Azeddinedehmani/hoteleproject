import axios from 'axios';

// ── Instance Axios centrale ──────────────────────────────────────────────────
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('hotel_token');
    if (token && !config.headers['Authorization']) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('hotel_token');
      localStorage.removeItem('hotel_user');
      delete api.defaults.headers.common['Authorization'];
      if (!['/login', '/register'].includes(window.location.pathname)) {
        window.location.href = '/login';
      }
    }
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      'Une erreur est survenue';
    return Promise.reject(new Error(message));
  }
);

const normalizeRole = (role) => {
  if (!role) return 'client';
  const map = {
    ADMIN: 'admin',
    RECEPTIONNISTE: 'receptionist',
    CLIENT: 'client',
  };
  return map[role.toUpperCase()] ?? role.toLowerCase();
};

/**
 * FIX : extractAuth inclut désormais clientId dans l'objet user.
 * Le backend renvoie UserResponse.clientId (non null si rôle CLIENT).
 * Ce champ est stocké dans localStorage et utilisé par BookingPage
 * pour créer une réservation avec le bon identifiant client.
 */
const extractAuth = (apiResponse) => {
  const data = apiResponse.data?.data ?? apiResponse.data;
  const token = data?.token ?? data?.accessToken;
  const rawUser = data?.user;

  if (!token || !rawUser) throw new Error('Réponse serveur invalide');

  const user = {
    id:       rawUser.id,
    name:     rawUser.name,
    email:    rawUser.email,
    role:     normalizeRole(rawUser.role),
    active:   rawUser.active,
    clientId: rawUser.clientId ?? null,   // ← AJOUT : id dans la table clients
  };

  return { token, user };
};

const authService = {
  login: async ({ email, password }) => {
    const response = await api.post('/auth/login', { email, password });
    return extractAuth(response);
  },

  register: async ({ name, email, phone, password, role = 'CLIENT' }) => {
    const response = await api.post('/auth/register', { name, email, phone, password, role });
    return extractAuth(response);
  },

  /**
   * FIX : /auth/me retourne aussi UserResponse (avec clientId si CLIENT).
   * On normalise de la même façon.
   */
  me: async () => {
    const response = await api.get('/auth/me');
    const rawUser = response.data?.data ?? response.data;
    return {
      id:       rawUser.id,
      name:     rawUser.name,
      email:    rawUser.email,
      role:     normalizeRole(rawUser.role),
      active:   rawUser.active,
      clientId: rawUser.clientId ?? null,  // ← AJOUT
    };
  },

  setAuthHeader: (token) => {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  },

  removeAuthHeader: () => {
    delete api.defaults.headers.common['Authorization'];
  },
};

export { api };
export default authService;