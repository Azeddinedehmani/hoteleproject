import React from 'react';
import './StatusBadge.css';

/**
 * COMPOSANT COMMUN — StatusBadge
 *
 * Affiche un badge coloré à partir d'un statut (string).
 * Gère les valeurs UPPERCASE (backend Java) et lowercase (frontend).
 *
 * POINT 12 — Traductions françaises complètes pour l'espace client :
 *   pending      → "En attente"
 *   confirmed    → "Confirmée"
 *   checked_in   → "En cours"
 *   checked_out  → "Terminée"
 *   cancelled    → "Annulée"
 */
const STATUS_MAP = {
  // ── Réservations (lowercase frontend) ──────────────────────
  pending:     { label: 'En attente',  cls: 'badge--pending'   },
  confirmed:   { label: 'Confirmée',   cls: 'badge--confirmed' },
  checked_in:  { label: 'En cours',    cls: 'badge--checkin'   },
  checked_out: { label: 'Terminée',    cls: 'badge--checkout'  },
  cancelled:   { label: 'Annulée',     cls: 'badge--cancelled' },

  // ── Réservations (UPPERCASE backend) ──────────────────────
  PENDING:      { label: 'En attente',  cls: 'badge--pending'   },
  CONFIRMED:    { label: 'Confirmée',   cls: 'badge--confirmed' },
  CHECKED_IN:   { label: 'En cours',    cls: 'badge--checkin'   },
  CHECKED_OUT:  { label: 'Terminée',    cls: 'badge--checkout'  },
  CANCELLED:    { label: 'Annulée',     cls: 'badge--cancelled' },

  // ── Chambres (lowercase frontend) ──────────────────────────
  available:    { label: 'Disponible',  cls: 'badge--confirmed' },
  occupied:     { label: 'Occupée',     cls: 'badge--checkin'   },
  maintenance:  { label: 'Maintenance', cls: 'badge--pending'   },

  // ── Chambres (UPPERCASE backend) ───────────────────────────
  AVAILABLE:    { label: 'Disponible',  cls: 'badge--confirmed' },
  OCCUPIED:     { label: 'Occupée',     cls: 'badge--checkin'   },
  MAINTENANCE:  { label: 'Maintenance', cls: 'badge--pending'   },

  // ── Factures (lowercase frontend) ──────────────────────────
  paid:         { label: 'Payée',       cls: 'badge--confirmed' },
  unpaid:       { label: 'Non payée',   cls: 'badge--pending'   },

  // ── Factures (UPPERCASE backend) ───────────────────────────
  PAID:         { label: 'Payée',       cls: 'badge--confirmed' },
  UNPAID:       { label: 'Non payée',   cls: 'badge--pending'   },
};

const StatusBadge = ({ status }) => {
  const config = STATUS_MAP[status] || { label: status ?? '—', cls: 'badge--default' };
  return (
    <span className={`badge ${config.cls}`}>{config.label}</span>
  );
};

export default StatusBadge;