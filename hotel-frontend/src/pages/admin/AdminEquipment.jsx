import React, { useEffect, useState, useCallback, useMemo } from 'react';
import equipmentService from '../../services/equipmentService';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import SearchBar from '../../components/common/SearchBar';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import Pagination from '../../components/common/Pagination';
import { usePagination } from '../../hooks/usePagination';
import '../../components/common/shared.css';
import './AdminEquipment.css';

const CATEGORIES = ['Confort','Technologie','Salle de bain','Cuisine','Sécurité','Divertissement','Autre'];

const EMPTY_FORM = { name: '', category: 'Confort', description: '', icon: '' };

const ICON_SUGGESTIONS = ['📺','🛋️','❄️','🔒','☕','🛁','🌿','🎮','🖥️','📡','🔑','🌡️','💡','🪞','🏊'];

const EquipmentForm = ({ initial = EMPTY_FORM, onSubmit, onClose, loading }) => {
  const [form, setForm] = useState({ ...EMPTY_FORM, ...initial });
  const [errors, setErrors] = useState({});

  const handle = ({ target: { name, value } }) => {
    setForm(p => ({ ...p, [name]: value }));
    if (errors[name]) setErrors(p => ({ ...p, [name]: '' }));
  };

  const submit = (e) => {
    e.preventDefault();
    if (!form.name.trim()) { setErrors({ name: 'Nom requis' }); return; }
    onSubmit(form);
  };

  return (
    <form onSubmit={submit}>
      <div className="form-grid">
        <div className="form-group">
          <label className="form-label">Nom de l'équipement *</label>
          <input name="name" className="form-input" value={form.name} onChange={handle} placeholder="Ex: Jacuzzi" />
          {errors.name && <span className="form-error">{errors.name}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Catégorie</label>
          <select name="category" className="form-select" value={form.category} onChange={handle}>
            {CATEGORIES.map(c => <option key={c}>{c}</option>)}
          </select>
        </div>
        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
          <label className="form-label">Description</label>
          <textarea name="description" className="form-textarea" value={form.description} onChange={handle} placeholder="Description de l'équipement…" />
        </div>
      </div>

      {/* Icon picker */}
      <div className="form-group" style={{ marginTop: '1rem' }}>
        <label className="form-label">Icône</label>
        <div className="icon-picker">
          {ICON_SUGGESTIONS.map(icon => (
            <button
              type="button"
              key={icon}
              className={`icon-btn ${form.icon === icon ? 'is-selected' : ''}`}
              onClick={() => setForm(p => ({ ...p, icon }))}
            >
              {icon}
            </button>
          ))}
          <input
            name="icon"
            className="form-input"
            value={form.icon}
            onChange={handle}
            placeholder="Ou tapez un emoji…"
            style={{ flex: 1, minWidth: 120 }}
          />
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

const AdminEquipment = ({ toast }) => {
  const [items, setItems]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [modal, setModal]       = useState(null);
  const [saving, setSaving]     = useState(false);
  const [search, setSearch]     = useState('');
  const [catFilter, setCat]     = useState('');
  const [confirm, setConfirm]   = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    equipmentService.getAll()
      .then(setItems)
      .catch(() => toast?.error('Impossible de charger les équipements'))
      .finally(() => setLoading(false));
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  const filtered = useMemo(() => {
    return items.filter(i => {
      const matchSearch = !search || i.name?.toLowerCase().includes(search.toLowerCase());
      const matchCat    = !catFilter || i.category === catFilter;
      return matchSearch && matchCat;
    });
  }, [items, search, catFilter]);

  const { page, totalPages, currentData, goTo, reset } = usePagination(filtered, 10);

  const handleCreate = async (form) => {
    setSaving(true);
    try {
      await equipmentService.create(form);
      toast?.success('Équipement ajouté');
      setModal(null); load();
    } catch (err) { toast?.error(err.message); }
    finally { setSaving(false); }
  };

  const handleEdit = async (form) => {
    setSaving(true);
    try {
      await equipmentService.update(modal.item.id, form);
      toast?.success('Équipement mis à jour');
      setModal(null); load();
    } catch (err) { toast?.error(err.message); }
    finally { setSaving(false); }
  };

  const handleDelete = async () => {
    try {
      await equipmentService.delete(confirm.id);
      toast?.success('Équipement supprimé');
      setConfirm(null); load();
    } catch (err) { toast?.error(err.message); }
  };

  const columns = [
    {
      key: 'name', label: 'Équipement',
      render: r => (
        <div className="eq-cell">
          <span className="eq-cell__icon">{r.icon || '⊟'}</span>
          <span className="eq-cell__name">{r.name}</span>
        </div>
      ),
    },
    {
      key: 'category', label: 'Catégorie',
      render: r => <span className="cat-badge">{r.category ?? '—'}</span>,
    },
    { key: 'description', label: 'Description', render: r => r.description || '—' },
  ];

  return (
    <div>
      <PageHeader
        title="Gestion des équipements"
        subtitle="Définir les équipements disponibles pour les chambres"
        action={
          <button className="btn btn--primary" onClick={() => setModal('create')}>
            + Ajouter équipement
          </button>
        }
      />

      <div className="adm-toolbar">
        <SearchBar
          value={search}
          onChange={v => { setSearch(v); reset(); }}
          placeholder="Rechercher un équipement…"
        />
        <div className="adm-toolbar__filters">
          {['', ...CATEGORIES].map(c => (
            <button key={c}
              className={`btn btn--sm ${catFilter === c ? 'btn--primary' : 'btn--outline'}`}
              onClick={() => { setCat(c); reset(); }}>
              {c || 'Tous'}
            </button>
          ))}
        </div>
      </div>

      <DataTable
        columns={columns}
        data={currentData}
        loading={loading}
        empty="Aucun équipement enregistré."
        actions={row => (
          <div style={{ display: 'flex', gap: 6 }}>
            <button className="btn btn--sm btn--outline" onClick={() => setModal({ type: 'edit', item: row })}>
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
        <Modal title="Ajouter un équipement" onClose={() => setModal(null)} size="lg">
          <EquipmentForm onSubmit={handleCreate} onClose={() => setModal(null)} loading={saving} />
        </Modal>
      )}

      {modal?.type === 'edit' && (
        <Modal title="Modifier l'équipement" onClose={() => setModal(null)} size="lg">
          <EquipmentForm initial={modal.item} onSubmit={handleEdit} onClose={() => setModal(null)} loading={saving} />
        </Modal>
      )}

      {confirm && (
        <ConfirmDialog
          title="Supprimer l'équipement"
          message={`Supprimer "${confirm.name}" ?`}
          danger
          onConfirm={handleDelete}
          onCancel={() => setConfirm(null)}
        />
      )}
    </div>
  );
};

export default AdminEquipment;