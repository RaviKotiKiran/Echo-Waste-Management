import { useEffect, useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { FiAlertCircle, FiArrowUpRight, FiCheckCircle, FiClock, FiPlus, FiX } from 'react-icons/fi';
import api, { apiErrorMessage } from '../services/api';
import { useAuth } from '../context/AuthContext';

const categories = [
  ['MISSED_PICKUP', 'Missed pickup'],
  ['DAMAGED_BIN', 'Damaged or missing bin'],
  ['ILLEGAL_DUMPING', 'Illegal dumping'],
  ['OTHER', 'Other service issue'],
];
const statusIcon = { OPEN: <FiClock />, IN_REVIEW: <FiAlertCircle />, RESOLVED: <FiCheckCircle /> };
const statusName = { OPEN: 'Open', IN_REVIEW: 'In review', RESOLVED: 'Resolved' };

export default function Issues() {
  const { user } = useAuth();
  const [items, setItems] = useState([]);
  const [requests, setRequests] = useState([]);
  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState({ category: 'MISSED_PICKUP', description: '', requestId: '' });
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const [issuesResponse, requestsResponse] = await Promise.all([api.get('/issues'), api.get('/requests/my-requests')]);
      setItems(issuesResponse.data);
      setRequests(requestsResponse.data);
    } catch (requestError) {
      setError(apiErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => { load(); }, []);

  async function submit(event) {
    event.preventDefault();
    setBusy(true); setError(''); setNotice('');
    try {
      await api.post('/issues', { ...form, requestId: form.requestId ? Number(form.requestId) : null, description: form.description.trim() });
      setForm({ category: 'MISSED_PICKUP', description: '', requestId: '' });
      setFormOpen(false); setNotice('Your report was sent to the service team.');
      await load();
    } catch (requestError) { setError(apiErrorMessage(requestError)); }
    finally { setBusy(false); }
  }

  if (user.role !== 'CITIZEN') return <Navigate to="/app" replace />;
  return <>
    <div className="page-heading d-flex justify-content-between align-items-end"><div><div className="eyebrow">RESIDENT SUPPORT</div><h2>Service issues</h2><p>Report a missed collection or another service problem and follow its progress.</p></div><button className="btn btn-echo" onClick={() => setFormOpen(!formOpen)}>{formOpen ? <FiX /> : <FiPlus />}{formOpen ? 'Close form' : 'Report an issue'}</button></div>
    {notice && <div className="alert alert-success" role="status">{notice}</div>}{error && <div className="alert alert-danger" role="alert">{error}</div>}
    {formOpen && <form className="panel issue-form" onSubmit={submit}><div className="panel-head"><div><span className="eyebrow">TELL US WHAT HAPPENED</span><h3>New service report</h3></div></div><div className="row g-3"><div className="col-md-6"><label className="form-label">Issue type<select className="form-select" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })}>{categories.map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></label></div><div className="col-md-6"><label className="form-label">Related pickup <span className="optional-label">OPTIONAL</span><select className="form-select" value={form.requestId} onChange={(event) => setForm({ ...form, requestId: event.target.value })}><option value="">No specific pickup</option>{requests.map((request) => <option key={request.id} value={request.id}>EW-{String(request.id).padStart(4, '0')} · {request.pickupDate} · {request.wasteType}</option>)}</select></label></div><div className="col-12"><label className="form-label">Description<textarea className="form-control" required minLength="8" maxLength="1000" rows="4" placeholder="Share the location and details that will help the team investigate." value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} /></label><small className="character-count">{form.description.length}/1000 characters</small></div></div><div className="form-actions"><button type="button" className="btn btn-outline-echo" onClick={() => setFormOpen(false)}>Cancel</button><button className="btn btn-echo" disabled={busy}>{busy ? 'Sending report…' : 'Send report'} <FiArrowUpRight /></button></div></form>}
    <section className="issue-list" aria-live="polite"><div className="issue-list-heading"><div><span className="eyebrow">YOUR SUPPORT HISTORY</span><h3>Reports and updates</h3></div><span className="guide-count">{items.length} {items.length === 1 ? 'report' : 'reports'}</span></div>{loading ? <div className="panel empty-state">Loading your reports…</div> : items.length === 0 ? <div className="panel issue-empty"><span className="issue-empty-icon"><FiCheckCircle /></span><h3>Nothing needs attention</h3><p>You haven’t reported a service issue. If something goes wrong, you can send the team a report here.</p></div> : <div className="issue-cards">{items.map((item) => <article className="issue-card" key={item.id}><div className="issue-card-heading"><div><span className="issue-ref">REPORT #{String(item.id).padStart(4, '0')}</span><h3>{categories.find(([value]) => value === item.category)?.[1] || item.category}</h3></div><span className={`issue-status ${item.status.toLowerCase()}`}>{statusIcon[item.status]}{statusName[item.status]}</span></div><p>{item.description}</p><div className="issue-card-meta"><span>{new Date(item.createdAt).toLocaleDateString()}</span>{item.requestId && <Link to="/app/requests">Pickup EW-{String(item.requestId).padStart(4, '0')} <FiArrowUpRight /></Link>}</div></article>)}</div>}</section>
  </>;
}
