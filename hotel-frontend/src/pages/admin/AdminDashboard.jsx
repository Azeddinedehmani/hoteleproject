import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import reservationService from '../../services/reservationService';
import userService from '../../services/userService';
import roomService from '../../services/roomService';
import invoiceService from '../../services/invoiceService';
import './AdminDashboard.css';

const StatCard = ({ label, value, icon, trend, color }) => (
  <div className="adm-stat" style={{ '--color': color }}>
    <div className="adm-stat__top">
      <span className="adm-stat__icon">{icon}</span>
      {trend !== undefined && (
        <span className={`adm-stat__trend ${trend >= 0 ? 'up' : 'down'}`}>
          {trend >= 0 ? '↑' : '↓'} {Math.abs(trend)}%
        </span>
      )}
    </div>
    <p className="adm-stat__value">{value}</p>
    <p className="adm-stat__label">{label}</p>
  </div>
);

const AdminDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState({
    reservations: [], users: [], rooms: [], invoices: [],
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.allSettled([
      reservationService.getAll(),
      userService.getAll(),
      roomService.getAll(),
      invoiceService.getAll(),
    ]).then(([r, u, ro, inv]) => {
      setData({
        reservations: r.status === 'fulfilled' ? r.value : [],
        users:        u.status === 'fulfilled' ? u.value : [],
        rooms:        ro.status === 'fulfilled' ? ro.value : [],
        invoices:     inv.status === 'fulfilled' ? inv.value : [],
      });
      setLoading(false);
    });
  }, []);

  const today = new Date().toISOString().split('T')[0];

  const revenue = data.invoices.reduce((acc, i) => acc + (Number(i.total) || 0), 0);
  const occupied = data.rooms.filter(r => r.status === 'occupied').length;
  const occupancyRate = data.rooms.length
    ? Math.round((occupied / data.rooms.length) * 100)
    : 0;
  const todayArrivals = data.reservations.filter(r => r.check_in?.startsWith(today)).length;

  const recentReservations = [...data.reservations]
    .sort((a, b) => new Date(b.created_at) - new Date(a.created_at))
    .slice(0, 5);

  const roleCount = data.users.reduce((acc, u) => {
    acc[u.role] = (acc[u.role] || 0) + 1;
    return acc;
  }, {});

  const shortcuts = [
    { label: 'Utilisateurs',  icon: '⊛', path: '/admin/users',      color: '#3B82F6' },
    { label: 'Chambres',      icon: '⊞', path: '/admin/rooms',      color: '#10B981' },
    { label: 'Tarifs',        icon: '◈', path: '/admin/tariffs',    color: '#F59E0B' },
    { label: 'Équipements',   icon: '⊟', path: '/admin/equipment',  color: '#8B5CF6' },
  ];

  return (
    <div className="adm-dash">
      {/* Header */}
      <div className="adm-dash__header">
        <div>
          <h2 className="adm-dash__title">Dashboard Administrateur</h2>
          <p className="adm-dash__date">
            {new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
          </p>
        </div>
        <div className="adm-dash__welcome-tag">
          <span className="adm-dash__avatar">{user?.name?.charAt(0)}</span>
          {user?.name?.split(' ')[0]}
        </div>
      </div>

      {/* KPI Stats */}
      <div className="adm-stats-grid">
        <StatCard
          label="Revenus totaux"
          value={`${revenue.toLocaleString('fr-FR')} DH`}
          icon="◉"
          color="#C9A84C"
          trend={12}
        />
        <StatCard
          label="Réservations"
          value={loading ? '…' : data.reservations.length}
          icon="◫"
          color="#3B82F6"
          trend={5}
        />
        <StatCard
          label="Taux d'occupation"
          value={`${occupancyRate}%`}
          icon="⊞"
          color="#10B981"
          trend={occupancyRate - 70}
        />
        <StatCard
          label="Arrivées aujourd'hui"
          value={loading ? '…' : todayArrivals}
          icon="⊛"
          color="#8B5CF6"
        />
        <StatCard
          label="Utilisateurs"
          value={loading ? '…' : data.users.length}
          icon="⊙"
          color="#EC4899"
          trend={3}
        />
        <StatCard
          label="Chambres totales"
          value={loading ? '…' : data.rooms.length}
          icon="⊟"
          color="#F59E0B"
        />
      </div>

      {/* Quick access */}
      <div className="adm-dash__section-title">Accès rapide</div>
      <div className="adm-shortcuts">
        {shortcuts.map(s => (
          <button
            key={s.path}
            className="adm-shortcut"
            style={{ '--sc-color': s.color }}
            onClick={() => navigate(s.path)}
          >
            <span className="adm-shortcut__icon">{s.icon}</span>
            <span className="adm-shortcut__label">{s.label}</span>
            <span className="adm-shortcut__arrow">→</span>
          </button>
        ))}
      </div>

      <div className="adm-dash__cols">
        {/* Recent reservations */}
        <div className="adm-panel">
          <div className="adm-panel__header">
            <h3>Réservations récentes</h3>
            <button className="btn btn--sm btn--outline" onClick={() => navigate('/reception/reservations')}>
              Voir tout
            </button>
          </div>
          {loading ? (
            <div className="adm-panel__loading">Chargement…</div>
          ) : recentReservations.length === 0 ? (
            <div className="adm-panel__empty">Aucune réservation</div>
          ) : (
            <div className="adm-res-list">
              {recentReservations.map(r => (
                <div key={r.id} className="adm-res-row">
                  <div className="adm-res-row__dot" data-status={r.status} />
                  <div>
                    <p className="adm-res-row__client">{r.client?.name ?? `Client #${r.client_id}`}</p>
                    <p className="adm-res-row__meta">
                      Ch. {r.room?.number ?? r.room_id} ·{' '}
                      {new Date(r.check_in).toLocaleDateString('fr-FR')}
                    </p>
                  </div>
                  <span className={`adm-res-row__status status--${r.status}`}>{r.status}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* User breakdown */}
        <div className="adm-panel">
          <div className="adm-panel__header">
            <h3>Répartition utilisateurs</h3>
            <button className="btn btn--sm btn--outline" onClick={() => navigate('/admin/users')}>
              Gérer
            </button>
          </div>
          <div className="adm-role-bars">
            {[
              { role: 'admin',        label: 'Admins',         color: '#C9A84C' },
              { role: 'receptionist', label: 'Réceptionnistes',color: '#3B82F6' },
              { role: 'client',       label: 'Clients',        color: '#10B981' },
            ].map(({ role, label, color }) => {
              const count = roleCount[role] || 0;
              const pct   = data.users.length ? Math.round((count / data.users.length) * 100) : 0;
              return (
                <div key={role} className="adm-role-bar">
                  <div className="adm-role-bar__info">
                    <span>{label}</span>
                    <span>{count}</span>
                  </div>
                  <div className="adm-role-bar__track">
                    <div
                      className="adm-role-bar__fill"
                      style={{ width: `${pct}%`, background: color }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;