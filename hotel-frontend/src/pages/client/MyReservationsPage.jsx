import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import reservationService from '../../services/reservationService';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
// Devise centralisée — remplace le littéral '€'
import { CURRENCY } from '../../constants';
import '../../components/common/shared.css';

const TYPE_LABELS = {
  SIMPLE: 'Simple', DOUBLE: 'Double', SUITE: 'Suite', DELUXE: 'Deluxe',
  single: 'Simple', double: 'Double', suite: 'Suite', deluxe: 'Deluxe',
};

const MIN_DAYS_CANCEL = 2; // doit correspondre à MIN_DAYS_BEFORE_CHECKIN côté backend

const getCancellationDeadline = (checkIn) => {
  if (!checkIn) return null;
  const d = new Date(checkIn);
  d.setDate(d.getDate() - MIN_DAYS_CANCEL);
  return d;
};

const MyReservationsPage = () => {
  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [cancelling, setCancelling]     = useState(null);

  const load = () => {
    setLoading(true);
    reservationService.getMyReservations()
      .then(setReservations)
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleCancel = async (id) => {
    if (!window.confirm('Annuler cette réservation ?')) return;
    setCancelling(id);
    try {
      await reservationService.cancel(id);
      load();
    } catch (err) {
      alert(err.message || 'Erreur');
    } finally {
      setCancelling(null);
    }
  };

  const columns = [
    {
      /*
       * CORRECTION #1 : ne pas afficher le numéro de chambre.
       * On affiche le type et la description (données présentes dans room).
       */
      key: 'room',
      label: 'Chambre',
      render: r => {
        const type = r.room?.type;
        const desc = r.room?.description;
        const label = TYPE_LABELS[type] ?? type ?? '—';
        return desc ? `${label} — ${desc}` : label;
      },
    },
    {
      key: 'check_in',
      label: 'Arrivée',
      render: r => {
        const d = r.check_in ?? r.checkInDate;
        return d ? new Date(d).toLocaleDateString('fr-FR') : '—';
      },
    },
    {
      key: 'check_out',
      label: 'Départ',
      render: r => {
        const d = r.check_out ?? r.checkOutDate;
        return d ? new Date(d).toLocaleDateString('fr-FR') : '—';
      },
    },
    {
      key: 'total',
      label: 'Montant estimé',
      render: r => r.total_price ? `${r.total_price} ${CURRENCY}` : '—',
    },
    {
      key: 'status',
      label: 'Statut',
      render: r => {
        const checkIn = r.check_in ?? r.checkInDate;
        const deadline = getCancellationDeadline(checkIn);
        const now = new Date();
        const canCancel = deadline && now <= deadline;
        const isPending = r.status === 'pending' || r.status === 'PENDING';
        const isConfirmed = r.status === 'confirmed' || r.status === 'CONFIRMED';

        return (
          <div>
            <StatusBadge status={r.status} />
            {isPending && (
              <p style={{
                fontSize: '0.72rem',
                color: 'var(--color-text-muted, #888)',
                marginTop: 4,
                lineHeight: 1.3,
              }}>
                En attente de confirmation par la réception
              </p>
            )}
            {(isPending || isConfirmed) && canCancel && deadline && (
              <p style={{
                fontSize: '0.72rem',
                color: 'var(--color-success, #2e7d32)',
                marginTop: 4,
                lineHeight: 1.3,
              }}>
                ✓ Annulation gratuite jusqu'au {deadline.toLocaleDateString('fr-FR')}
              </p>
            )}
            {(isPending || isConfirmed) && !canCancel && deadline && (
              <p style={{
                fontSize: '0.72rem',
                color: 'var(--color-danger, #c0392b)',
                marginTop: 4,
                lineHeight: 1.3,
              }}>
                ✗ Délai d'annulation dépassé
              </p>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <div>
      <PageHeader
        title="Mes réservations"
        subtitle="Historique complet de vos séjours"
        action={
          <button className="btn btn--gold" onClick={() => navigate('/client/rooms')}>
            + Nouvelle réservation
          </button>
        }
      />

      {/*
       * CORRECTION #2 : bandeau informatif si des réservations sont en attente.
       * Permet d'expliquer le processus de validation sans surcharger chaque ligne.
       */}
      {!loading && reservations.some(r =>
        r.status === 'pending' || r.status === 'PENDING'
      ) && (
        <div style={{
          background: 'var(--color-bg-muted, #fff8e7)',
          border: '1px solid var(--color-gold, #c9a227)',
          borderRadius: 8,
          padding: '12px 16px',
          marginBottom: 20,
          fontSize: '0.875rem',
          color: 'var(--color-text, #333)',
        }}>
          <strong>ℹ️ Information :</strong> Vos demandes en attente seront traitées
          par notre réception dans les meilleurs délais. Vous recevrez une confirmation
          dès validation.
        </div>
      )}

      <DataTable
        columns={columns}
        data={reservations}
        loading={loading}
        empty="Vous n'avez encore aucune réservation."
        actions={row => (
          <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
            {(row.status === 'pending' || row.status === 'PENDING' || row.status === 'confirmed' || row.status === 'CONFIRMED') && (
              <button
                className="btn btn--sm btn--danger"
                disabled={cancelling === row.id}
                onClick={() => handleCancel(row.id)}
              >
                {cancelling === row.id ? 'Annulation…' : 'Annuler'}
              </button>
            )}
            {row.invoice_id && (
              <button
                className="btn btn--sm btn--outline"
                onClick={() => navigate('/client/invoices')}
              >
                Facture
              </button>
            )}
          </div>
        )}
      />
    </div>
  );
};

export default MyReservationsPage;