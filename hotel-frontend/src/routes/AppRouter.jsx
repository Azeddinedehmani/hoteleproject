import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Layouts
import AuthLayout      from '../layouts/AuthLayout';
import DashboardLayout from '../layouts/DashboardLayout';
import PrivateRoute    from './PrivateRoute';

// Landing page
import HotelLandingPage from '../pages/HotelLandingPage';

// Auth pages
import LoginPage    from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';

// Shared
import UnauthorizedPage from '../pages/UnauthorizedPage';

// Client pages
import ClientDashboard    from '../pages/client/ClientDashboard';
import RoomsPage          from '../pages/client/RoomsPage';
import BookingPage        from '../pages/client/BookingPage';
import MyReservationsPage from '../pages/client/MyReservationsPage';
import MyInvoicesPage     from '../pages/client/MyInvoicesPage';

// Receptionist pages
import ReceptionDashboard from '../pages/receptionist/ReceptionDashboard';
import ClientsPage        from '../pages/receptionist/ClientsPage';
import ReservationsPage   from '../pages/receptionist/ReservationsPage';
import CheckInOutPage     from '../pages/receptionist/CheckInOutPage';
import InvoicesPage       from '../pages/receptionist/InvoicesPage';

// Admin pages
import AdminDashboard from '../pages/admin/AdminDashboard';
import AdminUsers     from '../pages/admin/AdminUsers';
import AdminRooms     from '../pages/admin/AdminRooms';
import AdminTariffs   from '../pages/admin/AdminTariffs';
import AdminEquipment from '../pages/admin/AdminEquipment';

const RoleRedirect = () => {
  const { user } = useAuth();
  if (user?.role === 'admin')        return <Navigate to="/admin/dashboard" replace />;
  if (user?.role === 'receptionist') return <Navigate to="/reception/dashboard" replace />;
  return <Navigate to="/client/dashboard" replace />;
};

const AppRouter = ({ toast }) => (
  <BrowserRouter>
    <Routes>

      {/* ── Landing ── */}
      <Route path="/" element={<HotelLandingPage />} />

      {/* ── Public ── */}
      <Route element={<AuthLayout />}>
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      {/* ── Protected ── */}
      <Route element={<PrivateRoute />}>
        <Route element={<DashboardLayout toast={toast} />}>

          <Route path="/dashboard"    element={<RoleRedirect />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/* Admin */}
          <Route element={<PrivateRoute allowedRoles={['admin']} />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/users"     element={<AdminUsers     toast={toast} />} />
            <Route path="/admin/rooms"     element={<AdminRooms     toast={toast} />} />
            <Route path="/admin/tariffs"   element={<AdminTariffs   toast={toast} />} />
            <Route path="/admin/equipment" element={<AdminEquipment toast={toast} />} />
          </Route>

          {/* Receptionist + Admin */}
          {/* FIX : toast passé à toutes les pages réceptionniste, y compris CheckInOutPage */}
          <Route element={<PrivateRoute allowedRoles={['admin', 'receptionist']} />}>
            <Route path="/reception/dashboard"    element={<ReceptionDashboard toast={toast} />} />
            <Route path="/reception/clients"      element={<ClientsPage        toast={toast} />} />
            <Route path="/reception/reservations" element={<ReservationsPage   toast={toast} />} />
            <Route path="/reception/checkin"      element={<CheckInOutPage     toast={toast} />} />
            <Route path="/reception/invoices"     element={<InvoicesPage       toast={toast} />} />
          </Route>

          {/* Client + Admin + Receptionist */}
          <Route element={<PrivateRoute allowedRoles={['client', 'admin', 'receptionist']} />}>
            <Route path="/client/dashboard"    element={<ClientDashboard />} />
            <Route path="/client/rooms"        element={<RoomsPage />} />
            <Route path="/client/book"         element={<BookingPage />} />
            <Route path="/client/reservations" element={<MyReservationsPage />} />
            <Route path="/client/invoices"     element={<MyInvoicesPage />} />
          </Route>

        </Route>
      </Route>

      {/* Fallback */}
      <Route path="*"  element={<Navigate to="/" replace />} />

    </Routes>
  </BrowserRouter>
);

export default AppRouter;