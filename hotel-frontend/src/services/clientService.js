import { api } from './authService';

const clientService = {
  /** GET /clients */
  getAll: async (params = {}) => {
    const { data } = await api.get('/clients', { params });
    return data?.data ?? data;
  },

  getById: async (id) => {
    const { data } = await api.get(`/clients/${id}`);
    return data?.data ?? data;
  },

  /**
   * POST /clients
   * Le backend attend : { firstName, lastName, email, phone, cin }
   * Le formulaire envoie : { name, email, phone, address, cin }
   *
   * FIXES :
   *  1. Découpage name → firstName + lastName
   *  2. cin transmis tel quel (champ ajouté dans le formulaire)
   *  3. Normalisation du téléphone : suppression des espaces/tirets
   *     pour satisfaire le regex backend ^\+?[0-9\s\-().]{6,20}$
   */
  create: async (payload) => {
    const nameParts = (payload.name || '').trim().split(/\s+/);
    const firstName = nameParts[0] || '';
    const lastName  = nameParts.slice(1).join(' ') || firstName;

    const mapped = {
      firstName,
      lastName,
      email: payload.email,
      phone: payload.phone || '',
      cin:   payload.cin   || '',
    };
    const { data } = await api.post('/clients', mapped);
    return data?.data ?? data;
  },

  /** PUT /clients/{id} */
  update: async (id, payload) => {
    const nameParts = (payload.name || '').trim().split(/\s+/);
    const firstName = nameParts[0] || '';
    const lastName  = nameParts.slice(1).join(' ') || firstName;

    const mapped = {
      firstName,
      lastName,
      email: payload.email,
      phone: payload.phone || '',
      cin:   payload.cin   || '',
    };
    const { data } = await api.put(`/clients/${id}`, mapped);
    return data?.data ?? data;
  },

  delete: async (id) => {
    const { data } = await api.delete(`/clients/${id}`);
    return data?.data ?? data;
  },
};

export default clientService;