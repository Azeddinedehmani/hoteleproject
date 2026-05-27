import React, { useEffect, useState, useCallback } from 'react';
import tariffService, { fromBackendRoomType } from '../../services/tariffService';
import roomService from '../../services/roomService';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import Pagination from '../../components/common/Pagination';
import { usePagination } from '../../hooks/usePagination';
import { CURRENCY } from '../../constants';
import '../../components/common/shared.css';
import './AdminTariffs.css';

// ─────────────────────────────────────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────────────────────────────────────

const SEASONS = [
  { value: 'low',  label: 'Basse saison' },
  { value: 'mid',  label: 'Moyenne saison' },
  { value: 'high', label: 'Haute saison' },
  { value: 'peak', label: 'Très haute saison' },
];

const ROOM_TYPES = ['single', 'double', 'suite', 'deluxe', 'familiale'];
const TYPE_LABELS = {
  single: 'Simple', double: 'Double', suite: 'Suite',
  deluxe: 'Deluxe', familiale: 'Familiale',
};

// Icônes pour chaque type (optionnel, visuel)
const TYPE_ICONS = {
  single: '🛏', double: '🛏🛏', suite: '🌟', deluxe: '💎', familiale: '👨‍👩‍👧‍👦',
};

const EMPTY_FORM = {
  name: '', season: 'mid', room_type: 'double',
  price_per_night: '', start_date: '', end_date: '',
  discount_percent: 0, is_active: true,
};

// ─────────────────────────────────────────────────────────────────────────────
// Section Prix de base par type de chambre
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Calcule le prix de base actuel par type à partir des chambres existantes.
 * Le "prix de base" d'un type = médiane des prix des chambres de ce type.
 * L'admin peut le modifier ici, ce qui met à jour toutes les chambres du type.
 */
