import React from 'react';
import './ToastContainer.css';

const ICONS = {
  success: '✓',
  error:   '✕',
  info:    'ℹ',
  warning: '⚠',
};

const ToastContainer = ({ toasts = [], onDismiss }) => {
  if (!toasts.length) return null;

  return (
    <div className="toast-container" role="region" aria-label="Notifications">
      {toasts.map(t => (
        <div
          key={t.id}
          className={`toast toast--${t.type}`}
          onClick={() => onDismiss?.(t.id)}
        >
          <span className="toast__icon">{ICONS[t.type]}</span>
          <span className="toast__msg">{t.message}</span>
          <button className="toast__close" aria-label="Fermer">✕</button>
        </div>
      ))}
    </div>
  );
};

export default ToastContainer;