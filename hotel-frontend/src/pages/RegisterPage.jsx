import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import InputField from '../components/common/InputField';
import './AuthPage.css';

const validate = ({ name, email, password, confirm }) => {
  const errors = {};
  if (!name || name.trim().length < 2)
    errors.name = 'Nom requis (min. 2 caractères)';
  if (!email)
    errors.email = 'L\'email est requis';
  else if (!/\S+@\S+\.\S+/.test(email))
    errors.email = 'Email invalide';
  if (!password)
    errors.password = 'Le mot de passe est requis';
  else if (password.length < 8)
    errors.password = 'Minimum 8 caractères';
  if (password !== confirm)
    errors.confirm = 'Les mots de passe ne correspondent pas';
  return errors;
};

const RegisterPage = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: '', email: '', phone: '', password: '', confirm: '',
  });
  const [errors, setErrors]     = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading]   = useState(false);

  const handleChange = ({ target: { name, value } }) => {
    setForm(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    setApiError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate(form);
    if (Object.keys(validationErrors).length) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    try {
      const { name, email, phone, password } = form;
      // ✅ FIX : role: 'CLIENT' envoyé — obligatoire dans RegisterRequest backend (@NotNull)
      // Seuls les clients peuvent s'inscrire via cette page publique
      await register({ name, email, phone, password, role: 'CLIENT' });
      navigate('/dashboard');
    } catch (err) {
      setApiError(err.message || 'Erreur lors de l\'inscription');
    } finally {
      setLoading(false);
    }
  };

  const passwordStrength = (() => {
    const p = form.password;
    if (!p) return 0;
    let score = 0;
    if (p.length >= 8) score++;
    if (/[A-Z]/.test(p)) score++;
    if (/[0-9]/.test(p)) score++;
    if (/[^A-Za-z0-9]/.test(p)) score++;
    return score;
  })();

  const strengthLabel = ['', 'Faible', 'Moyen', 'Bon', 'Fort'][passwordStrength];
  const strengthClass = ['', 'weak', 'medium', 'good', 'strong'][passwordStrength];

  return (
    <div className="auth-page">
      <div className="auth-page__header">
        <h2 className="auth-page__title">Créer un compte</h2>
        <p className="auth-page__subtitle">Rejoignez notre système de réservation</p>
      </div>

      {apiError && (
        <div className="auth-page__alert" role="alert">
          {apiError}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate className="auth-page__form">
        <InputField
          label="Nom complet"
          type="text"
          name="name"
          value={form.name}
          onChange={handleChange}
          placeholder="Jean Dupont"
          error={errors.name}
          required
          autoFocus
        />

        <InputField
          label="Adresse email"
          type="email"
          name="email"
          value={form.email}
          onChange={handleChange}
          placeholder="vous@example.com"
          error={errors.email}
          required
        />

        <InputField
          label="Téléphone"
          type="tel"
          name="phone"
          value={form.phone}
          onChange={handleChange}
          placeholder="+33 6 12 34 56 78"
          error={errors.phone}
        />

        <div>
          <InputField
            label="Mot de passe"
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder="••••••••"
            error={errors.password}
            required
          />
          {form.password && (
            <div className={`auth-page__strength auth-page__strength--${strengthClass}`}>
              <div className="auth-page__strength-bar">
                {[1,2,3,4].map(i => (
                  <span
                    key={i}
                    className={`auth-page__strength-seg ${i <= passwordStrength ? 'filled' : ''}`}
                  />
                ))}
              </div>
              <span>{strengthLabel}</span>
            </div>
          )}
        </div>

        <InputField
          label="Confirmer le mot de passe"
          type="password"
          name="confirm"
          value={form.confirm}
          onChange={handleChange}
          placeholder="••••••••"
          error={errors.confirm}
          required
        />

        <button
          type="submit"
          className="auth-page__btn"
          disabled={loading}
        >
          {loading ? <span className="auth-page__spinner" /> : 'Créer mon compte'}
        </button>
      </form>
    </div>
  );
};

export default RegisterPage;