import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import reservationService from '../../services/reservationService';
import clientService from '../../services/clientService';
import StatusBadge from '../../components/common/StatusBadge';
import '../../components/common/shared.css';
import './ReceptionDashboard.css';

const ReceptionDashboard = () => {
  const navigate = useNavigate();
  const [stats, setStats]           = useState({ total: 0, checkin: 0, checkout: 0, clients: 0 });
  const [today, setToday]           = useState([]);
  const [loadingToday, setLT]       = useState(true);

  useEffect(() => {
    const todayStr = new Date().toISOString().split('T')[0];

    Promise.allSettled([
      reservationService.getAll(),
      clientService.getAll(),
    ]).then(([resResult, cliResult]) => {
      const reservations = resResult.status === 'fulfilled' ? resResult.value : [];
      const clients      = cliResult.status === 'fulfilled' ? cliResult.value : [];

      const todayRes = reservations.filter(r =>
        r.check_in?.startsWith(todayStr) || r.check_out?.startsWith(todayStr)
      );

      setStats({
        total:   reservations.length,
        checkin: reservations.filter(r => r.check_in?.startsWith(todayStr)).length,
        checkout: reservations.filter(r => r.check_out?.startsWith(todayStr)).length,
        clients: Array.isArray(clients) ? clients.length : 0,
      });
      setToday(todayRes);
      setLT(false);
    });
  }, []);

  const shortcuts = [
    { label: 'Gestion clients',       icon: '👤', path: '/reception/clients' },
    { label: 'Réservations',          icon: '◫',  path: '/reception/reservations' },
    { label: 'Check-in / Check-out',  icon: '✓',  path: '/reception/checkin' },
    { label: 'Générer une facture',   icon: '◉',  path: '/reception/invoices' },
  ];

  return (
    <div>
      <div className="rec-welcome">
        <h2>Tableau de bord Réception</h2>
        <p>{new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })}</p>
      </div>

      <div className="rec-stats">
        {[
          { label: 'Réservations totales', value: stats.total, color: '#C9A84C' },
          { label: 'Arrivées aujourd\'hui', value: stats.checkin, color: '#3B82F6' },
          { label: 'Départs aujourd\'hui', value: stats.checkout, color: '#8B5CF6' },
          { label: 'Clients enregistrés', value: stats.clients, color: '#3A9E6B' },
        ].map((s, i) => (
          <div key={i} className="rec-stat" style={{ '--accent': s.color }}>
            <p className="rec-stat__value">{s.value}</p>
            <p className="rec-stat__label">{s.label}</p>
          </div>
        ))}
      </div>

      <div className="rec-shortcuts">
        {shortcuts.map(s => (
          <button key={s.path} className="rec-shortcut" onClick={() => navigate(s.path)}>
            <span className="rec-shortcut__icon">{s.icon}</span>
            <span>{s.label}</span>
            <span className="rec-shortcut__arrow">→</span>
          </button>
        ))}
      </div>

      {!loadingToday && today.length > 0 && (
        <div className="rec-today">
          <h3 className="rec-today__title">Activité du jour</h3>
          <div className="rec-today__list">
            {today.map(r => (
              <div key={r.id} className="rec-today__row">
                <div>
                  <p className="rec-today__client">{r.client?.name ?? `Client #${r.client_id}`}</p>
                  <p className="rec-today__room">Chambre {r.room?.number ?? r.room_id}</p>
                </div>
                <StatusBadge status={r.status} />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default ReceptionDashboard;