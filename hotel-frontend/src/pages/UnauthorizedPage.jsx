import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const UnauthorizedPage = () => {
  const navigate = useNavigate();
  const { logout } = useAuth();

  return (
    <div style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      justifyContent: 'center', minHeight: '60vh', gap: '1rem', textAlign: 'center'
    }}>
      <span style={{ fontSize: 48, color: 'var(--gold)' }}>⚠</span>
      <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 26, color: 'var(--charcoal)' }}>
        Accès refusé
      </h2>
      <p style={{ color: 'var(--text-muted)', maxWidth: 360 }}>
        Vous n'avez pas les droits nécessaires pour accéder à cette page.
      </p>
      <div style={{ display: 'flex', gap: 12 }}>
        <button
          onClick={() => navigate(-1)}
          style={{
            padding: '10px 20px', background: 'var(--charcoal)', color: 'white',
            border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 14
          }}
        >
          ← Retour
        </button>
        <button
          onClick={() => { logout(); navigate('/login'); }}
          style={{
            padding: '10px 20px', background: 'transparent', color: 'var(--text-muted)',
            border: '1px solid var(--border)', borderRadius: 6, cursor: 'pointer', fontSize: 14
          }}
        >
          Se déconnecter
        </button>
      </div>
    </div>
  );
};

export default UnauthorizedPage;