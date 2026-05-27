import React, { useEffect, useState, useCallback, useMemo } from 'react';
import userService from '../../services/userService';
import { useAuth } from '../../context/AuthContext';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import SearchBar from '../../components/common/SearchBar';
import Pagination from '../../components/common/Pagination';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import StatusBadge from '../../components/common/StatusBadge';
import { usePagination } from '../../hooks/usePagination';
import '../../components/common/shared.css';
import './AdminUsers.css';

const ROLES = ['admin', 'receptionist', 'client'];
const ROLE_LABELS = { admin: 'Administrateur', receptionist: 'Réceptionniste', client: 'Client' };

const EMPTY_FORM = { name: '', email: '', password: '', role: 'client', phone: '' };

const UserForm = ({ initial = EMPTY_FORM, onSubmit, onClose, loading, isEdit }) => {
  const [form, setForm]     = useState(initial);
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.name.trim())      e.name  = 'Nom requis';
    if (!form.email.trim())     e.email = 'Email requis';
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Email invalide';
    if (!isEdit && !form.password) e.password = 'Mot de passe requis';
    if (!isEdit && form.password?.length < 8) e.password = 'Min. 8 caractères';
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
          <input name="phone" type="tel" className="form-input" value={form.phone} onChange={handle} placeholder="+212 6…" />
        </div>
        <div className="form-group">
          <label className="form-label">Rôle *</label>
          <select name="role" className="form-select" value={form.role} onChange={handle}>
            {ROLES.map(r => <option key={r} value={r}>{ROLE_LABELS[r]}</option>)}
          </select>
        </div>
        {!isEdit && (
          <div className="form-group" style={{ gridColumn: '1 / -1' }}>
            <label className="form-label">Mot de passe *</label>
            <input name="password" type="password" className="form-input" value={form.password} onChange={handle} placeholder="••••••••" />
            {errors.password && <span className="form-error">{errors.password}</span>}
          </div>
        )}
      </div>
      <div className="form-actions">
        <button type="button" className="btn btn--outline" onClick={onClose}>Annuler</button>
        <button type="submit" className="btn btn--primary" disabled={loading}>
          {loading ? <span className="dt-spinner" /> : (isEdit ? 'Mettre à jour' : 'Créer')}
        </button>
      </div>
    </form>
  );
};

const RoleBadge = ({ role }) => {
  const cls = { admin: 'role--admin', receptionist: 'role--rec', client: 'role--client' }[role] || '';
  return <span className={`role-badge ${cls}`}>{ROLE_LABELS[role] ?? role}</span>;
};

// ── Badge statut actif/inactif ──
const ActiveBadge = ({ active }) => (
  <span style={{
    display: 'inline-flex',
    alignItems: 'center',
    gap: 4,
    padding: '2px 10px',
    borderRadius: 12,
    fontSize: '0.78rem',
    fontWeight: 600,
    background: active ? 'var(--success-light, #e8f5e9)' : 'var(--danger-light, #fce4ec)',
    color:      active ? 'var(--success-dark, #2e7d32)' : 'var(--danger-dark, #c62828)',
    border:     `1px solid ${active ? 'var(--success, #4caf50)' : 'var(--danger, #e53935)'}`,
  }}>
    <span style={{ width: 7, height: 7, borderRadius: '50%', background: 'currentColor', display: 'inline-block' }} />
    {active ? 'Actif' : 'Inactif'}
  </span>
);

