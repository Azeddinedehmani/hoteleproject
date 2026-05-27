import React, { useEffect, useState, useCallback } from 'react';
import reservationService from '../../services/reservationService';
import roomService from '../../services/roomService';
import clientService from '../../services/clientService';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import StatusBadge from '../../components/common/StatusBadge';
// FIX P : importer ConfirmDialog pour remplacer window.confirm / alert
import ConfirmDialog from '../../components/common/ConfirmDialog';
import '../../components/common/shared.css';

// ✅ FIX : helpers pour normaliser les champs camelCase vs snake_case du backend
const getCheckIn  = (r) => r.checkInDate  ?? r.check_in  ?? '';
const getCheckOut = (r) => r.checkOutDate ?? r.check_out ?? '';
const getStatus   = (r) => (r.status ?? '').toLowerCase();
const clientLabel = (c) =>
  c.fullName || [c.firstName, c.lastName].filter(Boolean).join(' ') || c.name || `Client #${c.id}`;

const STATUSES = ['pending', 'confirmed', 'checked_in', 'checked_out', 'cancelled'];

const EMPTY_FORM = {
  client_id: '', room_id: '', check_in: '', check_out: '', guests: 1, notes: '', status: '',
};

// ✅ FIX : quand on ouvre "Modifier", on normalise les champs pour le formulaire
const reservationToForm = (r) => ({
  client_id: r.clientId  ?? r.client_id  ?? r.client?.id ?? '',
  room_id:   r.roomId    ?? r.room_id    ?? r.room?.id   ?? '',
  check_in:  (r.checkInDate  ?? r.check_in  ?? '').split('T')[0],
  check_out: (r.checkOutDate ?? r.check_out ?? '').split('T')[0],
  guests:    r.guests ?? 1,
  notes:     r.notes  ?? '',
  status:    getStatus(r),
});

