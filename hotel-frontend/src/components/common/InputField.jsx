import React, { useState } from 'react';
import './InputField.css';

/**
 * InputField — champ de formulaire réutilisable
 *
 * Props :
 *   label       — libellé au-dessus
 *   type        — input type (text, email, password, tel…)
 *   name        — name HTML
 *   value       — valeur contrôlée
 *   onChange    — handler de changement
 *   placeholder — placeholder
 *   error       — message d'erreur à afficher
 *   required    — booléen
 *   autoFocus   — booléen
 */
const InputField = ({
  label,
  type = 'text',
  name,
  value,
  onChange,
  placeholder,
  error,
  required,
  autoFocus,
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const isPassword = type === 'password';
  const resolvedType = isPassword ? (showPassword ? 'text' : 'password') : type;

  return (
    <div className={`input-field ${error ? 'has-error' : ''}`}>
      {label && (
        <label className="input-field__label" htmlFor={name}>
          {label}
          {required && <span className="input-field__required">*</span>}
        </label>
      )}

      <div className="input-field__wrapper">
        <input
          id={name}
          name={name}
          type={resolvedType}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          autoFocus={autoFocus}
          className="input-field__input"
          aria-invalid={!!error}
          aria-describedby={error ? `${name}-error` : undefined}
        />
        {isPassword && (
          <button
            type="button"
            className="input-field__toggle"
            onClick={() => setShowPassword(!showPassword)}
            aria-label={showPassword ? 'Masquer' : 'Afficher'}
          >
            {showPassword ? '○' : '●'}
          </button>
        )}
      </div>

      {error && (
        <p id={`${name}-error`} className="input-field__error" role="alert">
          {error}
        </p>
      )}
    </div>
  );
};

export default InputField;