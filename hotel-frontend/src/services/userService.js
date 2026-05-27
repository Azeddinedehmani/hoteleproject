import { api } from './authService';

/**
 * FIX : table de correspondance rôle frontend → valeur enum backend.
 *
 * Le frontend utilise 'receptionist' en interne (normalizeRole dans authService).
 * Le backend attend l'enum Role exactement : ADMIN | RECEPTIONNISTE | CLIENT
 * Un simple toUpperCase() donnait "RECEPTIONIST" → erreur de désérialisation Jackson.
 */
const ROLE_TO_BACKEND = {
  admin:        'ADMIN',
  receptionist: 'RECEPTIONNISTE',
  client:       'CLIENT',
};

/**
 * Convertit un rôle backend (ADMIN, RECEPTIONNISTE, CLIENT) en valeur frontend.
 * FIX Bug #1 & #2 : la liste /users retourne les rôles en enum Java (majuscules).
 * Sans cette normalisation, tous les rôles s'affichent mal et les filtres ne fonctionnent pas.
 */
const ROLE_FROM_BACKEND = {
  ADMIN:          'admin',
  RECEPTIONNISTE: 'receptionist',
  CLIENT:         'client',
};

const fromBackendRole = (role) => {
  if (!role) return 'client';
  return ROLE_FROM_BACKEND[role.toUpperCase()] ?? role.toLowerCase();
};

/** Normalise un utilisateur reçu du backend vers le format frontend */
const normalizeUser = (user) => {
  if (!user) return user;
  return {
    ...user,
    role: fromBackendRole(user.role),
  };
};

/**
 * Convertit un rôle frontend en valeur enum backend.
 * Accepte aussi les valeurs déjà en majuscules (ex: "ADMIN").
 */
const toBackendRole = (role) => {
  if (!role) return 'CLIENT';
  const lower = role.toLowerCase();
  return ROLE_TO_BACKEND[lower] ?? role.toUpperCase();
};

const userService = {
  /** GET /users */
  getAll: async () => {
    const { data } = await api.get('/users');
    const list = data?.data ?? data;
    // FIX Bug #1 & #2 : normaliser le rôle de chaque utilisateur
    return Array.isArray(list) ? list.map(normalizeUser) : list;
  },

  getById: async (id) => {
    const { data } = await api.get(`/users/${id}`);
    // FIX : normaliser aussi lors de la récupération individuelle (utilisé par updateRole)
    return normalizeUser(data?.data ?? data);
  },

  /**
   * POST /auth/register  ← utilisé pour créer un utilisateur depuis l'admin
   *
   * Le payload doit correspondre à RegisterRequest :
   *   { name, email, password, role, phone }
   * avec role = ADMIN | RECEPTIONNISTE | CLIENT
   */
  create: async (payload) => {
    const body = {
      name:     payload.name,
      email:    payload.email,
      password: payload.password,
      role:     toBackendRole(payload.role),   // FIX : 'receptionist' → 'RECEPTIONNISTE'
      phone:    payload.phone || '',
    };
    const { data } = await api.post('/auth/register', body);
    return data?.data ?? data;
  },

  /**
   * PUT /users/{id}
   * Corps attendu par UpdateUserRequest : { name, email, role, phone }
   */
  update: async (id, payload) => {
    const body = {
      name:  payload.name,
      email: payload.email,
      role:  toBackendRole(payload.role),      // FIX : même mapping
      phone: payload.phone || '',
    };
    const { data } = await api.put(`/users/${id}`, body);
    return data?.data ?? data;
  },

  /**
   * PATCH /users/{id}/password
   */
  changePassword: async (id, oldPassword, newPassword) => {
    const { data } = await api.patch(`/users/${id}/password`, { oldPassword, newPassword });
    return data?.data ?? data;
  },

  /**
   * Mise à jour du rôle uniquement — réutilise PUT /users/{id}.
   */
  updateRole: async (id, role) => {
    const user = await userService.getById(id);
    return userService.update(id, { ...user, role });
  },

  /** PATCH /users/{id}/deactivate */
  deactivate: async (id) => {
    const { data } = await api.patch(`/users/${id}/deactivate`);
    return data?.data ?? data;
  },

  /** PATCH /users/{id}/activate */
  activate: async (id) => {
    const { data } = await api.patch(`/users/${id}/activate`);
    return data?.data ?? data;
  },

  /** DELETE /users/{id} */
  delete: async (id) => {
    const { data } = await api.delete(`/users/${id}`);
    return data?.data ?? data;
  },
};

export default userService;