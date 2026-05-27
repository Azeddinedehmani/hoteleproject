import { useState, useMemo } from 'react';

/**
 * usePagination — pagination générique côté client
 * @param {Array} data — tableau complet
 * @param {number} pageSize — items par page
 */
export const usePagination = (data = [], pageSize = 10) => {
  const [page, setPage] = useState(1);

  const totalPages = Math.max(1, Math.ceil(data.length / pageSize));

  const currentData = useMemo(() => {
    const start = (page - 1) * pageSize;
    return data.slice(start, start + pageSize);
  }, [data, page, pageSize]);

  const goTo    = (p) => setPage(Math.min(Math.max(1, p), totalPages));
  const next    = () => goTo(page + 1);
  const prev    = () => goTo(page - 1);
  const reset   = () => setPage(1);

  return { page, totalPages, currentData, goTo, next, prev, reset, pageSize };
};