import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import InputField from '../components/common/InputField';
import './AuthPage.css';

// ─── Validation ─────────────────────────────────────────────────────────────
const validate = ({ email, password }) => {
  const errors = {};
  if (!email)                        errors.email = 'L\'email est requis';
  else if (!/\S+@\S+\.\S+/.test(email)) errors.email = 'Email invalide';
  if (!password)                     errors.password = 'Le mot de passe est requis';
  else if (password.length < 6)      errors.password = 'Minimum 6 caractères';
  return errors;
};

// ─── Composant ───────────────────────────────────────────────────────────────
const LoginPage = () => {
  const { login } = useAuth();
  const navigate   = useNavigate();
  const location   = useLocation();

  // Redirection vers la page demandée avant le login, ou /dashboard par défaut
  const from = location.state?.from?.pathname || '/dashboard';

  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors]   = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading]  = useState(false);

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
      await login(form);
      navigate(from, { replace: true });
    } catch (err) {
      setApiError(err.message || 'Identifiants incorrects');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-page__header">
        <h2 className="auth-page__title">Connexion</h2>
        <p className="auth-page__subtitle">Accédez à votre espace de gestion</p>
      </div>

      {apiError && (
        <div className="auth-page__alert" role="alert">
          {apiError}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate className="auth-page__form">
        <InputField
          label="Adresse email"
          type="email"
          name="email"
          value={form.email}
          onChange={handleChange}
          placeholder="vous@example.com"
          error={errors.email}
          required
          autoFocus
        />

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

        <div className="auth-page__forgot">
          <a href="/forgot-password">Mot de passe oublié ?</a>
        </div>

        <button
          type="submit"
          className="auth-page__btn"
          disabled={loading}
        >
          {loading ? (
            <span className="auth-page__spinner" />
          ) : (
            'Se connecter'
          )}
        </button>
      </form>
    </div>
  );
};

export default LoginPage;