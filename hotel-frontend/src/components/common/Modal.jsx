import React, { useEffect } from 'react';
import './Modal.css';

/**
 * Modal — dialogue générique
 *
 * Props: title, onClose, children, size ('sm' | 'md' | 'lg')
 */
const Modal = ({ title, onClose, children, size = 'md' }) => {
  // Fermer avec Escape
  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className={`modal modal--${size}`}
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        <div className="modal__header">
          <h3 className="modal__title">{title}</h3>
          <button className="modal__close" onClick={onClose} aria-label="Fermer">✕</button>
        </div>
        <div className="modal__body">{children}</div>
      </div>
    </div>
  );
};

export default Modal;