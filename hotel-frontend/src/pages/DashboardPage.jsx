import React from 'react';
import { useAuth } from '../context/AuthContext';
import './DashboardPage.css';

const DashboardPage = () => {
  const { user, isAdmin, isReceptionist } = useAuth();

  return (
    <div className="dashboard-page">
      <div className="dashboard-page__welcome">
        <h2>Tableau de bord</h2>
        <p>Vous êtes connecté en tant que <strong>{user?.role}</strong></p>
      </div>

      <div className="dashboard-page__cards">
        <div className="stat-card">
          <span className="stat-card__icon">◫</span>
          <div>
            <p className="stat-card__value">—</p>
            <p className="stat-card__label">Réservations</p>
          </div>
        </div>
        <div className="stat-card">
          <span className="stat-card__icon">⊞</span>
          <div>
            <p className="stat-card__value">—</p>
            <p className="stat-card__label">Chambres</p>
          </div>
        </div>
        {(isAdmin || isReceptionist) && (
          <div className="stat-card">
            <span className="stat-card__icon">⊙</span>
            <div>
              <p className="stat-card__value">—</p>
              <p className="stat-card__label">Clients</p>
            </div>
          </div>
        )}
      </div>

      <div className="dashboard-page__placeholder">
        <p>🚀 Les prochains modules (chambres, réservations, clients) seront ajoutés ici.</p>
      </div>
    </div>
  );
};

export default DashboardPage;