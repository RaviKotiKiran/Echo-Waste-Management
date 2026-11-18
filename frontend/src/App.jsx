import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import AppErrorBoundary from './components/AppErrorBoundary.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { useAuth } from './context/AuthContext.jsx';

const Home = lazy(() => import('./pages/Home.jsx'));
const Login = lazy(() => import('./pages/Login.jsx'));
const Register = lazy(() => import('./pages/Register.jsx'));
const PortalLayout = lazy(() => import('./pages/PortalLayout.jsx'));
const Dashboard = lazy(() => import('./pages/Dashboard.jsx'));
const RequestForm = lazy(() => import('./pages/RequestForm.jsx'));
const MyRequests = lazy(() => import('./pages/MyRequests.jsx'));
const Assignments = lazy(() => import('./pages/Assignments.jsx'));
const Management = lazy(() => import('./pages/Management.jsx'));
const Profile = lazy(() => import('./pages/Profile.jsx'));
const WasteGuide = lazy(() => import('./pages/WasteGuide.jsx'));
const Issues = lazy(() => import('./pages/Issues.jsx'));
const IssueManagement = lazy(() => import('./pages/IssueManagement.jsx'));
const CollectionCalendar = lazy(() => import('./pages/CollectionCalendar.jsx'));

function StartRoute() {
  const { user } = useAuth();
  return <Navigate to={user ? '/app' : '/'} replace />;
}

function PageLoading() {
  return (
    <main className="app-loading" role="status" aria-live="polite">
      <span className="app-loading-mark" aria-hidden="true">e</span>
      <span>Preparing your Echo workspace…</span>
    </main>
  );
}

export default function App() {
  return (
    <AppErrorBoundary>
      <Suspense fallback={<PageLoading />}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/guide" element={<WasteGuide />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/app" element={<PortalLayout />}>
              <Route index element={<Dashboard />} />
              <Route path="guide" element={<WasteGuide />} />
              <Route element={<ProtectedRoute roles={['CITIZEN']} />}>
                <Route path="calendar" element={<CollectionCalendar />} />
                <Route path="requests/new" element={<RequestForm />} />
                <Route path="requests" element={<MyRequests />} />
                <Route path="issues" element={<Issues />} />
              </Route>
              <Route element={<ProtectedRoute roles={['COLLECTOR']} />}>
                <Route path="assignments" element={<Assignments />} />
              </Route>
              <Route element={<ProtectedRoute roles={['ADMIN']} />}>
                <Route path="manage" element={<Management />} />
                <Route path="issue-queue" element={<IssueManagement />} />
              </Route>
              <Route path="profile" element={<Profile />} />
            </Route>
          </Route>
          <Route path="*" element={<StartRoute />} />
        </Routes>
      </Suspense>
    </AppErrorBoundary>
  );
}
