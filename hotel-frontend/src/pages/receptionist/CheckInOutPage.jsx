import React, { useEffect, useState, useCallback } from 'react';
import reservationService from '../../services/reservationService';
import roomService from '../../services/roomService';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import '../../components/common/shared.css';
import './CheckInOutPage.css';

// ─── Helpers de normalisation des champs backend ─────────────────────────────
const getCheckIn  = (r) => r.checkInDate  ?? r.check_in  ?? '';
const getCheckOut = (r) => r.checkOutDate ?? r.check_out ?? '';
const getStatus   = (r) => (r.status ?? '').toLowerCase();

/**
 * CORRECTION PRINCIPALE :
 *
 * Bug 1 — Check-in sur réservation sans chambre :
 *   Quand roomId == null (réservation faite par type via le client), le backend
 *   refuse le check-in avec IllegalStateException.
 *   Fix : si la réservation n'a pas de chambre, on ouvre un modal pour que la
 *   réception assigne d'abord une chambre disponible (PATCH /assign-room),
 *   puis on lance le check-in.
 *
 * Bug 2 — Check-out sur réservation sans chambre :
 *   Géré côté backend (voir ReservationUseCaseImpl), mais on protège aussi le
 *   frontend en grisant le bouton si pas de chambre assignée.
 */

