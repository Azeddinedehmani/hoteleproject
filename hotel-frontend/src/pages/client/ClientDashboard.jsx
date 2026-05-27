import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import reservationService from '../../services/reservationService';
import StatusBadge from '../../components/common/StatusBadge';
import './ClientDashboard.css';

const ClientDashboard = () => {
  const { user } = useAuth();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    reservationService.getMyReservations()
      .then(setReservations)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const upcoming = reservations.filter(r =>
    ['pending', 'confirmed'].includes(r.status)
  );
  const active = reservations.filter(r => r.status === 'checked_in');
  const past = reservations.filter(r =>
    ['checked_out', 'cancelled'].includes(r.status)
  );

  return (
    <div className="client-dash">
      <div className="client-dash__welcome">
        <h2>Bonjour, {user?.name?.split(' ')[0]} 👋</h2>
        <p>Voici un résumé de vos séjours au Grand Hôtel.</p>
      </div>

      {/* Stats */}
      <div className="client-dash__stats">
        <div className="cstat cstat--gold">
          <p className="cstat__value">{loading ? '…' : upcoming.length}</p>
          <p className="cstat__label">Réservations à venir</p>
        </div>
        <div className="cstat cstat--blue">
          <p className="cstat__value">{loading ? '…' : active.length}</p>
          <p className="cstat__label">En cours</p>
        </div>
        <div className="cstat cstat--grey">
          <p className="cstat__value">{loading ? '…' : past.length}</p>
          <p className="cstat__label">Séjours passés</p>
        </div>
      </div>

      {/* Quick actions */}
      <div className="client-dash__actions">
        <Link to="/client/rooms" className="caction">
          <span className="caction__icon">⊞</span>
          <span className="caction__label">Voir les chambres</span>
          <span className="caction__arrow">→</span>
        </Link>
        <Link to="/client/reservations" className="caction">
          <span className="caction__icon">◫</span>
          <span className="caction__label">Mes réservations</span>
          <span className="caction__arrow">→</span>
        </Link>
        <Link to="/client/invoices" className="caction">
          <span className="caction__icon">◉</span>
          <span className="caction__label">Mes factures</span>
          <span className="caction__arrow">→</span>
        </Link>
      </div>

      {/* Recent reservations */}
      {!loading && reservations.length > 0 && (
        <div className="client-dash__recent">
          <h3 className="client-dash__section-title">Réservations récentes</h3>
          <div className="client-dash__list">
            {reservations.slice(0, 3).map(r => (
              <div key={r.id} className="res-item">
                <div className="res-item__main">
                  <p className="res-item__room">Chambre {r.room?.number ?? r.room_id}</p>
                  <p className="res-item__dates">
                    {new Date(r.check_in).toLocaleDateString('fr-FR')}
                    {' → '}
                    {new Date(r.check_out).toLocaleDateString('fr-FR')}
                  </p>
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

export default ClientDashboard;