const AdminUsers = ({ toast }) => {
  const { user: currentUser } = useAuth();
  const [users, setUsers]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [modal, setModal]       = useState(null);
  const [saving, setSaving]     = useState(false);
  const [search, setSearch]     = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [confirm, setConfirm]   = useState(null);
  // FIX #2 : confirmation pour activer/désactiver
  const [toggleConfirm, setToggleConfirm] = useState(null); // { user, action: 'activate'|'deactivate' }

  const load = useCallback(() => {
    setLoading(true);
    userService.getAll()
      .then(setUsers)
      .catch(() => toast?.error('Impossible de charger les utilisateurs'))
      .finally(() => setLoading(false));
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  const filtered = useMemo(() => {
    return users.filter(u => {
      const matchSearch = !search ||
        u.name?.toLowerCase().includes(search.toLowerCase()) ||
        u.email?.toLowerCase().includes(search.toLowerCase());
      const matchRole = !roleFilter || u.role === roleFilter;
      return matchSearch && matchRole;
    });
  }, [users, search, roleFilter]);

  const { page, totalPages, currentData, goTo, reset } = usePagination(filtered, 8);

  const handleCreate = async (form) => {
    setSaving(true);
    try {
      await userService.create(form);
      toast?.success('Utilisateur créé avec succès');
      setModal(null);
      load();
    } catch (err) {
      toast?.error(err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = async (form) => {
    setSaving(true);
    try {
      await userService.update(modal.user.id, form);
      toast?.success('Utilisateur mis à jour');
      setModal(null);
      load();
    } catch (err) {
      toast?.error(err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      await userService.delete(confirm.id);
      toast?.success('Utilisateur supprimé');
      setConfirm(null);
      load();
    } catch (err) {
      toast?.error(err.message);
    }
  };

  // FIX #2 : handler pour activer / désactiver
  const handleToggleActive = async () => {
    if (!toggleConfirm) return;
    const { user, action } = toggleConfirm;
    try {
      if (action === 'activate') {
        await userService.activate(user.id);
        toast?.success(`Compte de "${user.name}" activé`);
      } else {
        await userService.deactivate(user.id);
        toast?.success(`Compte de "${user.name}" désactivé`);
      }
      setToggleConfirm(null);
      load();
    } catch (err) {
      toast?.error(err.message);
    }
  };

  const handleRoleChange = async (user, role) => {
    try {
      await userService.updateRole(user.id, role);
      toast?.success(`Rôle mis à jour : ${ROLE_LABELS[role]}`);
      load();
    } catch (err) {
      toast?.error(err.message);
    }
  };

  const isRoleChangeLocked = (u) => {
    if (!u) return false;
    if (currentUser && u.id === currentUser.id) return true;
    if (u.role === 'admin') return true;
    return false;
  };

  const columns = [
    {
      key: 'name', label: 'Utilisateur',
      render: u => (
        <div className="user-cell">
          <div
            className="user-cell__avatar"
            style={{ opacity: u.active === false ? 0.45 : 1 }}
          >
            {u.name?.charAt(0).toUpperCase()}
          </div>
          <div>
            <p className="user-cell__name">
              {u.name}
              {currentUser && u.id === currentUser.id && (
                <span style={{
                  marginLeft: 6, fontSize: '0.7rem',
                  background: 'var(--gold, #d4af37)', color: '#fff',
                  borderRadius: 4, padding: '1px 6px', verticalAlign: 'middle',
                }}>Vous</span>
              )}
            </p>
            <p className="user-cell__email">{u.email}</p>
          </div>
        </div>
      ),
    },
    { key: 'phone', label: 'Téléphone' },
    {
      key: 'role', label: 'Rôle',
      render: u => {
        const locked = isRoleChangeLocked(u);
        if (locked) {
          return (
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <RoleBadge role={u.role} />
              <span
                title={
                  currentUser && u.id === currentUser.id
                    ? 'Vous ne pouvez pas modifier votre propre rôle'
                    : "Le rôle d'un administrateur ne peut pas être modifié ici"
                }
                style={{ cursor: 'help', fontSize: '0.85rem', color: '#aaa' }}
              >🔒</span>
            </div>
          );
        }
        return (
          <select
            className="role-select"
            value={u.role}
            onChange={e => handleRoleChange(u, e.target.value)}
          >
            {ROLES.filter(r => r !== 'admin').map(r => (
              <option key={r} value={r}>{ROLE_LABELS[r]}</option>
            ))}
          </select>
        );
      },
    },
    // FIX #2 : colonne statut actif/inactif
    {
      key: 'active', label: 'Statut',
      render: u => <ActiveBadge active={u.active !== false} />,
    },
    {
      key: 'created_at', label: 'Créé le',
      render: u => u.created_at ? new Date(u.created_at).toLocaleDateString('fr-FR') : '—',
    },
  ];

  return (
    <div>
      <PageHeader
        title="Gestion des utilisateurs"
        subtitle={`${users.length} utilisateur${users.length > 1 ? 's' : ''} enregistrés`}
        action={
          <button className="btn btn--primary" onClick={() => setModal('create')}>
            + Ajouter utilisateur
          </button>
        }
      />

      {/* Filters */}
      <div className="adm-toolbar">
        <SearchBar
          value={search}
          onChange={v => { setSearch(v); reset(); }}
          placeholder="Rechercher par nom ou email…"
        />
        <div className="adm-toolbar__filters">
          {['', ...ROLES].map(r => (
            <button
              key={r}
              className={`btn btn--sm ${roleFilter === r ? 'btn--primary' : 'btn--outline'}`}
              onClick={() => { setRoleFilter(r); reset(); }}
            >
              {r === '' ? 'Tous' : ROLE_LABELS[r]}
            </button>
          ))}
        </div>
      </div>

      <DataTable
        columns={columns}
        data={currentData}
        loading={loading}
        empty="Aucun utilisateur trouvé."
        actions={row => (
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            <button
              className="btn btn--sm btn--outline"
              onClick={() => setModal({ type: 'edit', user: row })}
            >
              Modifier
            </button>

            {/* FIX #2 : bouton Activer / Désactiver */}
            {!isRoleChangeLocked(row) && (
              <button
                className={`btn btn--sm ${row.active !== false ? 'btn--warning' : 'btn--success'}`}
                title={row.active !== false ? 'Désactiver ce compte' : 'Activer ce compte'}
                onClick={() => setToggleConfirm({
                  user: row,
                  action: row.active !== false ? 'deactivate' : 'activate',
                })}
                style={{
                  background: row.active !== false ? 'var(--warning, #f9a825)' : 'var(--success, #4caf50)',
                  color: '#fff',
                  border: 'none',
                }}
              >
                {row.active !== false ? '⏸ Désactiver' : '▶ Activer'}
              </button>
            )}

            <button
              className="btn btn--sm btn--danger"
              disabled={isRoleChangeLocked(row)}
              title={
                isRoleChangeLocked(row)
                  ? (currentUser && row.id === currentUser.id
                      ? 'Vous ne pouvez pas supprimer votre propre compte'
                      : 'Un administrateur ne peut pas être supprimé ici')
                  : undefined
              }
              onClick={() => !isRoleChangeLocked(row) && setConfirm(row)}
            >
              Supprimer
            </button>
          </div>
        )}
      />

      <Pagination page={page} totalPages={totalPages} onPage={goTo} />

      {/* Modals */}
      {modal === 'create' && (
        <Modal title="Créer un utilisateur" onClose={() => setModal(null)}>
          <UserForm onSubmit={handleCreate} onClose={() => setModal(null)} loading={saving} />
        </Modal>
      )}

      {modal?.type === 'edit' && (
        <Modal title="Modifier l'utilisateur" onClose={() => setModal(null)}>
          <UserForm
            initial={modal.user}
            isEdit
            onSubmit={handleEdit}
            onClose={() => setModal(null)}
            loading={saving}
          />
        </Modal>
      )}

      {/* Confirmation suppression */}
      {confirm && (
        <ConfirmDialog
          title="Supprimer l'utilisateur"
          message={`Voulez-vous vraiment supprimer "${confirm.name}" ? Cette action est irréversible.`}
          danger
          onConfirm={handleDelete}
          onCancel={() => setConfirm(null)}
        />
      )}

      {/* FIX #2 : Confirmation activation / désactivation */}
      {toggleConfirm && (
        <ConfirmDialog
          title={toggleConfirm.action === 'activate' ? 'Activer le compte' : 'Désactiver le compte'}
          message={
            toggleConfirm.action === 'activate'
              ? `Activer le compte de "${toggleConfirm.user.name}" ? L'utilisateur pourra se reconnecter.`
              : `Désactiver le compte de "${toggleConfirm.user.name}" ? L'utilisateur ne pourra plus se connecter.`
          }
          danger={toggleConfirm.action === 'deactivate'}
          onConfirm={handleToggleActive}
          onCancel={() => setToggleConfirm(null)}
        />
      )}
    </div>
  );
};

export default AdminUsers;