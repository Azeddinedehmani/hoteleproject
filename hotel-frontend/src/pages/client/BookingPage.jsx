import React, { useState, useEffect, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import reservationService from '../../services/reservationService';
import tariffService from '../../services/tariffService';
import { CURRENCY } from '../../constants'; // CORRIGÉ — Bug #1 : import de la constante devise centrale
import PageHeader from '../../components/common/PageHeader';
import '../../components/common/shared.css';
import './BookingPage.css';

// ─────────────────────────────────────────────────────────────
// Libellés des types de chambre
// ─────────────────────────────────────────────────────────────
const TYPE_LABELS = {
  SIMPLE:    'Simple',
  DOUBLE:    'Double',
  SUITE:     'Suite',
  DELUXE:    'Deluxe',
  FAMILIALE: 'Familiale',
  // compatibilité lowercase
  simple: 'Simple',
  single: 'Simple',
  double: 'Double',
  suite:  'Suite',
  deluxe: 'Deluxe',
};

// ─────────────────────────────────────────────────────────────
// Utilitaires
// ─────────────────────────────────────────────────────────────
const diffDays = (a, b) => {
  if (!a || !b) return 0;
  return Math.max(0, Math.round((new Date(b) - new Date(a)) / 86400000));
};

// ─────────────────────────────────────────────────────────────
// Composant
// ─────────────────────────────────────────────────────────────
const BookingPage = () => {
  const { state }  = useLocation();
  const navigate   = useNavigate();
  const { user }   = useAuth();

  // clientId du client connecté — requis par le backend (@NotNull sur clientId)
  // Le backend le remplace par celui du JWT côté serveur, mais Bean Validation
  // rejette la requête si le champ est absent. On envoie donc l'id du user.
  const clientId = user?.clientId ?? user?.id ?? null;

  /**
   * roomType : string UPPERCASE transmis depuis RoomsPage ("SIMPLE", "DOUBLE", etc.)
   * Rétrocompatibilité : si l'état contient encore un objet `room`, on extrait son type.
   */
  const roomType = state?.roomType
    ?? (state?.room?.type ? String(state.room.type).toUpperCase() : null);

  const [form, setForm] = useState({
    check_in:  state?.checkIn  || '',
    check_out: state?.checkOut || '',
    guests:    1,
    notes:     '',
  });

  const [loading,         setLoading]         = useState(false);
  const [error,           setError]           = useState('');
  const [success,         setSuccess]         = useState(false);
  const [applicableTariff, setApplicableTariff] = useState(null);
  const [tariffLoading,   setTariffLoading]   = useState(false);
  // Prix indicatif récupéré via le tarif applicable (ou null si inconnu)
  const [basePrice,       setBasePrice]       = useState(null);

  // ── Chargement du tarif saisonnier applicable ──────────────
  const fetchApplicableTariff = useCallback(async (checkIn, checkOut) => {
    if (!roomType || !checkIn || !checkOut) {
      setApplicableTariff(null);
      return;
    }
    setTariffLoading(true);
    try {
      const tariff = await tariffService.getApplicable(roomType, checkIn, checkOut);
      setApplicableTariff(tariff);
      // Le tarif contient le prix de base sur lequel la remise s'applique
      if (tariff?.base_price != null) setBasePrice(Number(tariff.base_price));
    } catch {
      setApplicableTariff(null);
    } finally {
      setTariffLoading(false);
    }
  }, [roomType]);

  // Chargement initial si les dates sont déjà pré-remplies
  useEffect(() => {
    if (form.check_in && form.check_out) {
      fetchApplicableTariff(form.check_in, form.check_out);
    }
  }, []); // eslint-disable-line

  // ── Garde : aucun type de chambre sélectionné ──────────────
  if (!roomType) {
    return (
      <div className="booking-no-room">
        <p>Aucun type de chambre sélectionné.</p>
        <button className="btn btn--primary" onClick={() => navigate('/client/rooms')}>
          ← Voir les chambres
        </button>
      </div>
    );
  }

  // ── Calculs du récapitulatif ────────────────────────────────
  const nights       = diffDays(form.check_in, form.check_out);
  const nightlyPrice = applicableTariff?.effective_price != null
    ? Number(applicableTariff.effective_price)
    : (basePrice ?? 0);
  const total        = nights > 0 && nightlyPrice > 0 ? nights * nightlyPrice : 0;

  // ── Gestion des champs ──────────────────────────────────────
  const handleChange = ({ target: { name, value } }) => {
    const updated = { ...form, [name]: value };
    setForm(updated);
    if (name === 'check_in' || name === 'check_out') {
      const ci = name === 'check_in'  ? value : form.check_in;
      const co = name === 'check_out' ? value : form.check_out;
      if (ci && co) fetchApplicableTariff(ci, co);
      else          setApplicableTariff(null);
    }
  };

  // ── Soumission ──────────────────────────────────────────────
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!form.check_in || !form.check_out) {
      setError('Veuillez sélectionner vos dates.');
      return;
    }
    if (nights < 1) {
      setError("La date de départ doit être après la date d'arrivée.");
      return;
    }

    // FIX J : clientId obligatoire — bloquer l'envoi si absent pour éviter un 400/403 backend
    if (!clientId) {
      setError('Session invalide, veuillez vous reconnecter.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await reservationService.create({
        client_id:     clientId,          // requis par @NotNull backend (remplacé par JWT côté serveur)
        // Pas de room_id : la réception attribue la chambre précise lors du check-in
        room_type:     roomType,
        check_in:      form.check_in,
        check_out:     form.check_out,
        guests:        form.guests,
        notes:         form.notes,
        // CORRIGÉ — Bug #2 : le backend attend le prix UNITAIRE par nuit (appliedPrice),
        // pas le total. On envoie nightlyPrice au lieu de total.
        applied_price: nightlyPrice > 0 ? nightlyPrice : undefined,
      });
      setSuccess(true);
      setTimeout(() => navigate('/client/reservations'), 3000);
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.error   ||
        err?.message                 ||
        'Erreur lors de la réservation';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // ── Écran de succès ─────────────────────────────────────────
  if (success) {
    return (
      <div className="booking-success">
        <span className="booking-success__icon">✓</span>
        <h3>Demande envoyée !</h3>
        <p>
          Votre demande a bien été enregistrée. La réception vous confirmera
          la chambre attribuée dans les meilleurs délais.
        </p>
        <p className="booking-success__redirect">Redirection vers vos réservations…</p>
      </div>
    );
  }

  const typeLabel = TYPE_LABELS[roomType] ?? roomType;

  return (
    <div>
      <PageHeader
        title="Réserver une chambre"
        subtitle={`Type : ${typeLabel}`}
        action={
          <button className="btn btn--outline" onClick={() => navigate(-1)}>
            ← Retour
          </button>
        }
      />

      <div className="booking-layout">

        {/* ── Formulaire ── */}
        <form className="booking-form" onSubmit={handleSubmit}>
          {error && <div className="booking-error">{error}</div>}

          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Date d'arrivée *</label>
              <input
                type="date"
                name="check_in"
                className="form-input"
                value={form.check_in}
                min={new Date().toISOString().split('T')[0]}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Date de départ *</label>
              <input
                type="date"
                name="check_out"
                className="form-input"
                value={form.check_out}
                min={
                  form.check_in
                    ? new Date(new Date(form.check_in).getTime() + 86400000)
                        .toISOString().split('T')[0]
                    : new Date().toISOString().split('T')[0]
                }
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Nombre de personnes</label>
            <input
              type="number"
              name="guests"
              className="form-input"
              value={form.guests}
              min={1}
              max={10}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Remarques</label>
            <textarea
              name="notes"
              className="form-textarea"
              value={form.notes}
              onChange={handleChange}
              placeholder="Demandes particulières, heure d'arrivée estimée…"
            />
          </div>

          <div className="form-actions">
            <button
              type="submit"
              className="btn btn--gold"
              disabled={loading || nights < 1}
            >
              {loading
                ? <span className="dt-spinner" />
                : `Soumettre ma demande${total ? ` — ${total.toFixed(2)} ${CURRENCY}` : ''}` // CORRIGÉ — Bug #1 : € remplacé par {CURRENCY}
              }
            </button>
          </div>
        </form>

        {/* ── Récapitulatif ── */}
        <aside className="booking-summary">
          <h3 className="booking-summary__title">Récapitulatif</h3>

          {/* Type de chambre demandé — pas de numéro de chambre */}
          <div className="booking-summary__row">
            <span>Type demandé</span>
            <strong>{typeLabel}</strong>
          </div>

          {/* Dates */}
          {form.check_in && (
            <div className="booking-summary__row">
              <span>Arrivée</span>
              <strong>{form.check_in}</strong>
            </div>
          )}
          {form.check_out && (
            <div className="booking-summary__row">
              <span>Départ</span>
              <strong>{form.check_out}</strong>
            </div>
          )}

          <div className="booking-summary__row">
            <span>Nuits</span>
            <strong>{nights || '—'}</strong>
          </div>

          {/* Tarif saisonnier en cours de chargement */}
          {tariffLoading && (
            <div className="booking-summary__row">
              <span>Tarif en cours…</span>
              <span className="dt-spinner" style={{ width: 14, height: 14 }} />
            </div>
          )}

          {/* Tarif saisonnier appliqué */}
          {!tariffLoading && applicableTariff && applicableTariff.discount_percent > 0 && (
            <div className="booking-summary__row">
              <span>Tarif «&nbsp;{applicableTariff.name}&nbsp;»</span>
              <strong style={{ color: 'var(--color-gold)' }}>
                -{applicableTariff.discount_percent}%
              </strong>
            </div>
          )}

          <div className="booking-summary__divider" />

          {/*
            POINT 13 — Afficher uniquement le total estimé,
            sans ligne "Prix / nuit" séparée.
          */}
          <div className="booking-summary__total">
            <span>Total estimé</span>
            {/* CORRIGÉ — Bug #1 : € remplacé par {CURRENCY} */}
            <strong>{total ? `${total.toFixed(2)} ${CURRENCY}` : '—'}</strong>
          </div>

          {/* Mention tarifaire indicative obligatoire */}
          <p className="booking-summary__mention">
            * Tarif indicatif, sous réserve de confirmation par la réception.
          </p>

          {/*
            Rappel : la chambre précise sera attribuée à l'arrivée.
            On n'affiche aucun numéro de chambre.
          */}
          <p className="booking-summary__mention">
            La chambre vous sera attribuée lors de votre check-in.
          </p>
        </aside>

      </div>
    </div>
  );
};

export default BookingPage;