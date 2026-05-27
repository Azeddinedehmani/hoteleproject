import { api } from './authService';

const equipmentService = {
  /** GET /equipment */
  getAll: async () => {
    const { data } = await api.get('/equipment');
    return data?.data ?? data;
  },

  /** POST /equipment */
  create: async (payload) => {
    const { data } = await api.post('/equipment', payload);
    return data?.data ?? data;
  },

  /** PUT /equipment/{id} */
  update: async (id, payload) => {
    const { data } = await api.put(`/equipment/${id}`, payload);
    return data?.data ?? data;
  },

  /** DELETE /equipment/{id} */
  delete: async (id) => {
    const { data } = await api.delete(`/equipment/${id}`);
    return data?.data ?? data;
  },
};

export default equipmentService;