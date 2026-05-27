import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './DashboardLayout.css';

// Nav config per role
const NAV_CONFIG = {
  admin: [
    { section: 'Administration' },
    { to: '/admin/dashboard',        label: 'Dashboard',        icon: '⊞' },
    { to: '/admin/users',            label: 'Utilisateurs',     icon: '⊛' },
    { to: '/admin/rooms',            label: 'Chambres',         icon: '⊟' },
    { to: '/admin/tariffs',          label: 'Tarifs',           icon: '◈' },
    { to: '/admin/equipment',        label: 'Équipements',      icon: '⊙' },
    { section: 'Réception' },
    { to: '/reception/dashboard',    label: 'Vue réception',    icon: '◫' },
    { to: '/reception/reservations', label: 'Réservations',     icon: '◉' },
    { to: '/reception/clients',      label: 'Clients',          icon: '⊕' },
    { to: '/reception/checkin',      label: 'Check-in/out',     icon: '✓' },
    { to: '/reception/invoices',     label: 'Facturation',      icon: '⊖' },
  ],
  receptionist: [
    { section: 'Réception' },
    { to: '/reception/dashboard',    label: 'Dashboard',        icon: '⊞' },
    { to: '/reception/reservations', label: 'Réservations',     icon: '◫' },
    { to: '/reception/clients',      label: 'Clients',          icon: '⊛' },
    { to: '/reception/checkin',      label: 'Check-in/out',     icon: '✓' },
    { to: '/reception/invoices',     label: 'Facturation',      icon: '◉' },
  ],
  client: [
    { section: 'Mon espace' },
    { to: '/client/dashboard',    label: 'Accueil',          icon: '⊞' },
    { to: '/client/rooms',        label: 'Nos chambres',     icon: '⊟' },
    { to: '/client/reservations', label: 'Mes réservations', icon: '◫' },
    { to: '/client/invoices',     label: 'Mes factures',     icon: '◉' },
  ],
};

const ROLE_LABELS = {
  admin:        'Administrateur',
  receptionist: 'Réceptionniste',
  client:       'Client',
};

const DashboardLayout = ({ toast }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const navItems = NAV_CONFIG[user?.role] ?? NAV_CONFIG.client;
  const roleLabel = ROLE_LABELS[user?.role] ?? '';

  // Derive page title from current path
  const allLinks = navItems.filter(n => n.to);
  const activeLink = allLinks.find(n => location.pathname.startsWith(n.to));
  const pageTitle = activeLink?.label ?? 'Grand Hôtel';

  const handleLogout = () => {
    logout();
    toast?.info('Déconnexion réussie');
    navigate('/login');
  };

  return (
    <div className="dash-layout">
      {/* ── Sidebar ── */}
      <aside className={`dash-sidebar ${menuOpen ? 'is-open' : ''}`}>
        {/* Logo */}
        <div className="dash-sidebar__logo">
          <span className="dash-sidebar__logo-icon">✦</span>
          <span className="dash-sidebar__logo-text">Grand Hôtel</span>
        </div>

        {/* Nav */}
        <nav className="dash-nav">
          {navItems.map((item, i) =>
            item.section ? (
              <div key={`sec-${i}`} className="dash-nav__section">{item.section}</div>
            ) : (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `dash-nav__link ${isActive ? 'is-active' : ''}`
                }
                onClick={() => setMenuOpen(false)}
              >
                <span className="dash-nav__icon">{item.icon}</span>
                {item.label}
              </NavLink>
            )
          )}
        </nav>

        {/* User card */}
        <div className="dash-sidebar__user">
          <div className="dash-sidebar__avatar">
            {user?.name?.charAt(0).toUpperCase()}
          </div>
          <div className="dash-sidebar__info">
            <p className="dash-sidebar__name">{user?.name}</p>
            <p className="dash-sidebar__role">{roleLabel}</p>
          </div>
          <button
            className="dash-sidebar__logout"
            onClick={handleLogout}
            title="Déconnexion"
            aria-label="Se déconnecter"
          >
            ⏻
          </button>
        </div>
      </aside>

      {/* Mobile overlay */}
      {menuOpen && (
        <div
          className="dash-overlay"
          onClick={() => setMenuOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* ── Main ── */}
      <main className="dash-main">
        {/* Topbar */}
        <header className="dash-topbar">
          <button
            className="dash-topbar__burger"
            onClick={() => setMenuOpen(!menuOpen)}
            aria-label="Ouvrir le menu"
          >
            ☰
          </button>

          <div className="dash-topbar__left">
            <h1 className="dash-topbar__title">{pageTitle}</h1>
          </div>

          <div className="dash-topbar__right">
            <div className="dash-topbar__user">
              <div className="dash-topbar__avatar">
                {user?.name?.charAt(0).toUpperCase()}
              </div>
              <div className="dash-topbar__info">
                <span className="dash-topbar__name">{user?.name?.split(' ')[0]}</span>
                <span className="dash-topbar__role">{roleLabel}</span>
              </div>
            </div>
          </div>
        </header>

        {/* Content */}
        <div className="dash-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default DashboardLayout;