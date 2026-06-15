import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useEffect } from 'react';

// 页面组件
import HomePage from '@/pages/HomePage';
import AuthPage from '@/pages/AuthPage';
import ActivitiesPage from '@/pages/ActivitiesPage';
import ActivityDetailPage from '@/pages/ActivityDetailPage';
import ProfilePage from '@/pages/ProfilePage';
import AdminDashboardPage from '@/pages/AdminDashboardPage';
import AdminUsersPage from '@/pages/AdminUsersPage';
import AdminActivitiesPage from '@/pages/AdminActivitiesPage';
import AdminStatisticsPage from '@/pages/AdminStatisticsPage';
import AdminSettingsPage from '@/pages/AdminSettingsPage';
import PublishActivityPage from '@/pages/PublishActivityPage';
import NotificationsPage from '@/pages/NotificationsPage';
import MyActivitiesPage from '@/pages/MyActivitiesPage';
import MyRegistrationsPage from '@/pages/MyRegistrationsPage';
import ActivityRegistrationsPage from '@/pages/ActivityRegistrationsPage';

// 保护路由组件
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('token');
  
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
}

// 管理员路由保护
function AdminRoute({ children }: { children: React.ReactNode }) {
  const userStr = localStorage.getItem('user');
  
  if (!userStr) {
    return <Navigate to="/login" replace />;
  }
  
  const user = JSON.parse(userStr);
  if (user.role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }
  
  return <>{children}</>;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 公开路由 */}
        <Route path="/login" element={<AuthPage />} />
        
        {/* 受保护的路由 */}
        <Route path="/" element={<HomePage />} />
        <Route
          path="/activities"
          element={
            <ProtectedRoute>
              <ActivitiesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/activities/:id"
          element={
            <ProtectedRoute>
              <ActivityDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/notifications"
          element={
            <ProtectedRoute>
              <NotificationsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/activities/create"
          element={
            <ProtectedRoute>
              <PublishActivityPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-activities"
          element={
            <ProtectedRoute>
              <MyActivitiesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-registrations"
          element={
            <ProtectedRoute>
              <MyRegistrationsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/activities/:id/registrations"
          element={
            <ProtectedRoute>
              <ActivityRegistrationsPage />
            </ProtectedRoute>
          }
        />
        
        {/* 管理员路由 */}
        <Route
          path="/admin"
          element={
            <AdminRoute>
              <AdminDashboardPage />
            </AdminRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <AdminRoute>
              <AdminUsersPage />
            </AdminRoute>
          }
        />
        <Route
          path="/admin/activities"
          element={
            <AdminRoute>
              <AdminActivitiesPage />
            </AdminRoute>
          }
        />
        <Route
          path="/admin/statistics"
          element={
            <AdminRoute>
              <AdminStatisticsPage />
            </AdminRoute>
          }
        />
        <Route
          path="/admin/settings"
          element={
            <AdminRoute>
              <AdminSettingsPage />
            </AdminRoute>
          }
        />
        
        {/* 404 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
