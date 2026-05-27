import { api } from './authService';


/** Convertit le type frontend en valeur enum backend */
const toBackendType = (type) => {
  if (!type) return 'DOUBLE';
  const map = {
    single: 'SIMPLE',
    double: 'DOUBLE',
    suite:  'SUITE',
    deluxe: 'DELUXE',
    familiale: 'FAMILIALE', 
  };
  return map[type.toLowerCase()] ?? type.toUpperCase();
};

/** Convertit le statut frontend en valeur enum backend */
const toBackendStatus = (status) => {
  const map = {
    available:   'AVAILABLE',
    occupied:    'OCCUPIED',
    maintenance: 'MAINTENANCE',
  };
  if (!status) return 'AVAILABLE';
  return map[status.toLowerCase()] ?? status.toUpperCase();
};

/** Convertit le type backend (SIMPLE, DOUBLE...) en valeur frontend */
const fromBackendType = (type) => {
  if (!type) return 'double';
  const map = {
    SIMPLE:    'single',
    DOUBLE:    'double',
    SUITE:     'suite',
    DELUXE:    'deluxe',
    FAMILIALE: 'familiale', // CORRIGÉ — Bug #3 : FAMILIALE mappé vers 'familiale' (pas 'double')
  };
  return map[type.toUpperCase()] ?? type.toLowerCase();
};

/** Convertit le statut backend en valeur frontend */
const fromBackendStatus = (status) => {
  if (!status) return 'available';
  const map = {
    AVAILABLE:   'available',
    OCCUPIED:    'occupied',
    MAINTENANCE: 'maintenance',
  };
  return map[status.toUpperCase()] ?? status.toLowerCase();
};

/** Normalise une chambre reçue du backend vers le format frontend */
const normalizeRoom = (room) => {
  if (!room) return room;
  // Guard : number absent ou null → on retourne la chambre telle quelle
  // pour éviter que String(undefined) produise "undefined" dans les filtres texte.
  if (room.number == null) return room;
  return {
    ...room,
    // FIX M : room.number forcé en String pour que .includes() fonctionne toujours
    //         (le backend peut renvoyer un Number, ce qui casse la recherche textuelle)
    number: String(room.number),
    type:   fromBackendType(room.type),
    status: fromBackendStatus(room.status),
    price_per_night: room.price_per_night ?? room.price,
  };
};

const roomService = {
  /** GET /rooms */
  getAll: async (params = {}) => {
    // FIX Q : gestion d'erreur propagée à l'appelant pour affichage visible (toast/fallback UI)
    const { data } = await api.get('/rooms', { params });
    const list = data?.data ?? data;
    return Array.isArray(list) ? list.map(normalizeRoom) : list;
  },

  getAvailable: async ({ checkIn, checkOut } = {}) => {
    // FIX Q : gestion d'erreur propagée à l'appelant
    const { data } = await api.get('/rooms/available', {
      params: { checkIn, checkOut },
    });
    const list = data?.data ?? data;
    return Array.isArray(list) ? list.map(normalizeRoom) : list;
  },

  getById: async (id) => {
    const { data } = await api.get(`/rooms/${id}`);
    return normalizeRoom(data?.data ?? data);
  },

  /**
   * POST /rooms
   * FIX Bug #3 : le type 'familiale' → 'FAMILIALE' est correct, mais le backend
   * rejette price=null avec @DecimalMin(0.01). Solution : omettre le champ price
   * entièrement quand il est absent (le backend utilise alors le tarif de base).
   */
  create: async (roomData) => {
    const priceValue = Number(roomData.price_per_night) || Number(roomData.price) || null;
    const payload = {
      number:      String(roomData.number).trim(),
      floor:       Number(roomData.floor) || null,
      type:        toBackendType(roomData.type),
      // FIX Bug #3 : n'envoyer 'price' que s'il est défini et > 0
      ...(priceValue && priceValue > 0 ? { price: priceValue } : {}),
      capacity:    Number(roomData.capacity) || 1,
      description: roomData.description || '',
      amenities:   Array.isArray(roomData.amenities) ? roomData.amenities : [],
    };
    const { data } = await api.post('/rooms', payload);
    return normalizeRoom(data?.data ?? data);
  },

  /**
   * PUT /rooms/{id}
   * FIX L : floor ajouté dans le payload — champ silencieusement ignoré dans l'ancienne version
   */
  update: async (id, roomData) => {
    const payload = {
      number:      String(roomData.number).trim(),
      // FIX L : floor envoyé en Number (ou null si absent)
      floor:       Number(roomData.floor) || null,
      type:        toBackendType(roomData.type),
      price:       Number(roomData.price_per_night) || Number(roomData.price) || null,
      capacity:    Number(roomData.capacity) || 1,
      status:      toBackendStatus(roomData.status),
      description: roomData.description || '',
      amenities:   Array.isArray(roomData.amenities) ? roomData.amenities : [],
    };
    const { data } = await api.put(`/rooms/${id}`, payload);
    return normalizeRoom(data?.data ?? data);
  },

  delete: async (id) => {
    const { data } = await api.delete(`/rooms/${id}`);
    return data?.data ?? data;
  },
};

export default roomService;