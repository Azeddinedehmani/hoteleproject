import React from 'react';
import { useNavigate } from 'react-router-dom';
import './HotelLandingPage.css';

const HotelLandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="landing">

      {/* ── HERO ── */}
      <header className="landing__hero">
        <div className="landing__hero-overlay" />
        <nav className="landing__nav">
          <div className="landing__logo">
            <span className="landing__logo-icon">✦</span>
            <span className="landing__logo-text">Grand Hôtel Royal</span>
          </div>
          <div className="landing__nav-actions">
            <button className="landing__btn landing__btn--outline" onClick={() => navigate('/login')}>
              Se connecter
            </button>
            <button className="landing__btn landing__btn--gold" onClick={() => navigate('/register')}>
              Réserver
            </button>
          </div>
        </nav>

        <div className="landing__hero-content">
          <p className="landing__hero-label">Bienvenue au</p>
          <h1 className="landing__hero-title">Grand Hôtel Royal</h1>
          <p className="landing__hero-sub">
            Une expérience d'exception au cœur de la ville — luxe, confort et service personnalisé
          </p>
          <div className="landing__hero-btns">
            <button className="landing__btn landing__btn--gold landing__btn--lg" onClick={() => navigate('/register')}>
              Réserver une chambre
            </button>
            <a href="#contact" className="landing__btn landing__btn--outline landing__btn--lg">
              Nous contacter
            </a>
          </div>
        </div>
      </header>

      {/* ── INFO RAPIDES ── */}
      <section className="landing__infobanner">
        <div className="landing__infocard">
          <span className="landing__infocard-icon">📍</span>
          <div>
            <strong>Adresse</strong>
            <p>12 Avenue Mohammed V, Rabat 10000</p>
          </div>
        </div>
        <div className="landing__infocard">
          <span className="landing__infocard-icon">📞</span>
          <div>
            <strong>Téléphone</strong>
            <p>+212 537 70 00 00</p>
          </div>
        </div>
        <div className="landing__infocard">
          <span className="landing__infocard-icon">✉️</span>
          <div>
            <strong>Email</strong>
            <p>contact@grandhotelroyal.ma</p>
          </div>
        </div>
        <div className="landing__infocard">
          <span className="landing__infocard-icon">🕐</span>
          <div>
            <strong>Réception 24h/24</strong>
            <p>Check-in : 14h00 — Check-out : 12h00</p>
          </div>
        </div>
      </section>

      {/* ── À PROPOS ── */}
      <section className="landing__about">
        <div className="landing__about-text">
          <p className="landing__section-label">À propos</p>
          <h2 className="landing__section-title">Un hôtel de prestige depuis 1985</h2>
          <p className="landing__about-desc">
            Niché au cœur de Rabat, le Grand Hôtel Royal accueille ses hôtes depuis plus de 38 ans
            dans un cadre alliant architecture marocaine traditionnelle et confort contemporain.
          </p>
          <p className="landing__about-desc">
            Nos équipes dévouées sont disponibles 24h/24 pour vous garantir un séjour inoubliable,
            que vous voyagiez pour affaires ou pour le plaisir.
          </p>
          <div className="landing__stats">
            <div className="landing__stat">
              <span className="landing__stat-num">120</span>
              <span className="landing__stat-lbl">Chambres</span>
            </div>
            <div className="landing__stat">
              <span className="landing__stat-num">38</span>
              <span className="landing__stat-lbl">Ans d'expérience</span>
            </div>
            <div className="landing__stat">
              <span className="landing__stat-num">4.8</span>
              <span className="landing__stat-lbl">Note moyenne</span>
            </div>
          </div>
        </div>
        <div className="landing__about-img">
          <div className="landing__about-img-inner">
            <div className="landing__about-badge">★★★★★</div>
          </div>
        </div>
      </section>

      {/* ── CHAMBRES ── */}
      <section className="landing__rooms">
        <p className="landing__section-label landing__section-label--center">Nos chambres</p>
        <h2 className="landing__section-title landing__section-title--center">
          Confort et élégance à chaque étage
        </h2>
        <div className="landing__rooms-grid">
          {[
            { type: 'Standard', desc: 'Chambre confortable avec vue sur le jardin', price: 'À partir de 600 DH/nuit', emoji: '🛏️' },
            { type: 'Double', desc: 'Spacieuse, idéale pour les couples et les familles', price: 'À partir de 900 DH/nuit', emoji: '🛏️' },
            { type: 'Suite', desc: 'Suite de luxe avec salon séparé et vue panoramique', price: 'À partir de 1 800 DH/nuit', emoji: '👑' },
          ].map(room => (
            <div key={room.type} className="landing__room-card">
              <div className="landing__room-img">
                <span className="landing__room-emoji">{room.emoji}</span>
                <span className="landing__room-type">{room.type}</span>
              </div>
              <div className="landing__room-body">
                <p className="landing__room-desc">{room.desc}</p>
                <p className="landing__room-price">{room.price}</p>
                <button
                  className="landing__btn landing__btn--gold"
                  onClick={() => navigate('/register')}
                >
                  Réserver
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── SERVICES ── */}
      <section className="landing__services">
        <p className="landing__section-label landing__section-label--center">Services</p>
        <h2 className="landing__section-title landing__section-title--center">
          Tout pour votre bien-être
        </h2>
        <div className="landing__services-grid">
          {[
            { icon: '🍽️', name: 'Restaurant gastronomique', desc: 'Cuisine marocaine et internationale, ouvert midi et soir' },
            { icon: '🏊', name: 'Piscine & Spa', desc: 'Détente et relaxation dans notre espace bien-être de 800 m²' },
            { icon: '🅿️', name: 'Parking privé', desc: 'Parking sécurisé et gratuit pour tous nos clients' },
            { icon: '📶', name: 'Wi-Fi haut débit', desc: 'Connexion internet fibre optique dans tout l\'hôtel' },
            { icon: '🚐', name: 'Navette aéroport', desc: 'Service de transfert disponible sur réservation' },
            { icon: '💼', name: 'Centre d\'affaires', desc: 'Salles de réunion équipées et secrétariat à votre service' },
          ].map(s => (
            <div key={s.name} className="landing__service-card">
              <span className="landing__service-icon">{s.icon}</span>
              <h3 className="landing__service-name">{s.name}</h3>
              <p className="landing__service-desc">{s.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── CONTACT ── */}
      <section className="landing__contact" id="contact">
        <div className="landing__contact-inner">
          <p className="landing__section-label">Contact</p>
          <h2 className="landing__section-title">Contactez-nous</h2>
          <div className="landing__contact-grid">
            <div className="landing__contact-item">
              <span className="landing__contact-icon">📍</span>
              <div>
                <strong>Adresse</strong>
                <p>12 Avenue Mohammed V<br />Rabat 10000, Maroc</p>
              </div>
            </div>
            <div className="landing__contact-item">
              <span className="landing__contact-icon">📞</span>
              <div>
                <strong>Téléphone</strong>
                <p>+212 537 70 00 00</p>
                <p>+212 537 70 00 01 (Fax)</p>
              </div>
            </div>
            <div className="landing__contact-item">
              <span className="landing__contact-icon">✉️</span>
              <div>
                <strong>Email</strong>
                <p>contact@grandhotelroyal.ma</p>
                <p>reservation@grandhotelroyal.ma</p>
              </div>
            </div>
            <div className="landing__contact-item">
              <span className="landing__contact-icon">🕐</span>
              <div>
                <strong>Horaires réception</strong>
                <p>Ouvert 24h/24 — 7j/7</p>
                <p>Check-in : 14h00 | Check-out : 12h00</p>
              </div>
            </div>
          </div>
          <div className="landing__contact-cta">
            <button
              className="landing__btn landing__btn--gold landing__btn--lg"
              onClick={() => navigate('/register')}
            >
              Créer un compte & Réserver
            </button>
            <button
              className="landing__btn landing__btn--outline landing__btn--lg"
              onClick={() => navigate('/login')}
            >
              Déjà client ? Se connecter
            </button>
          </div>
        </div>
      </section>

      {/* ── FOOTER ── */}
      <footer className="landing__footer">
        <div className="landing__footer-logo">
          <span className="landing__logo-icon">✦</span>
          <span className="landing__logo-text">Grand Hôtel Royal</span>
        </div>
        <p className="landing__footer-copy">
          © {new Date().getFullYear()} Grand Hôtel Royal — Rabat, Maroc. Tous droits réservés.
        </p>
      </footer>

    </div>
  );
};

export default HotelLandingPage;