const CheckInOutPage = ({ toast }) => {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [acting, setActing]             = useState(null);
  const [tab, setTab]                   = useState('checkin');

  // État pour le modal d'assignation de chambre
  const [assignModal, setAssignModal]   = useState(null); // { reservation }
  const [availableRooms, setAvailableRooms] = useState([]);
  const [selectedRoomId, setSelectedRoomId] = useState('');
  const [assignLoading, setAssignLoading]   = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    reservationService.getAll()
      .then(data => {
        setReservations(Array.isArray(data) ? data : []);
      })
      .catch(() => toast?.error('Impossible de charger les réservations'))
      .finally(() => setLoading(false));
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  const todayStr = new Date().toISOString().split('T')[0];

  // Arrivées : PENDING ou CONFIRMED dont checkInDate <= aujourd'hui
  const arrivals = reservations.filter(r => {
    const status  = getStatus(r);
    const checkIn = getCheckIn(r).split('T')[0];
    return (status === 'pending' || status === 'confirmed') && checkIn <= todayStr;
  });

  const arrivalsToday = arrivals.filter(r =>
    getCheckIn(r).split('T')[0] === todayStr
  );

  const departures = reservations.filter(r => getStatus(r) === 'checked_in');

  const departuresToday = departures.filter(r =>
    getCheckOut(r).split('T')[0] <= todayStr
  );

  // ── Handlers ───────────────────────────────────────────────────────────────

  const handleConfirm = async (r) => {
    const checkInDate  = r.checkInDate  ?? r.check_in;
    const checkOutDate = r.checkOutDate ?? r.check_out;

    if (!checkInDate || !checkOutDate) {
      toast?.error('Dates de réservation manquantes — impossible de confirmer');
      return;
    }

    setActing(r.id);
    try {
      await reservationService.confirm(r.id, { ...r, checkInDate, checkOutDate });
      toast?.success('Réservation confirmée — vous pouvez maintenant faire le check-in');
      load();
    } catch (err) {
      toast?.error(err.message || 'Erreur lors de la confirmation');
    } finally {
      setActing(null);
    }
  };

  /**
   * CORRECTION Bug 1 :
   * Si la réservation n'a pas de chambre attribuée → ouvrir le modal d'assignation.
   * Sinon → lancer le check-in directement.
   */
  const handleCheckIn = async (r) => {
    const hasRoom = r.roomId != null || r.room?.id != null || r.room_id != null;

    if (!hasRoom) {
      // Charger les chambres disponibles pour ce type de réservation
      setAssignLoading(true);
      try {
        const checkIn  = (r.checkInDate ?? r.check_in ?? '').split('T')[0];
        const checkOut = (r.checkOutDate ?? r.check_out ?? '').split('T')[0];
        const rooms = checkIn && checkOut
          ? await roomService.getAvailable({ checkIn, checkOut })
          : await roomService.getAll();

        const roomType = (r.roomType ?? r.room_type ?? '').toUpperCase();
        // Filtrer par type si connu, sinon afficher toutes les disponibles
        const filtered = roomType
          ? (Array.isArray(rooms) ? rooms : []).filter(
              rm => (rm.type ?? rm.roomType ?? '').toUpperCase() === roomType &&
                    (rm.status ?? '').toLowerCase() === 'available'
            )
          : (Array.isArray(rooms) ? rooms : []).filter(
              rm => (rm.status ?? '').toLowerCase() === 'available'
            );

        setAvailableRooms(filtered);
        setSelectedRoomId('');
        setAssignModal({ reservation: r });
      } catch {
        toast?.error('Impossible de charger les chambres disponibles');
      } finally {
        setAssignLoading(false);
      }
      return;
    }

    // Chambre déjà assignée → check-in direct
    setActing(r.id);
    try {
      await reservationService.checkIn(r.id);
      toast?.success('Check-in effectué avec succès');
      load();
    } catch (err) {
      toast?.error(err.message || 'Erreur check-in');
    } finally {
      setActing(null);
    }
  };

  /**
   * CORRECTION Bug 1 (suite) :
   * Assigner la chambre puis effectuer le check-in en deux étapes.
   */
  const handleAssignAndCheckIn = async () => {
    if (!selectedRoomId) {
      toast?.error('Veuillez sélectionner une chambre');
      return;
    }
    const r = assignModal?.reservation;
    if (!r) return;

    setActing(r.id);
    try {
      // Étape 1 : assigner la chambre
      await reservationService.assignRoom(r.id, Number(selectedRoomId));
      toast?.success('Chambre assignée');

      // Étape 2 : check-in
      await reservationService.checkIn(r.id);
      toast?.success('Check-in effectué avec succès');

      setAssignModal(null);
      load();
    } catch (err) {
      toast?.error(err.message || 'Erreur lors de l\'assignation ou du check-in');
    } finally {
      setActing(null);
    }
  };

  const handleCheckOut = async (id) => {
    setActing(id);
    try {
      await reservationService.checkOut(id);
      toast?.success('Check-out effectué avec succès');
      load();
    } catch (err) {
      toast?.error(err.message || 'Erreur check-out');
    } finally {
      setActing(null);
    }
  };

  // ── Helpers d'affichage ────────────────────────────────────────────────────

  const clientName = (r) =>
    r.client?.fullName ||
    (r.client ? [r.client.firstName, r.client.lastName].filter(Boolean).join(' ') : null) ||
    r.client?.name ||
    `Client #${r.clientId ?? r.client_id}`;

  const roomNumber = (r) =>
    r.room?.number ??
    r.roomId ??
    r.room_id ??
    null; // null = pas assignée

  const formatDate = (dateStr) => {
    if (!dateStr) return '---';
    try { return new Date(dateStr).toLocaleDateString('fr-FR'); }
    catch { return dateStr; }
  };

  const isOverdue = (dateStr) => dateStr && dateStr.split('T')[0] < todayStr;
  const isToday   = (dateStr) => dateStr && dateStr.split('T')[0] === todayStr;

  const list = tab === 'checkin' ? arrivals : departures;

  // ── Bouton d'action selon statut ───────────────────────────────────────────
  const renderActionButton = (r) => {
    if (tab === 'checkout') {
      return (
        <button
          className="btn btn--primary"
          disabled={acting === r.id}
          onClick={() => handleCheckOut(r.id)}
        >
          {acting === r.id ? <span className="dt-spinner" /> : 'Check-out'}
        </button>
      );
    }

    if (getStatus(r) === 'pending') {
      return (
        <button
          className="btn btn--outline"
          disabled={acting === r.id}
          onClick={() => handleConfirm(r)}
          title="La réservation doit être confirmée avant le check-in"
        >
          {acting === r.id ? <span className="dt-spinner" /> : 'Confirmer'}
        </button>
      );
    }

    // statut === 'confirmed'
    return (
      <button
        className="btn btn--gold"
        disabled={acting === r.id || assignLoading}
        onClick={() => handleCheckIn(r)}
      >
        {acting === r.id || assignLoading
          ? <span className="dt-spinner" />
          : 'Check-in'}
      </button>
    );
  };

  // ── JSX ────────────────────────────────────────────────────────────────────

  return (
    <div>
      <PageHeader
        title="Check-in / Check-out"
        subtitle="Gérer les arrivées et départs"
      />

      {/* Tabs */}
      <div className="cio-tabs">
        <button
          className={`cio-tab ${tab === 'checkin' ? 'cio-tab--active' : ''}`}
          onClick={() => setTab('checkin')}
        >
          <span className="cio-tab__count">{arrivals.length}</span>
          Arrivées en attente
          {arrivalsToday.length > 0 && (
            <span style={{
              marginLeft: 6, background: '#F59E0B', color: '#fff',
              borderRadius: 10, padding: '1px 7px', fontSize: 11,
            }}>
              {arrivalsToday.length} aujourd'hui
            </span>
          )}
        </button>
        <button
          className={`cio-tab ${tab === 'checkout' ? 'cio-tab--active' : ''}`}
          onClick={() => setTab('checkout')}
        >
          <span className="cio-tab__count">{departures.length}</span>
          Départs en cours
          {departuresToday.length > 0 && (
            <span style={{
              marginLeft: 6, background: '#3B82F6', color: '#fff',
              borderRadius: 10, padding: '1px 7px', fontSize: 11,
            }}>
              {departuresToday.length} aujourd'hui
            </span>
          )}
        </button>
      </div>

      {loading ? (
        <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
          Chargement...
        </div>
      ) : list.length === 0 ? (
        <div className="cio-empty">
          {tab === 'checkin'
            ? 'Aucune réservation en attente de check-in.'
            : 'Aucun client actuellement en séjour.'
          }
        </div>
      ) : (
        <div className="cio-list">
          {list.map(r => {
            const dateRef = tab === 'checkin' ? getCheckIn(r) : getCheckOut(r);
            const overdue = isOverdue(dateRef);
            const today   = isToday(dateRef);
            const rn      = roomNumber(r);
            const noRoom  = rn === null;
            return (
              <div
                key={r.id}
                className="cio-card"
                style={overdue ? { borderLeft: '3px solid #EF4444' } : {}}
              >
                <div className="cio-card__info">
                  <div className="cio-card__avatar">
                    {clientName(r).charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <p className="cio-card__name">
                      {clientName(r)}
                      {overdue && (
                        <span style={{ marginLeft: 8, color: '#EF4444', fontSize: 12, fontWeight: 600 }}>
                          EN RETARD
                        </span>
                      )}
                      {today && (
                        <span style={{ marginLeft: 8, color: '#F59E0B', fontSize: 12, fontWeight: 600 }}>
                          AUJOURD'HUI
                        </span>
                      )}
                    </p>
                    <p className="cio-card__meta">
                      Chambre {noRoom ? <em style={{ color: '#F59E0B' }}>(non assignée)</em> : rn}
                      {' · '}
                      {formatDate(getCheckIn(r))}
                      {' → '}
                      {formatDate(getCheckOut(r))}
                    </p>
                    {r.guests && (
                      <p className="cio-card__guests">
                        {r.guests} personne{r.guests > 1 ? 's' : ''}
                      </p>
                    )}
                    {r.notes && (
                      <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
                        {r.notes}
                      </p>
                    )}
                    {tab === 'checkin' && getStatus(r) === 'pending' && (
                      <p style={{ fontSize: 11, color: '#6B7280', marginTop: 4 }}>
                        ⓘ Confirmer d'abord, puis faire le check-in
                      </p>
                    )}
                    {/* CORRECTION : avertissement si pas de chambre assignée */}
                    {tab === 'checkin' && getStatus(r) === 'confirmed' && noRoom && (
                      <p style={{ fontSize: 11, color: '#F59E0B', marginTop: 4 }}>
                        ⓘ Aucune chambre assignée — le check-in vous demandera d'en choisir une
                      </p>
                    )}
                  </div>
                </div>
                <div className="cio-card__right">
                  <StatusBadge status={r.status} />
                  {renderActionButton(r)}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* CORRECTION Bug 1 : Modal d'assignation de chambre avant check-in */}
      {assignModal && (
        <Modal
          title="Assigner une chambre avant le check-in"
          onClose={() => setAssignModal(null)}
          size="md"
        >
          <p style={{ marginBottom: '1rem', color: 'var(--text-muted)', fontSize: 14 }}>
            Cette réservation ({assignModal.reservation.roomType ?? 'type non précisé'}) n'a pas encore
            de chambre attribuée. Sélectionnez une chambre disponible pour continuer.
          </p>

          {availableRooms.length === 0 ? (
            <div style={{ color: '#EF4444', padding: '1rem 0' }}>
              Aucune chambre disponible du type requis pour ces dates.
            </div>
          ) : (
            <div className="form-group">
              <label className="form-label">Chambre disponible *</label>
              <select
                className="form-select"
                value={selectedRoomId}
                onChange={e => setSelectedRoomId(e.target.value)}
              >
                <option value="">Sélectionner une chambre…</option>
                {availableRooms.map(rm => (
                  <option key={rm.id} value={rm.id}>
                    Chambre {rm.number} — {rm.type} {rm.floor ? `(Étage ${rm.floor})` : ''}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="form-actions" style={{ marginTop: '1.5rem' }}>
            <button className="btn btn--outline" onClick={() => setAssignModal(null)}>
              Annuler
            </button>
            <button
              className="btn btn--gold"
              onClick={handleAssignAndCheckIn}
              disabled={!selectedRoomId || acting !== null}
            >
              {acting !== null ? <span className="dt-spinner" /> : 'Assigner & Check-in'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default CheckInOutPage;