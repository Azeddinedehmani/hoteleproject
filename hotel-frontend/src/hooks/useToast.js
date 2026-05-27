import { useState, useCallback } from 'react';

/**
 * useToast — système de notifications globales
 * Retourne : { toasts, toast }
 * toast.success(msg) | toast.error(msg) | toast.info(msg)
 */
export const useToast = () => {
  const [toasts, setToasts] = useState([]);

  const push = useCallback((message, type = 'info', duration = 3500) => {
    const id = Date.now() + Math.random();
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, duration);
  }, []);

  const toast = {
    success: (msg) => push(msg, 'success'),
    error:   (msg) => push(msg, 'error'),
    info:    (msg) => push(msg, 'info'),
    warning: (msg) => push(msg, 'warning'),
  };

  const dismiss = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  }, []);

  return { toasts, toast, dismiss };
};