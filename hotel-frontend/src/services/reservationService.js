import { api } from './authService';

/**
 * Convertit un statut frontend (minuscule) vers l'enum Java backend (UPPERCASE).
 * ReservationStatus backend : PENDING | CONFIRMED | CHECKED_IN | CHECKED_OUT | CANCELLED
 */
const toBackendStatus = (status) => {
  if (!status) return null;
  if (status === status.toUpperCase()) return status; // déjà uppercase
  const map = {
    pending:     'PENDING',
    confirmed:   'CONFIRMED',
    checked_in:  'CHECKED_IN',
    checked_out: 'CHECKED_OUT',
    cancelled:   'CANCELLED',
  };
  return map[status] ?? status.toUpperCase();
};

const reservationService = {
  /** GET /reservations — ADMIN / RECEPTIONNISTE uniquement */
  getAll: async (params = {}) => {
    const { data } = await api.get('/reservations', { params });
    return data?.data ?? data;
  },

  /** GET /reservations/my — CLIENT uniquement */
  getMyReservations: async () => {
    const { data } = await api.get('/reservations/my');
    return data?.data ?? data;
  },

  /** GET /reservations/{id} — avec vérification de propriété côté backend pour CLIENT */
  getById: async (id) => {
    const { data } = await api.get(`/reservations/${id}`);
    return data?.data ?? data;
  },

  /**
   * POST /reservations
   *
   * POINT 10 — Réservation par type de chambre :
   *   - Le client envoie `room_type` (ex. "DOUBLE") au lieu d'un `room_id`.
   *   - Le `room_id` est ignoré côté backend pour les rôles CLIENT.
   *   - La chambre précise est attribuée par la réception lors du check-in.
   *
   * POINT 13 — `applied_price` : total indicatif transmis pour traçabilité.
   *
   * Mapping vers CreateReservationRequest Java :
   *   roomType       ← room_type  (RoomType enum UPPERCASE)
   *   roomId         ← null pour les clients ; respecté pour ADMIN/RECEPTIONNISTE
   *   checkInDate    ← check_in
   *   checkOutDate   ← check_out
   *   guests         ← guests
   *   notes          ← notes
   *   appliedPrice   ← applied_price
   */
  create: async (payload) => {
    // Normalisation du type de chambre → UPPERCASE (enum Java)
    const roomType = payload.room_type
      ? String(payload.room_type).toUpperCase()
      : null;

    const mapped = {
      // clientId : obligatoire pour ADMIN/RECEPTIONNISTE (backend @NotNull)
      clientId:     payload.client_id != null ? Number(payload.client_id) : null,
      // roomId : null pour les clients (POINT 10) ; fourni si ADMIN/RECEPTIONNISTE
      roomId:       payload.room_id != null ? Number(payload.room_id) : null,
      // POINT 10 : type de chambre demandé
      roomType,
      checkInDate:  payload.check_in,
      checkOutDate: payload.check_out,
      guests:       Number(payload.guests) || 1,
      notes:        payload.notes || '',
      // POINT 13 : prix indicatif total pour traçabilité
      appliedPrice: payload.applied_price ?? null,
    };

    const { data } = await api.post('/reservations', mapped);
    const created = data?.data ?? data;

    // Si un statut différent de PENDING est demandé (ADMIN/RECEPTIONNISTE),
    // on le met à jour immédiatement après la création.
    const requestedStatus = toBackendStatus(payload.status);
    if (created?.id && requestedStatus && requestedStatus !== 'PENDING') {
      try {
        const updatePayload = {
          checkInDate:  payload.check_in,
          checkOutDate: payload.check_out,
          guests:       Number(payload.guests) || 1,
          notes:        payload.notes || '',
          status:       requestedStatus,
        };
        const { data: updated } = await api.put(`/reservations/${created.id}`, updatePayload);
        return updated?.data ?? updated;
      } catch (e) {
        // La réservation existe déjà (PENDING) — on retourne l'objet créé plutôt que de bloquer.
        // L'admin pourra changer le statut manuellement si nécessaire.
        console.warn(
          `[reservationService] PUT statut ${requestedStatus} échoué pour #${created.id} :`,
          e.message
        );
        return created; // ← fallback explicite : la réservation créée est retournée dans tous les cas
      }
    }

    return created;
  },

  /**
   * PUT /reservations/{id}
   * UpdateReservationRequest : checkInDate, checkOutDate, guests, notes, status (optionnel)
   */
  update: async (id, payload) => {
    const mapped = {
      checkInDate:  payload.check_in,
      checkOutDate: payload.check_out,
      guests:       Number(payload.guests) || 1,
      notes:        payload.notes || '',
    };
    const status = toBackendStatus(payload.status);
    if (status) mapped.status = status;

    const { data } = await api.put(`/reservations/${id}`, mapped);
    return data?.data ?? data;
  },

  /**
   * PUT vers CONFIRMED (pas de route /confirm dédiée).
   */
  confirm: async (id, reservationData) => {
    const mapped = {
      checkInDate:  reservationData.checkInDate  ?? reservationData.check_in,
      checkOutDate: reservationData.checkOutDate ?? reservationData.check_out,
      guests:       Number(reservationData.guests) || 1,
      notes:        reservationData.notes || '',
      status:       'CONFIRMED',
    };
    const { data } = await api.put(`/reservations/${id}`, mapped);
    return data?.data ?? data;
  },

  /** PATCH /reservations/{id}/cancel — vérification de propriété côté backend si CLIENT */
  cancel: async (id) => {
    const { data } = await api.patch(`/reservations/${id}/cancel`);
    return data?.data ?? data;
  },

  checkIn: async (id) => {
    const { data } = await api.patch(`/reservations/${id}/check-in`);
    return data?.data ?? data;
  },

  checkOut: async (id) => {
    const { data } = await api.patch(`/reservations/${id}/check-out`);
    return data?.data ?? data;
  },

  /**
   * PATCH /reservations/{id}/assign-room
   * BUG 4 FIX : assigne une chambre à une réservation sans roomId
   */
  assignRoom: async (id, roomId) => {
    const { data } = await api.patch(`/reservations/${id}/assign-room`, { roomId });
    return data?.data ?? data;
  },

  delete: async (id) => {
    const { data } = await api.delete(`/reservations/${id}`);
    return data?.data ?? data;
  },
};

export default reservationService;