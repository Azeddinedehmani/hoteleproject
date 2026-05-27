import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import roomService from '../../services/roomService';
import PageHeader from '../../components/common/PageHeader';
// FIX H : import de la devise centralisée — remplace le littéral '€'
import { CURRENCY } from '../../constants';
import '../../components/common/shared.css';
import './RoomsPage.css';

// ─────────────────────────────────────────────────────────────
// Libellés affichés au client (jamais de numéro de chambre)
// ─────────────────────────────────────────────────────────────
const TYPE_LABELS = {
  SIMPLE: 'Simple',
  DOUBLE: 'Double',
  SUITE:  'Suite',
  DELUXE: 'Deluxe',
  // compatibilité avec les réponses backend en lowercase
  simple: 'Simple',
  single: 'Simple',
  double: 'Double',
  suite:  'Suite',
  deluxe: 'Deluxe',
};

// Clé de normalisation : ramène SIMPLE / single → "SIMPLE"
const normalizeType = (type) => {
  if (!type) return 'INCONNU';
  const map = {
    simple:    'SIMPLE',
    single:    'SIMPLE',
    double:    'DOUBLE',
    suite:     'SUITE',
    deluxe:    'DELUXE',
    familiale: 'FAMILIALE',
  };
  const upper = type.toUpperCase();
  return map[type.toLowerCase()] ?? upper;
};

// Ordre d'affichage des types
const TYPE_ORDER = ['SIMPLE', 'DOUBLE', 'SUITE', 'DELUXE', 'FAMILIALE'];

/**
 * Regroupe une liste de chambres par type.
 * Pour chaque type, conserve :
 *   - le prix le plus bas (prix de départ)
 *   - la capacité maximale observée
 *   - l'union des équipements (dédupliqués)
 *   - le nombre de chambres disponibles
 *
 * @param {Array} rooms  Liste normalisée de chambres
 * @returns {Array}       Un objet par type présent, trié selon TYPE_ORDER
 */
const groupByType = (rooms) => {
  const map = {};

  rooms.forEach((room) => {
    const typeKey = normalizeType(room.type);

    if (!map[typeKey]) {
      map[typeKey] = {
        typeKey,
        label:          TYPE_LABELS[typeKey] ?? typeKey,
        minPrice:       Infinity,
        maxCapacity:    0,
        amenitiesSet:   new Set(),
        count:          0,
        sampleDesc:     '',
      };
    }

    const entry = map[typeKey];
    const price = Number(room.price_per_night ?? room.price ?? 0);

    if (price > 0 && price < entry.minPrice) entry.minPrice = price;
    if ((room.capacity ?? 0) > entry.maxCapacity) entry.maxCapacity = room.capacity;
    if (!entry.sampleDesc && room.description)    entry.sampleDesc  = room.description;

    (room.amenities ?? []).forEach((a) => entry.amenitiesSet.add(a));
    entry.count += 1;
  });

  return TYPE_ORDER
    .filter((k) => map[k])
    .map((k) => ({
      ...map[k],
      amenities: Array.from(map[k].amenitiesSet),
      minPrice:  map[k].minPrice === Infinity ? 0 : map[k].minPrice,
    }));
};

