import React from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';
import './AuthLayout.css';

const AuthLayout = () => {
  const { pathname } = useLocation();
  const isLogin = pathname === '/login';

  return (
    <div className="auth-layout">
      {/* Panneau gauche : branding */}
      <div className="auth-brand">
        <div className="auth-brand__inner">
          <div className="auth-brand__logo">
            <span className="auth-brand__logo-icon">✦</span>
            <span className="auth-brand__logo-text">Grand Hôtel</span>
          </div>
          <blockquote className="auth-brand__quote">
            "Le luxe, c'est quand on s'occupe des détails."
          </blockquote>
          <div className="auth-brand__features">
            <div className="auth-brand__feature">
              <span>⬡</span> Réservations en temps réel
            </div>
            <div className="auth-brand__feature">
              <span>⬡</span> Gestion multi-rôles
            </div>
            <div className="auth-brand__feature">
              <span>⬡</span> Suivi complet des séjours
            </div>
          </div>
        </div>
        <div className="auth-brand__pattern" aria-hidden="true" />
      </div>

      {/* Panneau droit : formulaire */}
      <div className="auth-form-panel">
        <div className="auth-form-panel__inner">
          {/* Lien de bascule */}
          <p className="auth-switch">
            {isLogin
              ? <>Pas encore de compte ? <Link to="/register">S'inscrire</Link></>
              : <>Déjà un compte ? <Link to="/login">Se connecter</Link></>
            }
          </p>

          {/* La page (Login ou Register) s'injecte ici */}
          <Outlet />
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;