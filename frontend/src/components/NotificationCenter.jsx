import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiAlertCircle, FiBell, FiCheck, FiCheckCircle, FiClock, FiInbox } from 'react-icons/fi';
import api, { apiErrorMessage } from '../services/api';

function relativeTime(value) {
  const elapsed = Math.max(0, Date.now() - new Date(value).getTime());
  const minutes = Math.floor(elapsed / 60000);
  if (minutes < 1) return 'Just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  return new Date(value).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

export default function NotificationCenter() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState({ notifications: [], unreadCount: 0 });
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async (silent = false) => {
    try {
      const { data } = await api.get('/notifications');
      setSummary(data);
      setError('');
    } catch (requestError) {
      if (!silent) setError(apiErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(true);
    const timer = window.setInterval(() => load(true), 45000);
    return () => window.clearInterval(timer);
  }, [load]);

  useEffect(() => {
    if (!open) return undefined;
    function handleKeyDown(event) {
      if (event.key === 'Escape') setOpen(false);
    }
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [open]);

  async function openNotification(item) {
    setOpen(false);
    if (!item.read) {
      try {
        const { data } = await api.patch(`/notifications/${item.id}/read`);
        setSummary(data);
      } catch (requestError) {
        setError(apiErrorMessage(requestError));
      }
    }
    navigate(item.linkPath);
  }

  async function markAllRead() {
    if (!summary.unreadCount || busy) return;
    setBusy(true);
    setError('');
    try {
      const { data } = await api.post('/notifications/read-all');
      setSummary(data);
    } catch (requestError) {
      setError(apiErrorMessage(requestError));
    } finally {
      setBusy(false);
    }
  }

  return <div className="notification-center">
    <button type="button" className={`notification-bell ${open ? 'is-open' : ''}`} aria-label={`Notifications${summary.unreadCount ? `, ${summary.unreadCount} unread` : ''}`} aria-expanded={open} aria-controls="notification-menu" onClick={() => { setOpen(!open); if (!open) load(); }}>
      <FiBell />{summary.unreadCount > 0 && <span className="notification-badge">{summary.unreadCount > 9 ? '9+' : summary.unreadCount}</span>}
    </button>
    {open && <>
      <button className="notification-scrim" aria-label="Close notifications" onClick={() => setOpen(false)} />
      <section className="notification-menu" id="notification-menu" aria-label="Notifications">
        <header className="notification-menu-head"><div><span className="eyebrow">YOUR ECHO UPDATES</span><h2>Notifications{summary.unreadCount > 0 && <span>{summary.unreadCount} new</span>}</h2></div><button type="button" className="notification-read-all" onClick={markAllRead} disabled={!summary.unreadCount || busy}>{busy ? 'Saving…' : <><FiCheck /> Mark all read</>}</button></header>
        {error && <p className="notification-error" role="alert">{error}</p>}
        {loading ? <div className="notification-empty"><span className="notification-empty-icon"><FiClock /></span><b>Loading updates</b><small>Checking for your latest activity…</small></div> : summary.notifications.length === 0 ? <div className="notification-empty"><span className="notification-empty-icon"><FiInbox /></span><b>You’re all caught up</b><small>Important pickup and service updates will appear here.</small></div> : <div className="notification-items" aria-live="polite">{summary.notifications.map((item) => <button type="button" key={item.id} className={`notification-item ${item.read ? '' : 'unread'}`} onClick={() => openNotification(item)}><span className={`notification-item-icon ${item.kind.toLowerCase()}`}>{item.kind === 'ISSUE' ? <FiAlertCircle /> : <FiCheckCircle />}</span><span className="notification-item-copy"><b>{item.title}</b><span>{item.message}</span><small>{relativeTime(item.createdAt)}</small></span>{!item.read && <span className="notification-unread-dot" aria-label="Unread" />}</button>)}</div>}
        <footer className="notification-menu-foot"><FiCheck /> Updates are saved to your account</footer>
      </section>
    </>}
  </div>;
}
