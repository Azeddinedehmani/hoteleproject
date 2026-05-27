import React, { useEffect, useState, useCallback } from 'react';
import clientService from '../../services/clientService';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import '../../components/common/shared.css';

// FIX : ajout du champ cin (requis par le backend CreateClientRequest)
const EMPTY_FORM = { name: '', email: '', phone: '', cin: '' };

const ClientForm = ({ initial = EMPTY_FORM, onSubmit, onClose, loading, serverError }) => {
  const [form, setForm] = useState({ ...EMPTY_FORM, ...initial });
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.name.trim())  e.name  = 'Nom requis';
    if (!form.email.trim()) e.email = 'Email requis';
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Email invalide';
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
      {serverError && (
        <div className="booking-error" style={{ marginBottom: '1rem' }}>
          {serverError}
        </div>
      )}
      <div className="form-grid">
        <div className="form-group">
          <label className="form-label">Nom complet *</label>
          <input name="name" className="form-input" value={form.name} onChange={handle} placeholder="Jean Dupont" />
          {errors.name && <span className="form-error">{errors.name}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Email *</label>
          <input name="email" type="email" className="form-input" value={form.email} onChange={handle} />
          {errors.email && <span className="form-error">{errors.email}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Téléphone</label>
          {/* FIX : placeholder avec format international pour guider l'utilisateur */}
          <input name="phone" type="tel" className="form-input" value={form.phone} onChange={handle} placeholder="+212658188363" />
        </div>
        <div className="form-group">
          {/* FIX : champ CIN ajouté — transmis au backend */}
          <label className="form-label">CIN</label>
          <input name="cin" className="form-input" value={form.cin} onChange={handle} placeholder="AB123456" />
        </div>
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

const ClientsPage = () => {
  const [clients, setClients]           = useState([]);
  const [loading, setLoading]           = useState(true);
  const [modal, setModal]               = useState(null);
  const [saving, setSaving]             = useState(false);
  const [search, setSearch]             = useState('');
  const [serverError, setServerError]   = useState('');
  const [confirm, setConfirm]           = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    clientService.getAll()
      .then(setClients)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  const extractError = (err) =>
    err?.response?.data?.message ||
    err?.response?.data?.error   ||
    err?.message                 ||
    'Une erreur est survenue';

  const handleCreate = async (form) => {
    setSaving(true);
    setServerError('');
    try {
      await clientService.create(form);
      setModal(null);
      load();
    } catch (err) {
      setServerError(extractError(err));
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = async (form) => {
    setSaving(true);
    setServerError('');
    try {
      await clientService.update(modal.client.id, form);
      setModal(null);
      load();
    } catch (err) {
      setServerError(extractError(err));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      await clientService.delete(confirm.id);
      setConfirm(null);
      load();
    } catch (err) {
      alert(extractError(err));
    }
  };

  const openCreate = () => { setServerError(''); setModal('create'); };
  const openEdit   = (c) => { setServerError(''); setModal({ type: 'edit', client: c }); };

  const filtered = clients.filter(c =>
    !search ||
    `${c.firstName} ${c.lastName}`.toLowerCase().includes(search.toLowerCase()) ||
    c.fullName?.toLowerCase().includes(search.toLowerCase()) ||
    c.email?.toLowerCase().includes(search.toLowerCase())
  );

  const columns = [
    { key: 'fullName', label: 'Nom',       render: c => c.fullName ?? `${c.firstName} ${c.lastName}` },
    { key: 'email',    label: 'Email' },
    { key: 'phone',    label: 'Téléphone' },
    { key: 'cin',      label: 'CIN' },
  ];

  return (
    <div>
      <PageHeader
        title="Gestion des clients"
        subtitle={`${clients.length} client${clients.length > 1 ? 's' : ''} enregistrés`}
        action={
          <button className="btn btn--primary" onClick={openCreate}>
            + Ajouter client
          </button>
        }
      />

      <div style={{ marginBottom: '1rem' }}>
        <input
          type="text"
          className="form-input"
          placeholder="Rechercher par nom ou email…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ maxWidth: 340 }}
        />
      </div>

      <DataTable
        columns={columns}
        data={filtered}
        loading={loading}
        empty="Aucun client trouvé."
        actions={row => (
          <div style={{ display: 'flex', gap: 6 }}>
            <button className="btn btn--sm btn--outline" onClick={() => openEdit(row)}>Modifier</button>
            <button className="btn btn--sm btn--danger"  onClick={() => setConfirm(row)}>Supprimer</button>
          </div>
        )}
      />

      {modal === 'create' && (
        <Modal title="Ajouter un client" onClose={() => setModal(null)}>
          <ClientForm onSubmit={handleCreate} onClose={() => setModal(null)} loading={saving} serverError={serverError} />
        </Modal>
      )}

      {modal?.type === 'edit' && (
        <Modal title="Modifier le client" onClose={() => setModal(null)}>
          <ClientForm
            initial={{
              name:  modal.client.fullName ?? `${modal.client.firstName} ${modal.client.lastName}`,
              email: modal.client.email,
              phone: modal.client.phone,
              cin:   modal.client.cin,
            }}
            onSubmit={handleEdit}
            onClose={() => setModal(null)}
            loading={saving}
            serverError={serverError}
          />
        </Modal>
      )}

      {confirm && (
        <ConfirmDialog
          title="Supprimer le client"
          message={`Supprimer "${confirm.fullName ?? confirm.firstName}" ? Cette action est irréversible.`}
          danger
          onConfirm={handleDelete}
          onCancel={() => setConfirm(null)}
        />
      )}
    </div>
  );
};

export default ClientsPage;