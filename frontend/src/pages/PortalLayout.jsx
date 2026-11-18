import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { FiActivity, FiAlertCircle, FiBookOpen, FiCalendar, FiClipboard, FiGrid, FiLogOut, FiPlusCircle, FiSettings, FiUsers } from 'react-icons/fi';
import { useAuth } from '../context/AuthContext';
import NotificationCenter from '../components/NotificationCenter.jsx';

const labels = { CITIZEN: 'Resident portal', COLLECTOR: 'Collector portal', ADMIN: 'Administration' };
const navigation = {
  ADMIN: [['/app', <FiGrid />, 'Overview'], ['/app/manage', <FiUsers />, 'Manage operations'], ['/app/issue-queue', <FiAlertCircle />, 'Service issues'], ['/app/guide', <FiBookOpen />, 'Waste guide'], ['/app/profile', <FiSettings />, 'Profile']],
  COLLECTOR: [['/app', <FiGrid />, 'Overview'], ['/app/assignments', <FiClipboard />, 'My assignments'], ['/app/guide', <FiBookOpen />, 'Waste guide'], ['/app/profile', <FiSettings />, 'Profile']],
  CITIZEN: [['/app', <FiGrid />, 'Overview'], ['/app/calendar', <FiCalendar />, 'Collection calendar'], ['/app/requests/new', <FiPlusCircle />, 'Request a pickup'], ['/app/requests', <FiActivity />, 'My requests'], ['/app/issues', <FiAlertCircle />, 'Report an issue'], ['/app/guide', <FiBookOpen />, 'Waste guide'], ['/app/profile', <FiSettings />, 'Profile']],
};

export default function PortalLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  function signOut() { logout(); navigate('/'); }
  return <div className="portal"><aside className="sidebar"><NavLink className="echo-brand" to="/"><span className="brand-mark">e</span> echo<span className="brand-dot">.</span></NavLink><div className="sidebar-label">{labels[user.role]}</div><nav aria-label="Workspace navigation">{navigation[user.role].map(([to, icon, label]) => <NavLink key={to} end={to === '/app'} to={to} className={({ isActive }) => `side-link ${isActive ? 'active' : ''}`}>{icon}<span>{label}</span></NavLink>)}</nav><div className="sidebar-bottom"><div className="avatar">{user.fullName?.slice(0, 1).toUpperCase()}</div><div className="side-user"><b>{user.fullName}</b><small>{user.role.toLowerCase()}</small></div><button className="icon-button" title="Sign out" aria-label="Sign out" onClick={signOut}><FiLogOut /></button></div></aside><main className="portal-main"><header className="portal-top"><div><small>GOOD THINGS ARE IN MOTION</small><h1>{user.role === 'ADMIN' ? 'Operations dashboard' : user.role === 'COLLECTOR' ? 'Collection workspace' : 'Your Echo dashboard'}</h1></div><div className="portal-top-actions"><div className="top-user"><span className="live-dot" /> Connected as {user.fullName}</div><NotificationCenter /></div></header><div className="portal-content"><Outlet /></div></main></div>;
}
