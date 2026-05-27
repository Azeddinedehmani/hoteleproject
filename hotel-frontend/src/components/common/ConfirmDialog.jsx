import React from 'react';
import './ConfirmDialog.css';

/**
 * ConfirmDialog — boîte de confirmation accessible
 * Props: title, message, onConfirm, onCancel, danger
 */
const ConfirmDialog = ({ title = 'Confirmer', message, onConfirm, onCancel, danger = false }) => (
  <div className="confirm-backdrop" onClick={onCancel}>
    <div className="confirm-dialog" onClick={e => e.stopPropagation()} role="alertdialog">
      <div className={`confirm-dialog__icon ${danger ? 'confirm-dialog__icon--danger' : ''}`}>
        {danger ? '⚠' : '?'}
      </div>
      <h3 className="confirm-dialog__title">{title}</h3>
      {message && <p className="confirm-dialog__msg">{message}</p>}
      <div className="confirm-dialog__actions">
        <button className="btn btn--outline" onClick={onCancel}>Annuler</button>
        <button
          className={`btn ${danger ? 'btn--confirm-danger' : 'btn--primary'}`}
          onClick={onConfirm}
        >
          Confirmer
        </button>
      </div>
    </div>
  </div>
);

export default ConfirmDialog;