// ─────────────────────────────────────────────────────────────
// Composant principal
// ─────────────────────────────────────────────────────────────
const RoomsPage = () => {
  const navigate = useNavigate();
  const [rooms,   setRooms]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ check_in: '', check_out: '', type: '' });

  // ── Récupération des chambres disponibles ──
  const [fetchError, setFetchError] = useState('');

  const fetchRooms = () => {
    setLoading(true);
    // FIX Q : réinitialise l'erreur avant chaque requête
    setFetchError('');
    const params = {};
    if (filters.check_in)  params.checkIn  = filters.check_in;
    if (filters.check_out) params.checkOut = filters.check_out;
    if (filters.type)      params.type     = filters.type;

    roomService.getAvailable(params)
      .then(data => { setRooms(Array.isArray(data) ? data : []); })
      .catch(() =>
        // FIX Q : fallback sur getAll() si /available échoue ; si getAll échoue aussi → message visible
        roomService.getAll(params)
          .then(data => { setRooms(Array.isArray(data) ? data : []); })
          .catch(err => {
            setFetchError(err?.message || 'Impossible de charger les chambres');
            setRooms([]);
          })
      )
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchRooms(); }, []); // eslint-disable-line

  const handleFilter = (e) => {
    e.preventDefault();
    fetchRooms();
  };

  // ── Regroupement par type ──
  const typeGroups = groupByType(rooms);

  // ── Clic sur "Réserver" : on transmet le roomType + dates au lieu d'une chambre précise ──
  const handleReserve = (typeKey) => {
    navigate('/client/book', {
      state: {
        roomType:  typeKey,
        checkIn:   filters.check_in,
        checkOut:  filters.check_out,
      },
    });
  };

  return (
    <div>
      <PageHeader
        title="Chambres disponibles"
        subtitle="Choisissez le type de chambre qui vous convient"
      />

      {/* ── Barre de filtres ── */}
      <form className="rooms-filter" onSubmit={handleFilter}>
        <div className="form-group">
          <label className="form-label">Arrivée</label>
          <input
            type="date"
            className="form-input"
            value={filters.check_in}
            min={new Date().toISOString().split('T')[0]}
            onChange={e => setFilters(p => ({ ...p, check_in: e.target.value }))}
          />
        </div>
        <div className="form-group">
          <label className="form-label">Départ</label>
          <input
            type="date"
            className="form-input"
            value={filters.check_out}
            min={filters.check_in || new Date().toISOString().split('T')[0]}
            onChange={e => setFilters(p => ({ ...p, check_out: e.target.value }))}
          />
        </div>
        <div className="form-group">
          <label className="form-label">Type</label>
          <select
            className="form-select"
            value={filters.type}
            onChange={e => setFilters(p => ({ ...p, type: e.target.value }))}
          >
            <option value="">Tous les types</option>
            <option value="SIMPLE">Simple</option>
            <option value="DOUBLE">Double</option>
            <option value="SUITE">Suite</option>
            <option value="DELUXE">Deluxe</option>
          </select>
        </div>
        <button type="submit" className="btn btn--gold rooms-filter__btn">
          Rechercher
        </button>
      </form>

      {/* ── États de chargement / vide ── */}
      {/* FIX Q : affichage d'erreur visible avec bouton de retry */}
      {fetchError && !loading && (
        <div className="rooms-empty" style={{ color: 'var(--danger, #EF4444)' }}>
          {fetchError}
          <br />
          <button className="btn btn--sm btn--outline" style={{ marginTop: 8 }} onClick={fetchRooms}>
            Réessayer
          </button>
        </div>
      )}
      {loading ? (
        <div className="rooms-loading">
          <span className="dt-spinner" style={{ width: 24, height: 24 }} />
          Chargement des chambres…
        </div>
      ) : typeGroups.length === 0 ? (
        <div className="rooms-empty">
          <p>Aucune chambre ne correspond à vos critères.</p>
          <button
            className="btn btn--outline"
            style={{ marginTop: '1rem' }}
            onClick={() => {
              setFilters({ check_in: '', check_out: '', type: '' });
              setTimeout(fetchRooms, 0);
            }}
          >
            Réinitialiser les filtres
          </button>
        </div>
      ) : (
        /* ── Grille : une carte par type ── */
        <div className="rooms-grid">
          {typeGroups.map((group) => (
            <div key={group.typeKey} className="room-card">

              {/* En-tête : type + disponibilité */}
              <div className="room-card__header">
                <span className="room-card__type">{group.label}</span>
                <span className="room-card__capacity">
                  {group.maxCapacity > 0 ? `jusqu'à ${group.maxCapacity} pers.` : ''}
                </span>
              </div>

              {/* Corps : description + équipements communs */}
              <div className="room-card__body">
                {group.sampleDesc && (
                  <p className="room-card__desc">{group.sampleDesc}</p>
                )}
                {group.amenities.length > 0 && (
                  <div className="room-card__amenities">
                    {group.amenities.slice(0, 6).map((a, i) => (
                      <span key={i} className="room-card__amenity">{a}</span>
                    ))}
                  </div>
                )}
                <p className="room-card__avail">
                  {group.count} chambre{group.count > 1 ? 's' : ''} disponible{group.count > 1 ? 's' : ''}
                </p>
              </div>

              {/* Pied de carte : prix de départ + bouton Réserver */}
              <div className="room-card__footer">
                <div>
                  {/* FIX H : CURRENCY remplace le littéral '€' */}
                  <p className="room-card__price">
                    {group.minPrice > 0 ? `À partir de ${group.minPrice} ${CURRENCY}` : '—'}
                  </p>
                  <p className="room-card__per">/ nuit</p>
                </div>
                <button
                  className="btn btn--gold"
                  onClick={() => handleReserve(group.typeKey)}
                >
                  Réserver
                </button>
              </div>

            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RoomsPage;