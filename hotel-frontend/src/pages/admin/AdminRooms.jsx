import React, { useEffect, useState, useCallback, useMemo, useRef } from 'react';
import roomService from '../../services/roomService';
import reservationService from '../../services/reservationService';
import tariffService from '../../services/tariffService';
import equipmentService from '../../services/equipmentService';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import SearchBar from '../../components/common/SearchBar';
import Pagination from '../../components/common/Pagination';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import StatusBadge from '../../components/common/StatusBadge';
import { usePagination } from '../../hooks/usePagination';
import { CURRENCY } from '../../constants';
import '../../components/common/shared.css';
import './AdminRooms.css';

const TYPES    = ['single','double','suite','deluxe','familiale'];
const STATUSES = ['available','occupied','maintenance'];
const TYPE_LABELS   = { single:'Simple', double:'Double', suite:'Suite', deluxe:'Deluxe', familiale:'Familiale' };
const STATUS_LABELS = { available:'Disponible', occupied:'Occupée', maintenance:'Maintenance' };

const EMPTY_FORM = {
  number: '', floor: '', type: 'double', status: 'available',
  capacity: 2, description: '', amenities: [], price_per_night: '',
};

// ── Bloc d'info disponibilité/occupation affiché dans le formulaire ──
const RoomAvailabilityInfo = ({ roomId, currentStatus }) => {
  const [info, setInfo] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!roomId || currentStatus === 'available') { setInfo(null); return; }
    setLoading(true);
    reservationService.getAll({ room_id: roomId, status: 'CHECKED_IN,CONFIRMED' })
      .then(reservations => {
        const active = Array.isArray(reservations)
          ? reservations.find(r =>
              String(r.room_id) === String(roomId) ||
              String(r.roomId) === String(roomId)
            )
          : null;
        setInfo(active || null);
      })
      .catch(() => setInfo(null))
      .finally(() => setLoading(false));
  }, [roomId, currentStatus]);

  if (currentStatus === 'available') return null;

  const fmtDate = (d) => d ? new Date(d).toLocaleDateString('fr-FR') : '—';

  const bgColor = currentStatus === 'occupied'
    ? 'var(--danger-light, #fff0f0)'
    : 'var(--warning-light, #fff8e1)';
  const borderColor = currentStatus === 'occupied'
    ? 'var(--danger, #e53935)'
    : 'var(--warning, #f9a825)';
  const textColor = currentStatus === 'occupied'
    ? 'var(--danger-dark, #b71c1c)'
    : 'var(--warning-dark, #795548)';
  const icon = currentStatus === 'occupied' ? '🔴' : '🔧';

  return (
    <div style={{
      background: bgColor,
      border: `1px solid ${borderColor}`,
      borderRadius: 8,
      padding: '0.65rem 1rem',
      marginBottom: '1.25rem',
      fontSize: '0.85rem',
      color: textColor,
    }}>
      <div style={{ fontWeight: 600, marginBottom: 4 }}>
        {icon} Chambre{' '}
        {currentStatus === 'occupied' ? 'actuellement occupée' : 'en maintenance'}
      </div>
      {loading && <span style={{ opacity: 0.7 }}>Chargement des détails…</span>}
      {!loading && info && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {info.client_name && (
            <span>👤 Client : <strong>{info.client_name || info.clientName}</strong></span>
          )}
          <span>
            📅 Arrivée :{' '}
            <strong>{fmtDate(info.check_in_date || info.checkInDate)}</strong>
            &nbsp;→&nbsp;
            Départ : <strong>{fmtDate(info.check_out_date || info.checkOutDate)}</strong>
          </span>
          {(info.check_in_date || info.checkInDate) && (info.check_out_date || info.checkOutDate) && (() => {
            const ci = new Date(info.check_in_date || info.checkInDate);
            const co = new Date(info.check_out_date || info.checkOutDate);
            const nights = Math.round((co - ci) / (1000 * 60 * 60 * 24));
            const today  = new Date();
            const remaining = Math.max(0, Math.round((co - today) / (1000 * 60 * 60 * 24)));
            return (
              <>
                <span>🌙 Durée : <strong>{nights} nuit{nights > 1 ? 's' : ''}</strong>
                  {remaining > 0 && ` — encore ${remaining} jour${remaining > 1 ? 's' : ''}`}
                </span>
              </>
            );
          })()}
          {info.status && (
            <span>📋 Statut réservation : <strong>{info.status}</strong></span>
          )}
        </div>
      )}
      {!loading && !info && currentStatus === 'occupied' && (
        <span style={{ opacity: 0.7 }}>Aucune réservation active trouvée pour cette chambre.</span>
      )}
      {!loading && currentStatus === 'maintenance' && (
        <span>Cette chambre est temporairement indisponible pour maintenance.</span>
      )}
    </div>
  );
};

