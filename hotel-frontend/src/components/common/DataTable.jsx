import React from 'react';
import './DataTable.css';

/**
 * DataTable — tableau générique réutilisable
 *
 * Props:
 *   columns  — [{ key, label, render? }]
 *   data     — tableau d'objets
 *   loading  — bool
 *   empty    — message quand vide
 *   actions  — render function (row) => JSX (optionnel)
 */
const DataTable = ({ columns, data = [], loading, empty = 'Aucun résultat', actions }) => {
  if (loading) {
    return (
      <div className="dt-loading">
        <span className="dt-spinner" />
        <span>Chargement…</span>
      </div>
    );
  }

  return (
    <div className="dt-wrapper">
      <table className="dt-table">
        <thead>
          <tr>
            {columns.map(col => (
              <th key={col.key} className="dt-th">{col.label}</th>
            ))}
            {actions && <th className="dt-th dt-th--actions">Actions</th>}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td colSpan={columns.length + (actions ? 1 : 0)} className="dt-empty">
                {empty}
              </td>
            </tr>
          ) : (
            data.map((row, i) => (
              <tr key={row.id ?? i} className="dt-row">
                {columns.map(col => (
                  <td key={col.key} className="dt-td">
                    {col.render ? col.render(row) : (row[col.key] ?? '—')}
                  </td>
                ))}
                {actions && (
                  <td className="dt-td dt-td--actions">
                    {actions(row)}
                  </td>
                )}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

export default DataTable;