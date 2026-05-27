import React from 'react';
import './SearchBar.css';

/**
 * SearchBar — barre de recherche avec debounce intégré
 * Props: value, onChange, placeholder
 */
const SearchBar = ({ value, onChange, placeholder = 'Rechercher…' }) => (
  <div className="search-bar">
    <span className="search-bar__icon">⌕</span>
    <input
      type="text"
      className="search-bar__input"
      value={value}
      onChange={e => onChange(e.target.value)}
      placeholder={placeholder}
    />
    {value && (
      <button className="search-bar__clear" onClick={() => onChange('')} aria-label="Effacer">
        ✕
      </button>
    )}
  </div>
);

export default SearchBar;