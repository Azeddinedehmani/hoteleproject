import React from 'react';
import './Pagination.css';

/**
 * Pagination — contrôles de pagination réutilisables
 * Props: page, totalPages, onPage
 */
const Pagination = ({ page, totalPages, onPage }) => {
  if (totalPages <= 1) return null;

  const pages = [];
  const delta = 2;
  const left  = Math.max(2, page - delta);
  const right = Math.min(totalPages - 1, page + delta);

  pages.push(1);
  if (left > 2) pages.push('…');
  for (let i = left; i <= right; i++) pages.push(i);
  if (right < totalPages - 1) pages.push('…');
  if (totalPages > 1) pages.push(totalPages);

  return (
    <div className="pagination">
      <button
        className="pagination__btn"
        onClick={() => onPage(page - 1)}
        disabled={page === 1}
        aria-label="Page précédente"
      >
        ‹
      </button>

      {pages.map((p, i) =>
        p === '…' ? (
          <span key={`ellipsis-${i}`} className="pagination__ellipsis">…</span>
        ) : (
          <button
            key={p}
            className={`pagination__btn ${p === page ? 'is-active' : ''}`}
            onClick={() => onPage(p)}
          >
            {p}
          </button>
        )
      )}

      <button
        className="pagination__btn"
        onClick={() => onPage(page + 1)}
        disabled={page === totalPages}
        aria-label="Page suivante"
      >
        ›
      </button>

      <span className="pagination__info">
        Page {page} / {totalPages}
      </span>
    </div>
  );
};

export default Pagination;