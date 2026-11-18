import { useEffect, useState } from 'react';
import { FiAlertCircle, FiCheckCircle, FiClock, FiRefreshCw } from 'react-icons/fi';
import api, { apiErrorMessage } from '../services/api';

const labels = { MISSED_PICKUP: 'Missed pickup', DAMAGED_BIN: 'Damaged or missing bin', ILLEGAL_DUMPING: 'Illegal dumping', OTHER: 'Other service issue' };
const statuses = ['OPEN', 'IN_REVIEW', 'RESOLVED'];
const icons = { OPEN: <FiClock />, IN_REVIEW: <FiAlertCircle />, RESOLVED: <FiCheckCircle /> };

export default function IssueManagement() {
  const [items, setItems] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  async function load() {
    setLoading(true); setError('');
    try { const response = await api.get('/admin/issues'); setItems(response.data); }
    catch (requestError) { setError(apiErrorMessage(requestError)); }
    finally { setLoading(false); }
  }
  useEffect(() => { load(); }, []);
  async function update(item, status) {
    setBusyId(item.id); setError(''); setNotice('');
    try {
      const { data } = await api.patch(`/admin/issues/${item.id}/status`, { status });
      setItems((current) => current.map((entry) => entry.id === data.id ? data : entry));
      setNotice(`Report #${String(item.id).padStart(4, '0')} updated to ${status.toLowerCase().replace('_', ' ')}.`);
    } catch (requestError) { setError(apiErrorMessage(requestError)); }
    finally { setBusyId(null); }
  }
  const shown = filter === 'ALL' ? items : items.filter((item) => item.status === filter);
  return <>
    <div className="page-heading d-flex justify-content-between align-items-end"><div><div className="eyebrow">SERVICE OPERATIONS</div><h2>Resident issue reports</h2><p>Review incoming service concerns and keep residents informed.</p></div><button className="btn btn-outline-echo" onClick={load} disabled={loading}><FiRefreshCw className={loading ? 'spin' : ''} /> Refresh</button></div>
    {notice && <div className="alert alert-success" role="status">{notice}</div>}{error && <div className="alert alert-danger" role="alert">{error}</div>}
    <div className="issue-filter-bar">{[['ALL', 'All reports'], ...statuses.map((status) => [status, status.replace('_', ' ')])].map(([value, label]) => <button className={filter === value ? 'active' : ''} key={value} onClick={() => setFilter(value)}>{label}<span>{value === 'ALL' ? items.length : items.filter((item) => item.status === value).length}</span></button>)}</div>
    {loading ? <div className="panel empty-state">Loading service reports…</div> : shown.length === 0 ? <div className="panel issue-empty"><span className="issue-empty-icon"><FiCheckCircle /></span><h3>{filter === 'ALL' ? 'No reports yet' : `No ${filter.toLowerCase().replace('_', ' ')} reports`}</h3><p>New resident reports will appear here.</p></div> : <div className="issue-cards admin-issue-cards">{shown.map((item) => <article className="issue-card" key={item.id}><div className="issue-card-heading"><div><span className="issue-ref">REPORT #{String(item.id).padStart(4, '0')} · {new Date(item.createdAt).toLocaleDateString()}</span><h3>{labels[item.category] || item.category}</h3></div><span className={`issue-status ${item.status.toLowerCase()}`}>{icons[item.status]}{item.status.replace('_', ' ')}</span></div><p>{item.description}</p><div className="issue-admin-meta"><div><b>{item.residentName}</b><span>{item.email}</span></div>{item.requestId && <span className="role-pill">Pickup EW-{String(item.requestId).padStart(4, '0')}</span>}</div><div className="issue-actions"><span className="issue-status-label">Update report status</span>{statuses.map((status) => <button key={status} disabled={item.status === status || item.status === 'RESOLVED' || busyId === item.id} className={`${item.status === status ? 'selected' : ''} ${status.toLowerCase()}`} onClick={() => update(item, status)}>{status === 'IN_REVIEW' ? 'In review' : status.charAt(0) + status.slice(1).toLowerCase()}</button>)}</div></article>)}</div>}
  </>;
}