const BasePricesSection = ({ toast }) => {
  const [rooms, setRooms]       = useState([]);
  const [prices, setPrices]     = useState({}); // { double: '500', suite: '900', ... }
  const [saving, setSaving]     = useState(''); // type en cours de sauvegarde
  const [loading, setLoading]   = useState(true);

  // Charge les chambres et calcule le prix médian par type
  const loadRooms = useCallback(() => {
    setLoading(true);
    roomService.getAll()
      .then(data => {
        const list = Array.isArray(data) ? data : [];
        setRooms(list);
        // Calcule un prix représentatif par type (moyenne des prix existants)
        const byType = {};
        ROOM_TYPES.forEach(t => {
          const matching = list.filter(r => r.type === t && r.price_per_night > 0);
          if (matching.length > 0) {
            const avg = matching.reduce((s, r) => s + Number(r.price_per_night), 0) / matching.length;
            byType[t] = avg.toFixed(2);
          } else {
            byType[t] = '';
          }
        });
        setPrices(byType);
      })
      .catch(() => toast?.error('Impossible de charger les chambres'))
      .finally(() => setLoading(false));
  }, [toast]);

  useEffect(() => { loadRooms(); }, [loadRooms]);

  const handleChange = (type, value) => {
    setPrices(p => ({ ...p, [type]: value }));
  };

  // Met à jour le prix de toutes les chambres du type donné
  const handleSave = async (type) => {
    const newPrice = parseFloat(prices[type]);
    if (!newPrice || newPrice <= 0) {
      toast?.error('Le prix doit être supérieur à 0');
      return;
    }
    setSaving(type);
    try {
      const toUpdate = rooms.filter(r => r.type === type);
      if (toUpdate.length === 0) {
        toast?.info?.(`Aucune chambre de type ${TYPE_LABELS[type]} trouvée`);
        return;
      }
      // Met à jour chaque chambre du type en parallèle
      await Promise.all(
        toUpdate.map(r =>
          roomService.update(r.id, { ...r, price_per_night: newPrice })
        )
      );
      toast?.success(`Prix de base ${TYPE_LABELS[type]} mis à jour (${toUpdate.length} chambre${toUpdate.length > 1 ? 's' : ''})`);
      loadRooms();
    } catch (err) {
      toast?.error(err?.message || 'Erreur lors de la mise à jour');
    } finally {
      setSaving('');
    }
  };

  const countByType = (type) => rooms.filter(r => r.type === type).length;

  return (
    <div className="base-prices-section">
      <div className="base-prices-header">
        <h2 className="base-prices-title">Prix de base par type de chambre</h2>
        <p className="base-prices-subtitle">
          Ce prix s'applique comme fallback quand aucun tarif saisonnier ne correspond à la période de réservation.
        </p>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '2rem', color: '#888' }}>
          <span className="dt-spinner" /> Chargement...
        </div>
      ) : (
        <div className="base-prices-grid">
          {ROOM_TYPES.map(type => {
            const count = countByType(type);
            const isSaving = saving === type;
            return (
              <div key={type} className={`base-price-card ${count === 0 ? 'base-price-card--empty' : ''}`}>
                <div className="base-price-card__icon">{TYPE_ICONS[type]}</div>
                <div className="base-price-card__label">{TYPE_LABELS[type]}</div>
                <div className="base-price-card__count">
                  {count > 0
                    ? `${count} chambre${count > 1 ? 's' : ''}`
                    : <span style={{ color: '#bbb' }}>Aucune chambre</span>
                  }
                </div>
                <div className="base-price-card__input-row">
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    className="form-input base-price-card__input"
                    value={prices[type] ?? ''}
                    onChange={e => handleChange(type, e.target.value)}
                    placeholder="—"
                    disabled={count === 0}
                  />
                  <span className="base-price-card__currency">{CURRENCY}</span>
                </div>
                <button
                  className="btn btn--primary btn--sm base-price-card__btn"
                  onClick={() => handleSave(type)}
                  disabled={isSaving || count === 0 || !prices[type]}
                >
                  {isSaving ? <span className="dt-spinner" /> : 'Appliquer'}
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

// ─────────────────────────────────────────────────────────────────────────────
// Formulaire Tarif saisonnier
// ─────────────────────────────────────────────────────────────────────────────

const TariffForm = ({ initial = EMPTY_FORM, onSubmit, onClose, loading }) => {
  const [form, setForm] = useState({ ...EMPTY_FORM, ...initial });
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.name.trim())     e.name  = 'Nom requis';
    if (!form.price_per_night) e.price = 'Prix requis';
    if (!form.start_date)      e.start = 'Date début requise';
    if (!form.end_date)        e.end   = 'Date fin requise';
    return e;
  };

  const handle = ({ target: { name, value, type, checked } }) => {
    setForm(p => ({ ...p, [name]: type === 'checkbox' ? checked : value }));
    if (errors[name]) setErrors(p => ({ ...p, [name]: '' }));
  };

  const submit = (e) => {
    e.preventDefault();
    const e2 = validate();
    if (Object.keys(e2).length) { setErrors(e2); return; }
    onSubmit(form);
  };

  const finalPrice = form.price_per_night
    ? (form.price_per_night * (1 - (form.discount_percent || 0) / 100)).toFixed(2)
    : '—';

  return (
    <form onSubmit={submit}>
      <div className="form-grid">
        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
          <label className="form-label">Nom du tarif *</label>
          <input name="name" className="form-input" value={form.name} onChange={handle} placeholder="Ex: Tarif été 2025" />
          {errors.name && <span className="form-error">{errors.name}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Saison</label>
          <select name="season" className="form-select" value={form.season} onChange={handle}>
            {SEASONS.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Type de chambre</label>
          <select name="room_type" className="form-select" value={form.room_type} onChange={handle}>
            <option value="">Tous types</option>
            {ROOM_TYPES.map(t => <option key={t} value={t}>{TYPE_LABELS[t]}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Prix / nuit ({CURRENCY}) *</label>
          <input name="price_per_night" type="number" min="0" step="0.01" className="form-input" value={form.price_per_night} onChange={handle} />
          {errors.price && <span className="form-error">{errors.price}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Remise (%)</label>
          <input name="discount_percent" type="number" min="0" max="100" className="form-input" value={form.discount_percent} onChange={handle} />
        </div>
        <div className="form-group">
          <label className="form-label">Date début *</label>
          <input name="start_date" type="date" className="form-input" value={form.start_date} onChange={handle} />
          {errors.start && <span className="form-error">{errors.start}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Date fin *</label>
          <input name="end_date" type="date" className="form-input" value={form.end_date} onChange={handle} />
          {errors.end && <span className="form-error">{errors.end}</span>}
        </div>
      </div>

      {form.price_per_night > 0 && (
        <div className="tariff-preview">
          <span className="tariff-preview__label">Prix final après remise :</span>
          <span className="tariff-preview__value">{finalPrice} {CURRENCY}</span>
          {form.discount_percent > 0 && (
            <span className="tariff-preview__badge">-{form.discount_percent}%</span>
          )}
        </div>
      )}

      <div className="form-group" style={{ marginTop: '1rem', flexDirection: 'row', alignItems: 'center', gap: 10 }}>
        <input
          type="checkbox"
          id="is_active"
          name="is_active"
          checked={form.is_active}
          onChange={handle}
          style={{ width: 16, height: 16 }}
        />
        <label htmlFor="is_active" className="form-label" style={{ margin: 0, cursor: 'pointer' }}>
          Tarif actif
        </label>
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

// ─────────────────────────────────────────────────────────────────────────────
// Badge saison
// ─────────────────────────────────────────────────────────────────────────────

const SeasonBadge = ({ season }) => {
  const map = {
    low:  { label: 'Basse',      cls: 'season--low' },
    mid:  { label: 'Moyenne',    cls: 'season--mid' },
    high: { label: 'Haute',      cls: 'season--high' },
    peak: { label: 'Très haute', cls: 'season--peak' },
  };
  const cfg = map[season] || { label: season, cls: '' };
  return <span className={`season-badge ${cfg.cls}`}>{cfg.label}</span>;
};

// ─────────────────────────────────────────────────────────────────────────────
// Page principale AdminTariffs
// ─────────────────────────────────────────────────────────────────────────────

const AdminTariffs = ({ toast }) => {
  const [tariffs, setTariffs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal]     = useState(null);
  const [saving, setSaving]   = useState(false);
  const [confirm, setConfirm] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    tariffService.getAll()
      .then(setTariffs)
      .catch(() => toast?.error('Impossible de charger les tarifs'))
      .finally(() => setLoading(false));
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  const { page, totalPages, currentData, goTo } = usePagination(tariffs, 8);

  const handleCreate = async (form) => {
    setSaving(true);
    try {
      await tariffService.create(form);
      toast?.success('Tarif créé');
      setModal(null); load();
    } catch (err) { toast?.error(err.message); }
    finally { setSaving(false); }
  };

  const handleEdit = async (form) => {
    setSaving(true);
    try {
      await tariffService.update(modal.tariff.id, form);
      toast?.success('Tarif mis à jour');
      setModal(null); load();
    } catch (err) { toast?.error(err.message); }
    finally { setSaving(false); }
  };

  const handleDelete = async () => {
    try {
      await tariffService.delete(confirm.id);
      toast?.success('Tarif supprimé');
      setConfirm(null); load();
    } catch (err) { toast?.error(err.message); }
  };

  const columns = [
    { key: 'name',    label: 'Nom du tarif', render: r => <strong>{r.name}</strong> },
    { key: 'season',  label: 'Saison',  render: r => <SeasonBadge season={r.season} /> },
    { key: 'room_type', label: 'Chambre', render: r => TYPE_LABELS[r.room_type] ?? 'Tous' },
    { key: 'price_per_night', label: 'Prix / nuit', render: r => `${r.price_per_night} ${CURRENCY}` },
    {
      key: 'discount_percent', label: 'Remise',
      render: r => r.discount_percent > 0
        ? <span className="discount-badge">-{r.discount_percent}%</span>
        : '—',
    },
    {
      key: 'final', label: 'Prix final',
      render: r => {
        const final = r.price_per_night * (1 - (r.discount_percent || 0) / 100);
        return <strong style={{ color: 'var(--gold-dark)' }}>{final.toFixed(2)} {CURRENCY}</strong>;
      },
    },
    {
      key: 'period', label: 'Période',
      render: r => r.start_date
        ? `${new Date(r.start_date + 'T00:00:00').toLocaleDateString('fr-FR')} → ${new Date(r.end_date + 'T00:00:00').toLocaleDateString('fr-FR')}`
        : '—',
    },
    {
      key: 'is_active', label: 'Statut',
      render: r => <span className={`active-badge ${r.is_active ? 'active-badge--on' : 'active-badge--off'}`}>
        {r.is_active ? 'Actif' : 'Inactif'}
      </span>,
    },
  ];

  return (
    <div>
      {/* ── Section 1 : Prix de base par type ───────────────────────────── */}
      <BasePricesSection toast={toast} />

      {/* ── Section 2 : Tarifs saisonniers ──────────────────────────────── */}
      <PageHeader
        title="Tarifs saisonniers"
        subtitle="Définir les prix selon les saisons et appliquer des remises"
        action={
          <button className="btn btn--primary" onClick={() => setModal('create')}>
            + Nouveau tarif
          </button>
        }
      />

      <DataTable
        columns={columns}
        data={currentData}
        loading={loading}
        empty="Aucun tarif saisonnier défini."
        actions={row => (
          <div style={{ display: 'flex', gap: 6 }}>
            <button className="btn btn--sm btn--outline" onClick={() => setModal({ type: 'edit', tariff: row })}>
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
        <Modal title="Nouveau tarif saisonnier" onClose={() => setModal(null)} size="lg">
          <TariffForm onSubmit={handleCreate} onClose={() => setModal(null)} loading={saving} />
        </Modal>
      )}

      {modal?.type === 'edit' && (
        <Modal title="Modifier le tarif" onClose={() => setModal(null)} size="lg">
          <TariffForm
            initial={{
              ...modal.tariff,
              room_type: fromBackendRoomType(modal.tariff.room_type),
            }}
            onSubmit={handleEdit} onClose={() => setModal(null)} loading={saving}
          />
        </Modal>
      )}

      {confirm && (
        <ConfirmDialog
          title="Supprimer le tarif"
          message={`Supprimer le tarif "${confirm.name}" ?`}
          danger
          onConfirm={handleDelete}
          onCancel={() => setConfirm(null)}
        />
      )}
    </div>
  );
};

export default AdminTariffs;