const ReservationForm = ({ initial = EMPTY_FORM, clients, rooms, onSubmit, onClose, loading }) => {
  const [form, setForm]     = useState(initial);
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.client_id) e.client_id = 'Client requis';
    // CORRIGÉ — Bug #4 : vérification que room_id est sélectionné ET que la chambre est disponible
    if (!form.room_id) {
      e.room_id = 'Veuillez sélectionner une chambre.';
    } else {
      const selectedRoom = rooms.find(r => String(r.id) === String(form.room_id));
      if (selectedRoom && (selectedRoom.status ?? '').toLowerCase() !== 'available') {
        e.room_id = 'La chambre sélectionnée n\'est pas disponible. Veuillez en choisir une autre.';
      }
    }
    if (!form.check_in)  e.check_in  = "Date d'arrivée requise";
    if (!form.check_out) e.check_out = 'Date de départ requise';
    if (form.check_in && form.check_out && form.check_out <= form.check_in)
      e.check_out = "Le départ doit être après l'arrivée";
    return e;
  };

  const handle = ({ target: { name, value } }) => {
    setForm(p => ({ ...p, [name]: value }));
    if (errors[name]) setErrors(p => ({ ...p, [name]: '' }));
  };

  const submit = (e) => {
    e.preventDefault();
    const e2 = validate();
    if (Object.keys(e2).length) { setErrors(e2); return; }
    onSubmit(form);
  };

  return (
    <form onSubmit={submit}>
      <div className="form-grid">

        <div className="form-group">
          <label className="form-label">Client *</label>
          <select name="client_id" className="form-select" value={form.client_id} onChange={handle}>
            <option value="">Sélectionner…</option>
            {clients.map(c => (
              <option key={c.id} value={c.id}>{clientLabel(c)}</option>
            ))}
          </select>
          {errors.client_id && <span className="form-error">{errors.client_id}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">Chambre *</label>
          <select name="room_id" className="form-select" value={form.room_id} onChange={handle}>
            <option value="">Sélectionner…</option>
            {rooms.map(r => (
              <option key={r.id} value={r.id}>Chambre {r.number} — {r.type}</option>
            ))}
          </select>
          {errors.room_id && <span className="form-error">{errors.room_id}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">Arrivée *</label>
          <input
            type="date" name="check_in" className="form-input"
            value={form.check_in}
            min={new Date().toISOString().split('T')[0]}
            onChange={handle}
          />
          {errors.check_in && <span className="form-error">{errors.check_in}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">Départ *</label>
          <input
            type="date" name="check_out" className="form-input"
            value={form.check_out}
            min={form.check_in
              ? new Date(new Date(form.check_in).getTime() + 86400000).toISOString().split('T')[0]
              : new Date().toISOString().split('T')[0]
            }
            onChange={handle}
          />
          {errors.check_out && <span className="form-error">{errors.check_out}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">Personnes</label>
          <input
            type="number" name="guests" className="form-input"
            value={form.guests} min={1} max={10} onChange={handle}
          />
        </div>

        <div className="form-group">
          <label className="form-label">Statut</label>
          <select name="status" className="form-select" value={form.status} onChange={handle}>
            <option value="">— inchangé —</option>
            {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>

      </div>

      <div className="form-group" style={{ marginTop: '1rem' }}>
        <label className="form-label">Notes</label>
        <textarea
          name="notes" className="form-textarea"
          value={form.notes} onChange={handle} placeholder="Remarques…"
        />
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

const ReservationsPage = () => {
  const [reservations, setReservations] = useState([]);
  const [clients, setClients]           = useState([]);
  const [rooms, setRooms]               = useState([]);
  const [loading, setLoading]           = useState(true);
  const [modal, setModal]               = useState(null);
  const [saving, setSaving]             = useState(false);
  const [statusFilter, setStatusFilter] = useState('');
  const [error, setError]               = useState('');
  // FIX P : remplace window.confirm — stocke la réservation à annuler
  const [confirmCancel, setConfirmCancel] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    Promise.allSettled([
      reservationService.getAll(),
      clientService.getAll(),
      roomService.getAll(),
    ]).then(([r, c, ro]) => {
      // FIX Q : collecter les erreurs de chargement et les afficher à l'utilisateur
      const loadErrors = [];
      if (r.status  === 'fulfilled') setReservations(r.value);
      else loadErrors.push('réservations');
      // FIX O : suppression du console.log de débogage
      if (c.status  === 'fulfilled') setClients(c.value);
      else loadErrors.push('clients');
      if (ro.status === 'fulfilled') setRooms(ro.value);
      else loadErrors.push('chambres');
      if (loadErrors.length) {
        setError(`Impossible de charger : ${loadErrors.join(', ')}. Veuillez recharger la page.`);
      }
      setLoading(false);
    });
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleCreate = async (form) => {
    setSaving(true); setError('');
    try {
      // FIX D : transmettre la liste rooms pour que le service puisse déduire roomType depuis room_id
      await reservationService.create({ ...form, rooms });
      setModal(null); load();
    }
    catch (err) { setError(err.message || 'Erreur création'); }
    finally { setSaving(false); }
  };

  const handleEdit = async (form) => {
    setSaving(true); setError('');
    try { await reservationService.update(modal.res.id, form); setModal(null); load(); }
    catch (err) { setError(err.message || 'Erreur mise à jour'); }
    finally { setSaving(false); }
  };

  // FIX P : ne plus utiliser window.confirm — ouvre le ConfirmDialog à la place
  const handleCancel = (row) => {
    setConfirmCancel(row);
  };

  const doCancel = async () => {
    const id = confirmCancel?.id;
    setConfirmCancel(null);
    if (!id) return;
    try { await reservationService.cancel(id); load(); }
    // FIX P : remplace alert() par setError()
    catch (err) { setError(err.message || 'Erreur lors de l\'annulation'); }
  };

  // ✅ FIX : filtre normalisé (le statut backend peut être UPPERCASE)
  const filtered = statusFilter
    ? reservations.filter(r => getStatus(r) === statusFilter)
    : reservations;

  // ✅ FIX : affichage nom client robuste
  const displayClient = (r) =>
    r.client ? clientLabel(r.client) : `#${r.clientId ?? r.client_id}`;

  const columns = [
    { key: 'id',       label: '#',       render: r => `#${r.id}` },
    { key: 'client',   label: 'Client',  render: r => displayClient(r) },
    { key: 'room',     label: 'Chambre', render: r => `Ch. ${r.room?.number ?? r.roomId ?? r.room_id}` },
    { key: 'check_in',  label: 'Arrivée', render: r => {
      const d = getCheckIn(r); return d ? new Date(d).toLocaleDateString('fr-FR') : '—';
    }},
    { key: 'check_out', label: 'Départ',  render: r => {
      const d = getCheckOut(r); return d ? new Date(d).toLocaleDateString('fr-FR') : '—';
    }},
    { key: 'status',   label: 'Statut',  render: r => (
      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
        <StatusBadge status={r.status} />
        {getStatus(r) === 'pending' && (
          <span
            title="Demande de réservation en attente de confirmation"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: '20px',
              height: '20px',
              borderRadius: '50%',
              background: '#f59e0b',
              color: '#fff',
              fontSize: '12px',
              fontWeight: '700',
              lineHeight: 1,
              flexShrink: 0,
              cursor: 'default',
              boxShadow: '0 0 0 3px rgba(245,158,11,0.25)',
              animation: 'pulse-alert 1.8s ease-in-out infinite',
            }}
          >
            !
          </span>
        )}
      </span>
    )},
  ];

  return (
    <div>
      <PageHeader
        title="Gestion des réservations"
        subtitle={
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '10px' }}>
            {`${reservations.length} réservation${reservations.length > 1 ? 's' : ''} au total`}
            {reservations.filter(r => getStatus(r) === 'pending').length > 0 && (
              <span style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '5px',
                background: 'rgba(245,158,11,0.15)',
                border: '1px solid #f59e0b',
                borderRadius: '20px',
                padding: '2px 10px',
                fontSize: '12px',
                fontWeight: '600',
                color: '#b45309',
              }}>
                <span style={{
                  display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                  width: '16px', height: '16px', borderRadius: '50%',
                  background: '#f59e0b', color: '#fff', fontSize: '10px', fontWeight: '700',
                }}>!</span>
                {reservations.filter(r => getStatus(r) === 'pending').length} en attente de confirmation
              </span>
            )}
          </span>
        }
        action={
          <button className="btn btn--primary" onClick={() => { setError(''); setModal('create'); }}>
            + Nouvelle réservation
          </button>
        }
      />

      {/* Filtres statut */}
      <div style={{ marginBottom: '1rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        {['', ...STATUSES].map(s => (
          <button
            key={s}
            className={`btn btn--sm ${statusFilter === s ? 'btn--primary' : 'btn--outline'}`}
            onClick={() => setStatusFilter(s)}
          >
            {s === '' ? 'Tous' : <StatusBadge status={s} />}
          </button>
        ))}
      </div>

      <DataTable
        columns={columns}
        data={filtered}
        loading={loading}
        empty="Aucune réservation."
        actions={row => (
          <div style={{ display: 'flex', gap: 6 }}>
            <button
              className="btn btn--sm btn--outline"
              onClick={() => { setError(''); setModal({ type: 'edit', res: row }); }}
            >
              Modifier
            </button>
            {!['cancelled', 'checked_out'].includes(getStatus(row)) && (
              // FIX P : passe l'objet row entier pour l'affichage dans ConfirmDialog
              <button className="btn btn--sm btn--danger" onClick={() => handleCancel(row)}>
                Annuler
              </button>
            )}
          </div>
        )}
      />

      {modal === 'create' && (
        <Modal title="Nouvelle réservation" onClose={() => setModal(null)} size="lg">
          {error && <div className="booking-error" style={{ margin: '0 0 1rem' }}>{error}</div>}
          <ReservationForm
            clients={clients} rooms={rooms}
            onSubmit={handleCreate} onClose={() => setModal(null)} loading={saving}
          />
        </Modal>
      )}

      {modal?.type === 'edit' && (
        <Modal title="Modifier la réservation" onClose={() => setModal(null)} size="lg">
          {error && <div className="booking-error" style={{ margin: '0 0 1rem' }}>{error}</div>}
          <ReservationForm
            // ✅ FIX : normalise les champs avant de passer au formulaire
            initial={reservationToForm(modal.res)}
            clients={clients} rooms={rooms}
            onSubmit={handleEdit} onClose={() => setModal(null)} loading={saving}
          />
        </Modal>
      )}

      {/* FIX P : ConfirmDialog remplace window.confirm pour l'annulation */}
      {confirmCancel && (
        <ConfirmDialog
          title="Annuler la réservation"
          message={`Annuler la réservation #${confirmCancel.id} ?`}
          danger
          onConfirm={doCancel}
          onCancel={() => setConfirmCancel(null)}
        />
      )}
    </div>
  );
};

export default ReservationsPage;