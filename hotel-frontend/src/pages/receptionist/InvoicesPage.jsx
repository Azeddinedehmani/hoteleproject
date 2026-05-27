import React, { useEffect, useState, useCallback } from 'react';
import invoiceService from '../../services/invoiceService';
import reservationService from '../../services/reservationService';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
// FIX H : import de la devise centralisée — remplace les littéraux 'MAD' / '€' éparpillés
import { CURRENCY } from '../../constants';
import '../../components/common/shared.css';

const InvoicesPage = () => {
  const [invoices, setInvoices]         = useState([]);
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [modal, setModal]               = useState(false);
  const [selectedRes, setSelectedRes]   = useState('');
  const [generating, setGenerating]     = useState(false);
  const [genError, setGenError]         = useState('');

  // État pour la confirmation de paiement
  const [payConfirm, setPayConfirm]     = useState(null); // { id, clientName, amount }
  const [paying, setPaying]             = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    Promise.allSettled([
      invoiceService.getAll(),
      // FIX : le backend attend CHECKED_OUT en UPPERCASE, pas checked_out
      reservationService.getAll({ status: 'CHECKED_OUT' }),
    ]).then(([inv, res]) => {
      if (inv.status === 'fulfilled') setInvoices(Array.isArray(inv.value) ? inv.value : []);
      if (res.status === 'rejected') console.warn('Impossible de charger les réservations :', res.reason);
      if (res.status === 'fulfilled') setReservations(Array.isArray(res.value) ? res.value : []);
      setLoading(false);
    });
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleGenerate = async () => {
    if (!selectedRes) return;
    setGenerating(true);
    setGenError('');
    try {
      await invoiceService.generate(selectedRes);
      setModal(false);
      setSelectedRes('');
      load();
    } catch (err) {
      setGenError(err.response?.data?.message || err.message || 'Erreur lors de la génération');
    } finally {
      setGenerating(false);
    }
  };

  const handleCloseModal = () => {
    setModal(false);
    setSelectedRes('');
    setGenError('');
  };

  const handleDownload = async (id) => {
    try {
      const blob = await invoiceService.download(id);
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement('a');
      a.href     = url;
      a.download = `facture-${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      alert('Téléchargement indisponible');
    }
  };

  const handleMarkAsPaid = async () => {
    if (!payConfirm) return;
    setPaying(true);
    try {
      await invoiceService.markAsPaid(payConfirm.id);
      setPayConfirm(null);
      load();
    } catch (err) {
      console.error('Erreur lors du marquage comme payé :', err);
      alert(err.response?.data?.message || 'Erreur lors de la mise à jour du statut');
    } finally {
      setPaying(false);
    }
  };

  const clientName = (r) =>
    r.client?.fullName ??
    (r.client ? [r.client.firstName, r.client.lastName].filter(Boolean).join(' ') : null) ??
    r.client?.name ??
    r.clientName ??
    `Client #${r.clientId ?? r.client_id}`;

  const roomNumber = (r) =>
    r.room?.number ?? r.roomNumber ?? r.roomId ?? r.room_id ?? '—';

  const isUnpaid = (row) => {
    const s = (row.status ?? '').toLowerCase();
    return s === 'unpaid' || s === '';
  };

  const columns = [
    { key: 'id',             label: 'N°',          render: r => `#${r.id}` },
    { key: 'client',         label: 'Client',       render: r => clientName(r) },
    { key: 'reservation_id', label: 'Réservation',  render: r => `#${r.reservationId ?? r.reservation_id ?? '—'}` },
    { key: 'total',          label: 'Montant',      render: r => `${r.totalAmount ?? r.total ?? '—'} ${CURRENCY}` },
    { key: 'created_at',     label: 'Date',         render: r => {
      const d = r.createdAt ?? r.created_at;
      return d ? new Date(d).toLocaleDateString('fr-FR') : '—';
    }},
    { key: 'status', label: 'Statut', render: r => <StatusBadge status={(r.status ?? 'unpaid').toLowerCase()} /> },
  ];

  const uninvoiced = reservations.filter(r =>
    !invoices.find(i => Number(i.reservationId ?? i.reservation_id) === Number(r.id))
  );

  return (
    <div>
      <PageHeader
        title="Facturation"
        subtitle={`${invoices.length} facture${invoices.length > 1 ? 's' : ''} générées`}
        action={
          <button className="btn btn--primary" onClick={() => { setGenError(''); setModal(true); }}>
            + Générer une facture
          </button>
        }
      />

      <DataTable
        columns={columns}
        data={invoices}
        loading={loading}
        empty="Aucune facture générée."
        actions={row => (
          <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
            {/* Bouton Marquer comme payée — visible uniquement si statut = non payée */}
            {isUnpaid(row) && (
              <button
                className="btn btn--sm btn--gold"
                title="Marquer comme payée"
                onClick={() => setPayConfirm({
                  id: row.id,
                  clientName: clientName(row),
                  amount: `${row.totalAmount ?? row.total ?? '—'} ${CURRENCY}`,
                })}
              >
                ✓ Marquer payée
              </button>
            )}
            <button className="btn btn--sm btn--outline" onClick={() => handleDownload(row.id)}>
              ⬇ PDF
            </button>
          </div>
        )}
      />

      {/* Modale génération facture */}
      {modal && (
        <Modal title="Générer une facture" onClose={handleCloseModal} size="sm">
          <div className="form-group" style={{ marginBottom: '1.25rem' }}>
            <label className="form-label">Sélectionner la réservation (checkout)</label>
            <select
              className="form-select"
              value={selectedRes}
              onChange={e => { setSelectedRes(e.target.value); setGenError(''); }}
            >
              <option value="">— choisir —</option>
              {uninvoiced.length === 0 ? (
                <option disabled>Aucune réservation disponible</option>
              ) : (
                uninvoiced.map(r => (
                  <option key={r.id} value={r.id}>
                    #{r.id} — {clientName(r)} — Ch. {roomNumber(r)}
                  </option>
                ))
              )}
            </select>
            {uninvoiced.length === 0 && (
              <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 6 }}>
                Toutes les réservations terminées ont déjà une facture.
              </p>
            )}
          </div>

          {genError && (
            <div className="booking-error" style={{ marginBottom: '1rem' }}>
              {genError}
            </div>
          )}

          <div className="form-actions">
            <button className="btn btn--outline" onClick={handleCloseModal}>Annuler</button>
            <button
              className="btn btn--gold"
              disabled={!selectedRes || generating}
              onClick={handleGenerate}
            >
              {generating ? <span className="dt-spinner" /> : 'Générer'}
            </button>
          </div>
        </Modal>
      )}

      {/* Confirmation paiement */}
      {payConfirm && (
        <ConfirmDialog
          title="Confirmer le paiement"
          message={`Marquer la facture #${payConfirm.id} de ${payConfirm.clientName} (${payConfirm.amount}) comme payée ?`}
          confirmLabel={paying ? 'En cours…' : 'Confirmer'}
          cancelLabel="Annuler"
          onConfirm={handleMarkAsPaid}
          onCancel={() => setPayConfirm(null)}
          confirmDisabled={paying}
        />
      )}
    </div>
  );
};

export default InvoicesPage;
