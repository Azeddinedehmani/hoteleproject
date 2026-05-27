import { api } from './authService';

/**
 * FIX : room_type doit etre envoye en UPPERCASE pour correspondre a l'enum Java RoomType
 *        (SIMPLE, DOUBLE, SUITE, DELUXE).
 *        Le frontend stocke 'single','double','suite','deluxe' en minuscule.
 *        season reste en minuscule car l'enum Java Season { low, mid, high, peak } est en minuscule.
 *
 * FIX 2 : room_type 'single' → 'SIMPLE' (meme mapping que roomService)
 */

// FIX A : room_type vide ("") doit retourner null et non ""
const toBackendRoomType = (type) => {
  if (!type || type === '') return null;
  const map = {
    single: 'SIMPLE',
    double: 'DOUBLE',
    suite:  'SUITE',
    deluxe: 'DELUXE',
    familiale: 'FAMILIALE', // CORRIGÉ — Bug #5 : ajout familiale → FAMILIALE
    // deja en majuscule (depuis la BDD)
    SIMPLE: 'SIMPLE',
    DOUBLE: 'DOUBLE',
    SUITE:  'SUITE',
    DELUXE: 'DELUXE',
    FAMILIALE: 'FAMILIALE', // CORRIGÉ — Bug #5 : ajout FAMILIALE → FAMILIALE
  };
  return map[type] ?? type.toUpperCase();
};

// FIX B : convertit le room_type UPPERCASE du backend vers la valeur lowercase du <select> frontend
const fromBackendRoomType = (type) => {
  if (!type) return '';
  const map = {
    DOUBLE: 'double',
    SIMPLE: 'single',
    SUITE:  'suite',
    DELUXE: 'deluxe',
    FAMILIALE: 'familiale', // CORRIGÉ — Bug #5 : ajout FAMILIALE → 'familiale'
    // rétrocompatibilité si déjà en minuscule
    double: 'double',
    single: 'single',
    suite:  'suite',
    deluxe: 'deluxe',
    familiale: 'familiale', // CORRIGÉ — Bug #5 : rétrocompatibilité familiale → 'familiale'
  };
  return map[type] ?? type.toLowerCase();
};

/** Normalise un tarif recu du backend vers le format frontend */
const normalizeTariff = (t) => {
  if (!t) return t;
  const price    = Number(t.price_per_night)  || 0;
  const discount = Number(t.discount_percent) || 0;
  // FIX K : effective_price calculé côté frontend si absent ou nul dans la réponse backend
  const effectivePrice =
    Number(t.effective_price) ||
    Math.round(price * (1 - discount / 100) * 100) / 100;
  return {
    ...t,
    price_per_night:   price,
    discount_percent:  discount,
    effective_price:   effectivePrice,
  };
};

const tariffService = {
  /** GET /tariffs */
  getAll: async () => {
    const { data } = await api.get('/tariffs');
    const list = data?.data ?? data;
    return Array.isArray(list) ? list.map(normalizeTariff) : list;
  },

  /**
   * CORRECTION #4 — GET /tariffs/applicable?roomType=SUITE&checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD
   *
   * Retourne le tarif saisonnier actif applicable pour le type de chambre
   * et la période donnée, ou null si aucun tarif ne correspond.
   *
   * @param {string} roomType  - type de chambre en minuscule (single, double, suite, deluxe)
   * @param {string} checkIn   - date d'arrivée au format YYYY-MM-DD
   * @param {string} checkOut  - date de départ au format YYYY-MM-DD
   * @returns {Object|null}    - tarif normalisé ou null
   */
  getApplicable: async (roomType, checkIn, checkOut) => {
    if (!roomType || !checkIn || !checkOut) return null;
    try {
      const { data } = await api.get('/tariffs/applicable', {
        params: {
          roomType: toBackendRoomType(roomType),
          checkIn,
          checkOut,
        },
      });
      const tariff = data?.data ?? data;
      return tariff ? normalizeTariff(tariff) : null;
    } catch (err) {
      // 403 si le token est expiré, 400 si paramètres invalides — on renvoie null sans bloquer le parcours
      console.warn('getApplicable tariff failed:', err?.response?.status, err?.message);
      return null;
    }
  },

  /** POST /tariffs */
  create: async (form) => {
    const payload = {
      name:             form.name?.trim(),
      season:           form.season,
      room_type:        form.room_type ? toBackendRoomType(form.room_type) : null,
      price_per_night:  Number(form.price_per_night) || 0,
      discount_percent: Number(form.discount_percent) || 0,
      start_date:       form.start_date,
      end_date:         form.end_date,
      is_active:        form.is_active !== false,
    };
    const { data } = await api.post('/tariffs', payload);
    return normalizeTariff(data?.data ?? data);
  },

  /** PUT /tariffs/{id} */
  update: async (id, form) => {
    const payload = {
      name:             form.name?.trim(),
      season:           form.season,
      room_type:        form.room_type ? toBackendRoomType(form.room_type) : null,
      price_per_night:  Number(form.price_per_night) || 0,
      discount_percent: Number(form.discount_percent) || 0,
      start_date:       form.start_date,
      end_date:         form.end_date,
      is_active:        form.is_active !== false,
    };
    const { data } = await api.put(`/tariffs/${id}`, payload);
    return normalizeTariff(data?.data ?? data);
  },

  /** DELETE /tariffs/{id} */
  delete: async (id) => {
    const { data } = await api.delete(`/tariffs/${id}`);
    return data?.data ?? data;
  },

  /** PATCH /tariffs/{id}/discount */
  applyDiscount: async (id, discount) => {
    const { data } = await api.patch(`/tariffs/${id}/discount`, { discount });
    return normalizeTariff(data?.data ?? data);
  },
};

export default tariffService;
export { fromBackendRoomType };