/**
 * RoomForm — Formulaire de création/édition d'une chambre.
 *
 * CORRIGÉ — Bug FAMILIALE (frontend) :
 *   - Le champ "Prix / nuit" est maintenant visible à la création pour tous les types.
 *   - Un avertissement s'affiche quand le type sélectionné n'a encore aucune chambre
 *     (ex: Familiale), expliquant que le backend utilisera un prix par défaut.
 *   - Le prix saisi ici est envoyé explicitement au backend, ce qui évite tout
 *     recours au fallback et garantit un prix correct dès la création.
 */
const RoomForm = ({ initial = EMPTY_FORM, onSubmit, onClose, loading, availableAmenities = [], isEdit = false, existingTypeCount = {} }) => {
  const [form, setForm] = useState({
    ...EMPTY_FORM,
    ...initial,
    amenities: initial.amenities ?? [],
    floor: initial.floor ?? '',
    price_per_night: initial.price_per_night ?? '',
  });
  const [errors, setErrors] = useState({});
  const submitting = useRef(false);

  // CORRIGÉ — Bug FAMILIALE : détecte si le type sélectionné n'a pas encore de chambre
  const typeHasNoRooms = !isEdit && (existingTypeCount[form.type] ?? 0) === 0;

  const validate = () => {
    const e = {};
    if (!form.number) e.number = 'Numéro requis';
    // CORRIGÉ — Bug FAMILIALE : prix obligatoire si c'est le premier du type
    if (typeHasNoRooms && !form.price_per_night) {
      e.price_per_night = `Prix requis pour la première chambre de type ${TYPE_LABELS[form.type]}`;
    }
    if (form.price_per_night && Number(form.price_per_night) <= 0) {
      e.price_per_night = 'Le prix doit être supérieur à 0';
    }
    return e;
  };

  const handle = ({ target: { name, value } }) => {
    setForm(p => ({ ...p, [name]: value }));
    if (errors[name]) setErrors(p => ({ ...p, [name]: '' }));
  };

  const toggleAmenity = (name) => {
    setForm(p => ({
      ...p,
      amenities: p.amenities.includes(name)
        ? p.amenities.filter(x => x !== name)
        : [...p.amenities, name],
    }));
  };

  const submit = (e) => {
    e.preventDefault();
    if (submitting.current) return;
    const e2 = validate();
    if (Object.keys(e2).length) { setErrors(e2); return; }
    submitting.current = true;
    onSubmit(form);
  };

  return (
    <form onSubmit={submit}>
      {/* Info : le prix est défini dans la section Tarifs */}
      <div style={{
        background: 'var(--gold-light, #fffbe6)',
        border: '1px solid var(--gold, #d4af37)',
        borderRadius: 8,
        padding: '0.65rem 1rem',
        marginBottom: '1.25rem',
        fontSize: '0.85rem',
        color: 'var(--gold-dark, #8a6d00)',
        display: 'flex',
        alignItems: 'center',
        gap: 8,
      }}>
        💡 Le prix de base par type de chambre se définit dans <strong>Tarifs → Prix de base</strong>.
      </div>

      {/* CORRIGÉ — Bug FAMILIALE : avertissement si premier du type */}
      {typeHasNoRooms && (
        <div style={{
          background: 'var(--info-light, #e8f4fd)',
          border: '1px solid var(--info, #2196f3)',
          borderRadius: 8,
          padding: '0.65rem 1rem',
          marginBottom: '1.25rem',
          fontSize: '0.85rem',
          color: 'var(--info-dark, #0d47a1)',
        }}>
          ℹ️ Aucune chambre de type <strong>{TYPE_LABELS[form.type]}</strong> n'existe encore.
          Veuillez saisir un prix pour cette première chambre.
          Vous pourrez ensuite l'ajuster depuis <strong>Tarifs → Prix de base</strong>.
        </div>
      )}

      {/* Bloc occupation/maintenance en mode édition */}
      {isEdit && (
        <RoomAvailabilityInfo
          roomId={initial.id}
          currentStatus={form.status}
        />
      )}

      <div className="form-grid">
        <div className="form-group">
          <label className="form-label">N° de chambre *</label>
          <input name="number" className="form-input" value={form.number} onChange={handle} placeholder="101" />
          {errors.number && <span className="form-error">{errors.number}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Étage</label>
          <input name="floor" type="number" className="form-input" value={form.floor} onChange={handle} />
        </div>
        <div className="form-group">
          <label className="form-label">Type</label>
          <select name="type" className="form-select" value={form.type} onChange={handle}>
            {TYPES.map(t => <option key={t} value={t}>{TYPE_LABELS[t]}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Statut</label>
          <select name="status" className="form-select" value={form.status} onChange={handle}>
            {STATUSES.map(s => <option key={s} value={s}>{STATUS_LABELS[s]}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Capacité (personnes)</label>
          <input name="capacity" type="number" min="1" max="10" className="form-input" value={form.capacity} onChange={handle} />
        </div>

        {/* CORRIGÉ — Bug FAMILIALE : champ prix visible pour saisie explicite */}
        <div className="form-group">
          <label className="form-label">
            Prix / nuit ({CURRENCY})
            {typeHasNoRooms && <span style={{ color: 'var(--danger, #e53935)', marginLeft: 4 }}>*</span>}
          </label>
          <input
            name="price_per_night"
            type="number"
            min="0.01"
            step="0.01"
            className="form-input"
            value={form.price_per_night}
            onChange={handle}
            placeholder={typeHasNoRooms ? 'Requis' : 'Optionnel — prix de base utilisé si vide'}
          />
          {errors.price_per_night && <span className="form-error">{errors.price_per_night}</span>}
        </div>

        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
          <label className="form-label">Description</label>
          <textarea name="description" className="form-textarea" value={form.description} onChange={handle} placeholder="Description de la chambre..." />
        </div>
      </div>

      <div className="form-group" style={{ marginTop: '1rem' }}>
        <label className="form-label">Équipements</label>
        {availableAmenities.length === 0 ? (
          <p style={{ color: '#888', fontSize: '0.875rem' }}>Chargement des équipements...</p>
        ) : (
          <div className="amenity-grid">
            {availableAmenities.map(eq => (
              <label key={eq.id} className={`amenity-chip ${form.amenities.includes(eq.name) ? 'is-selected' : ''}`}>
                <input
                  type="checkbox"
                  checked={form.amenities.includes(eq.name)}
                  onChange={() => toggleAmenity(eq.name)}
                  style={{ display: 'none' }}
                />
                {eq.icon ? eq.icon + ' ' : ''}{eq.name}
              </label>
            ))}
          </div>
        )}
      </div>

      <div className="form-actions">
        <button type="button" className="btn btn--outline" onClick={onClose}>Annuler</button>
        <button type="submit" className="btn btn--primary" disabled={loading}>
          {loading ? <span className="dt-spinner" /> : 'Enregistrer'}
        </button>
      </div>
    </form>
  );
};

const AdminRooms = ({ toast }) => {
  const [rooms, setRooms]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [loadError, setLoadError] = useState('');
  const [modal, setModal]       = useState(null);
  const [saving, setSaving]     = useState(false);
  const [search, setSearch]     = useState('');
  const [typeFilter, setType]   = useState('');
  const [statusFilter, setStatus] = useState('');
  const [confirm, setConfirm]   = useState(null);
  const [availableAmenities, setAvailableAmenities] = useState([]);
  const [activeTariffPrices, setActiveTariffPrices] = useState({});

  const loadActiveTariffPrices = useCallback(() => {
    tariffService.getAll().then(tariffs => {
      const today = new Date().toISOString().slice(0, 10);
      const priceByType = {};
      ['single','double','suite','deluxe','familiale'].forEach(type => {
        const active = (tariffs || [])
          .filter(t => t.is_active &&
            t.start_date && t.end_date &&
            t.start_date <= today && t.end_date >= today &&
            (!t.room_type || t.room_type === type))
          .sort((a, b) => {
            const aSpec = a.room_type ? 0 : 1;
            const bSpec = b.room_type ? 0 : 1;
            if (aSpec !== bSpec) return aSpec - bSpec;
            return (a.effective_price || 0) - (b.effective_price || 0);
          });
        if (active.length > 0) priceByType[type] = active[0].effective_price;
      });
      setActiveTariffPrices(priceByType);
    }).catch(() => {});
  }, []);

  const load = useCallback(() => {
    setLoading(true);
    setLoadError('');
    roomService.getAll()
      .then(data => { setRooms(Array.isArray(data) ? data : []); })
      .catch(err => {
        toast?.error('Impossible de charger les chambres');
        setLoadError(err?.message || 'Erreur lors du chargement des chambres');
      })
      .finally(() => setLoading(false));
  }, [toast]);

  useEffect(() => {
    equipmentService.getAll()
      .then(data => setAvailableAmenities(Array.isArray(data) ? data : []))
      .catch(() => {});
  }, []);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    loadActiveTariffPrices();
    const interval = setInterval(loadActiveTariffPrices, 5 * 60 * 1000);
    return () => clearInterval(interval);
  }, [loadActiveTariffPrices]);

  // CORRIGÉ — Bug FAMILIALE : calcule le nombre de chambres par type
  // pour détecter les types sans chambre et afficher l'avertissement dans le formulaire.
  const roomCountByType = useMemo(() => {
    const counts = {};
    TYPES.forEach(t => { counts[t] = 0; });
    rooms.forEach(r => {
      if (r.type && counts[r.type] !== undefined) counts[r.type]++;
    });
    return counts;
  }, [rooms]);

  const filtered = useMemo(() => {
    return rooms.filter(r => {
      const matchSearch = !search ||
        String(r.number).includes(search) ||
        r.type?.includes(search.toLowerCase());
      const matchType   = !typeFilter   || r.type   === typeFilter;
      const matchStatus = !statusFilter || r.status === statusFilter;
      return matchSearch && matchType && matchStatus;
    });
  }, [rooms, search, typeFilter, statusFilter]);

  const { page, totalPages, currentData, goTo, reset } = usePagination(filtered, 8);

  const handleCreate = async (form) => {
    setSaving(true);
    try {
      await roomService.create(form);
      toast?.success('Chambre créée');
      setModal(null); load();
    } catch (err) { toast?.error(err.message); }
    finally { setSaving(false); }
  };

  const handleEdit = async (form) => {
    setSaving(true);
    try {
      await roomService.update(modal.room.id, form);
      toast?.success('Chambre mise à jour');
      setModal(null); load();
    } catch (err) { toast?.error(err.message); }
    finally { setSaving(false); }
  };

  const handleDelete = async () => {
    try {
      await roomService.delete(confirm.id);
      toast?.success('Chambre supprimée');
      setConfirm(null); load();
    } catch (err) { toast?.error(err.message); }
  };

  const columns = [
    { key: 'number', label: 'N°', render: r => <strong>#{r.number}</strong> },
    { key: 'floor',  label: 'Étage', render: r => `Étage ${r.floor ?? '---'}` },
    { key: 'type',   label: 'Type',   render: r => TYPE_LABELS[r.type] ?? r.type },
    { key: 'capacity', label: 'Cap.', render: r => `${r.capacity ?? '---'} pers.` },
    {
      key: 'price', label: 'Prix/nuit',
      render: r => {
        const price = activeTariffPrices[r.type] ?? r.price_per_night;
        if (!price) return <span style={{ color: '#aaa', fontSize: '0.8rem' }}>—</span>;
        const fromTariff = !!activeTariffPrices[r.type];
        return (
          <span title={fromTariff ? 'Prix tarif actif' : 'Prix de base'}>
            {Number(price).toFixed(2)} {CURRENCY}
            {fromTariff && <span style={{ marginLeft: 4, fontSize: '0.7rem', color: 'var(--gold-dark, #8a6d00)' }}>🏷</span>}
          </span>
        );
      }
    },
    { key: 'status', label: 'Statut', render: r => <StatusBadge status={r.status} /> },
    {
      key: 'amenities', label: 'Équipements',
      render: r => (
        <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap', maxWidth: 200 }}>
          {(r.amenities ?? []).slice(0, 3).map(a => (
            <span key={a} className="amenity-tag">{a}</span>
          ))}
          {(r.amenities?.length ?? 0) > 3 && (
            <span className="amenity-tag amenity-tag--more">+{r.amenities.length - 3}</span>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Gestion des chambres"
        subtitle={`${rooms.length} chambre${rooms.length > 1 ? 's' : ''} enregistrées`}
        action={
          <button className="btn btn--primary" onClick={() => setModal('create')}>
            + Ajouter chambre
          </button>
        }
      />

      {loadError && !loading && (
        <div className="booking-error" style={{ marginBottom: '1rem' }}>
          {loadError}
          <button className="btn btn--sm btn--outline" style={{ marginLeft: '1rem' }} onClick={load}>
            Réessayer
          </button>
        </div>
      )}

      <div className="adm-toolbar">
        <SearchBar
          value={search}
          onChange={v => { setSearch(v); reset(); }}
          placeholder="Rechercher par numéro ou type..."
        />
        <div className="adm-toolbar__filters">
          {['', ...TYPES].map(t => (
            <button key={t} className={`btn btn--sm ${typeFilter === t ? 'btn--primary' : 'btn--outline'}`}
              onClick={() => { setType(t); reset(); }}>
              {t === '' ? 'Tous types' : TYPE_LABELS[t]}
            </button>
          ))}
        </div>
        <div className="adm-toolbar__filters">
          {['', ...STATUSES].map(s => (
            <button key={s} className={`btn btn--sm ${statusFilter === s ? 'btn--primary' : 'btn--outline'}`}
              onClick={() => { setStatus(s); reset(); }}>
              {s === '' ? 'Tous statuts' : STATUS_LABELS[s]}
            </button>
          ))}
        </div>
      </div>

      <DataTable
        columns={columns}
        data={currentData}
        loading={loading}
        empty="Aucune chambre trouvée."
        actions={row => (
          <div style={{ display: 'flex', gap: 6 }}>
            <button className="btn btn--sm btn--outline" onClick={() => setModal({ type: 'edit', room: row })}>
              Modifier
            </button>
            <button className="btn btn--sm btn--danger" onClick={() => setConfirm(row)}>
              Supprimer
            </button>
          </div>
        )}
      />

      <Pagination page={page} totalPages={totalPages} onPage={goTo} />

      {modal === 'create' && (
        <Modal title="Ajouter une chambre" onClose={() => setModal(null)} size="lg">
          {/* CORRIGÉ — Bug FAMILIALE : existingTypeCount passé pour détecter les types sans chambre */}
          <RoomForm
            onSubmit={handleCreate}
            onClose={() => setModal(null)}
            loading={saving}
            availableAmenities={availableAmenities}
            existingTypeCount={roomCountByType}
          />
        </Modal>
      )}

      {modal?.type === 'edit' && (
        <Modal title="Modifier la chambre" onClose={() => setModal(null)} size="lg">
          <RoomForm
            initial={modal.room}
            isEdit
            onSubmit={handleEdit}
            onClose={() => setModal(null)}
            loading={saving}
            availableAmenities={availableAmenities}
            existingTypeCount={roomCountByType}
          />
        </Modal>
      )}

      {confirm && (
        <ConfirmDialog
          title="Supprimer la chambre"
          message={`Supprimer la chambre N° ${confirm.number} ?`}
          danger
          onConfirm={handleDelete}
          onCancel={() => setConfirm(null)}
        />
      )}
    </div>
  );
};

export default AdminRooms;