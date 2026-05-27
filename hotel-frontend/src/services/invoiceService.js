import { api } from './authService';

const invoiceService = {
  /** GET /invoices — toutes les factures (ADMIN / RECEPTIONNISTE) */
  getAll: async (params = {}) => {
    const { data } = await api.get('/invoices', { params });
    return data?.data ?? data;
  },

  /**
   * GET /invoices/my — factures du client connecté
   */
  getMyInvoices: async () => {
    const { data } = await api.get('/invoices/my');
    return data?.data ?? data;
  },

  /** GET /invoices/{id} */
  getById: async (id) => {
    const { data } = await api.get(`/invoices/${id}`);
    return data?.data ?? data;
  },

  /**
   * POST /invoices — génère une facture pour une réservation
   * FIX : reservationId doit être un Number (pas une string issue du <select>)
   */
  generate: async (reservationId) => {
    const { data } = await api.post('/invoices', { reservationId: Number(reservationId) });
    return data?.data ?? data;
  },

  /**
   * PATCH /invoices/{id}/pay — marque la facture comme payée
   */
  markAsPaid: async (id) => {
    const { data } = await api.patch(`/invoices/${id}/pay`);
    return data?.data ?? data;
  },

  /**
   * GET /invoices/{id}/pdf — télécharge la facture en PDF (blob)
   */
  download: async (id) => {
    const response = await api.get(`/invoices/${id}/pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },

  /**
   * GET /invoices/reservation/{reservationId}
   */
  getByReservation: async (reservationId) => {
    const { data } = await api.get(`/invoices/reservation/${reservationId}`);
    return data?.data ?? data;
  },
};

export default invoiceService;
