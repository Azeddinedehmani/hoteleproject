import React, { useEffect, useState } from 'react';
import invoiceService from '../../services/invoiceService';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
// Devise centralisée — remplace le littéral '€'
import { CURRENCY } from '../../constants';
import '../../components/common/shared.css';

const MyInvoicesPage = () => {
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    invoiceService.getMyInvoices()
      .then(setInvoices)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleDownload = async (id) => {
    try {
      const blob = await invoiceService.download(id);
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement('a');
      a.href     = url;
      a.download = `facture-${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      alert('Téléchargement indisponible');
    }
  };

  const columns = [
    { key: 'id', label: 'N° Facture', render: r => `#${r.id}` },
    {
      key: 'created_at',
      label: 'Date',
      render: r => new Date(r.created_at).toLocaleDateString('fr-FR'),
    },
    {
      key: 'reservation',
      label: 'Réservation',
      render: r => r.reservation_id ? `#${r.reservation_id}` : '—',
    },
    { key: 'total', label: 'Montant', render: r => `${r.total ?? '—'} ${CURRENCY}` },
    { key: 'status', label: 'Statut', render: r => <StatusBadge status={r.status ?? 'unpaid'} /> },
  ];

  return (
    <div>
      <PageHeader title="Mes factures" subtitle="Vos documents de facturation" />
      <DataTable
        columns={columns}
        data={invoices}
        loading={loading}
        empty="Aucune facture disponible pour le moment."
        actions={row => (
          <button
            className="btn btn--sm btn--outline"
            onClick={() => handleDownload(row.id)}
          >
            ⬇ PDF
          </button>
        )}
      />
    </div>
  );
};

export default MyInvoicesPage;