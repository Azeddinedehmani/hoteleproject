import React from 'react';
import { AuthProvider } from './context/AuthContext';
import AppRouter from './routes/AppRouter';
import { useToast } from './hooks/useToast';
import ToastContainer from './components/common/ToastContainer';

const AppWithToast = () => {
  const { toasts, toast, dismiss } = useToast();

  return (
    <>
      <AppRouter toast={toast} />
      <ToastContainer toasts={toasts} onDismiss={dismiss} />
    </>
  );
};

const App = () => (
  <AuthProvider>
    <AppWithToast />
  </AuthProvider>
);

export default App;