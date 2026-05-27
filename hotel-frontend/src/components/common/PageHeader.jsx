import React from 'react';
import './PageHeader.css';

/**
 * PageHeader — en-tête de page standard
 * Props: title, subtitle, action (JSX button/link)
 */
const PageHeader = ({ title, subtitle, action }) => (
  <div className="page-header">
    <div>
      <h2 className="page-header__title">{title}</h2>
      {subtitle && <p className="page-header__sub">{subtitle}</p>}
    </div>
    {action && <div className="page-header__action">{action}</div>}
  </div>
);

export default PageHeader;