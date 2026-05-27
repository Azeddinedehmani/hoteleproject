import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * PrivateRoute — protège les routes nécessitant une authentification.
 *
 * Props :
 *   allowedRoles  — tableau de rôles autorisés, ex: ['admin', 'receptionist']
 *                   Si omis, toute personne connectée peut accéder.
 *
 * Comportement :
 *   - Non connecté           → redirige vers /login (avec retour URL mémorisé)
 *   - Connecté, rôle refusé  → redirige vers /unauthorized
 *   - Connecté, rôle OK      → affiche la page enfant via <Outlet />
 */
const PrivateRoute = ({ allowedRoles }) => {
  const { isAuthenticated, user, loading } = useAuth();
  const location = useLocation();

  // Attendre la vérification du token avant de décider
  if (loading) {
    return (
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        height: '100vh', fontFamily: 'var(--font-body)', color: 'var(--text-muted)'
      }}>
        Chargement…
      </div>
    );
  }

  // Pas connecté → login
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Rôle non autorisé → page d'erreur
  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
};

export default PrivateRoute;