import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  FiArrowUpRight,
  FiDownload,
  FiRefreshCw,
  FiSearch,
  FiX,
} from 'react-icons/fi';
import api from '../services/api';

const filters = [
  { id: 'ALL', label: 'All requests', matches: () => true },
  { id: 'PENDING', label: 'Awaiting review', matches: request => request.status === 'PENDING' },
  { id: 'ACTIVE', label: 'In progress', matches: request => ['APPROVED', 'ASSIGNED', 'IN_PROGRESS'].includes(request.status) },
  { id: 'COLLECTED', label: 'Collected', matches: request => request.status === 'COLLECTED' },
  { id: 'CLOSED', label: 'Closed', matches: request => ['CANCELLED', 'REJECTED'].includes(request.status) },
];

function displayDate(value) {
  if (!value) return 'Date not set';
  const [year, month, day] = value.split('-').map(Number);
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(year, month - 1, day));
}

function downloadCsv(requests) {
  const columns = ['Request ID', 'Waste category', 'Quantity (kg)', 'Pickup address', 'Pickup date', 'Pickup time', 'Status'];
  const quote = value => {
    const text = String(value ?? '');
    const spreadsheetSafe = /^[\t\r=+\-@]/.test(text) ? `'${text}` : text;
    return `"${spreadsheetSafe.replaceAll('"', '""')}"`;
  };
  const rows = requests.map(request => [
    `EW-${String(request.id).padStart(4, '0')}`,
    request.wasteType,
    request.quantity,
    request.pickupAddress,
    request.pickupDate,
    request.pickupTime?.slice(0, 5),
    request.status.replaceAll('_', ' '),
  ]);
  const csv = [columns, ...rows].map(row => row.map(quote).join(',')).join('\r\n');
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'echo-pickup-history.csv';
  document.body.append(link);
  link.click();
  link.remove();
  window.setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export default function MyRequests() {
  const location = useLocation();
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(location.state?.notice || '');
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [activeFilter, setActiveFilter] = useState('ALL');
  const [feedback, setFeedback] = useState({});
  const [sent, setSent] = useState({});

  const loadRequests = useCallback(async () => {
    setError('');
    setLoading(true);
    try {
      const response = await api.get('/requests/my-requests');
      setItems(response.data);
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Could not load your pickup history.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadRequests(); }, [loadRequests]);

  const visibleRequests = useMemo(() => {
    const selectedFilter = filters.find(filter => filter.id === activeFilter) || filters[0];
    const query = search.trim().toLowerCase();
    return items.filter(request => {
      const matchesQuery = !query || [
        `ew-${String(request.id).padStart(4, '0')}`,
        request.id,
        request.wasteType,
        request.pickupAddress,
        request.status,
      ].some(value => String(value ?? '').toLowerCase().includes(query));
      return matchesQuery && selectedFilter.matches(request);
    });
  }, [activeFilter, items, search]);

  function updateFeedback(id, patch) {
    setFeedback(current => ({
      ...current,
      [id]: { rating: 5, comments: '', ...current[id], ...patch },
    }));
  }

  async function cancelRequest(id) {
    setError('');
    try {
      await api.put(`/requests/${id}/cancel`);
      setNotice('Pickup request cancelled.');
      await loadRequests();
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Could not cancel this request.');
    }
  }

  async function sendFeedback(id) {
    const response = feedback[id] || { rating: 5, comments: '' };
    setError('');
    try {
      await api.post('/feedback', {
        requestId: id,
        rating: Number(response.rating),
        comments: response.comments,
      });
      setSent(current => ({ ...current, [id]: true }));
      setNotice('Thanks for sharing feedback about your collection.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Could not submit your feedback.');
    }
  }

  return (
    <>
      <div className="page-heading requests-heading">
        <div>
          <div className="eyebrow">COLLECTION HISTORY</div>
          <h2>My pickup requests</h2>
          <p>Search your pickups, follow their progress, or export a copy of your history.</p>
        </div>
        <div className="requests-heading-actions">
          {items.length > 0 && (
            <button
              className="btn btn-outline-echo"
              type="button"
              onClick={() => downloadCsv(visibleRequests)}
              disabled={visibleRequests.length === 0}
            >
              <FiDownload aria-hidden="true" /> Export visible
            </button>
          )}
          <Link className="btn btn-echo" to="/app/requests/new">
            New pickup <FiArrowUpRight aria-hidden="true" />
          </Link>
        </div>
      </div>

      {notice && <div className="alert alert-success" role="status">{notice}</div>}
      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      <section className="panel request-history-panel" aria-labelledby="request-history-title">
        <div className="request-history-toolbar">
          <div>
            <div className="eyebrow">YOUR SERVICE ACTIVITY</div>
            <h3 id="request-history-title">Pickup history</h3>
          </div>
          <label className="request-search">
            <FiSearch aria-hidden="true" />
            <span className="visually-hidden">Search pickup requests</span>
            <input
              type="search"
              value={search}
              onChange={event => setSearch(event.target.value)}
              placeholder="Search by ID, waste, or address"
            />
          </label>
        </div>

        {!loading && items.length > 0 && (
          <div className="request-filter-list" aria-label="Filter pickup requests">
            {filters.map(filter => {
              const count = filter.id === 'ALL'
                ? items.length
                : items.filter(filter.matches).length;
              return (
                <button
                  key={filter.id}
                  className={`request-filter ${activeFilter === filter.id ? 'active' : ''}`}
                  type="button"
                  aria-pressed={activeFilter === filter.id}
                  onClick={() => setActiveFilter(filter.id)}
                >
                  {filter.label}<span>{count}</span>
                </button>
              );
            })}
          </div>
        )}

        {loading ? (
          <p className="empty-state" role="status">Loading your pickup history…</p>
        ) : error && items.length === 0 ? (
          <div className="request-empty-state">
            <p>We couldn’t retrieve your pickup history.</p>
            <button className="btn btn-outline-echo" type="button" onClick={loadRequests}>
              <FiRefreshCw aria-hidden="true" /> Try again
            </button>
          </div>
        ) : items.length === 0 ? (
          <div className="request-empty-state">
            <p>You haven’t requested a pickup yet.</p>
            <Link to="/app/requests/new" className="text-link">Create your first pickup <FiArrowUpRight aria-hidden="true" /></Link>
          </div>
        ) : visibleRequests.length === 0 ? (
          <div className="request-empty-state">
            <p>No pickups match those filters.</p>
            <button className="btn btn-outline-echo" type="button" onClick={() => { setSearch(''); setActiveFilter('ALL'); }}>
              Clear filters
            </button>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table align-middle activity-table request-history-table">
              <thead>
                <tr><th>REQUEST</th><th>WASTE</th><th>PICKUP DATE</th><th>STATUS</th><th>ACTION</th></tr>
              </thead>
              <tbody>
                {visibleRequests.map(request => (
                  <tr key={request.id}>
                    <td><b>EW-{String(request.id).padStart(4, '0')}</b><small>{request.pickupAddress}</small></td>
                    <td>{request.wasteType}<small>{request.quantity} kg</small></td>
                    <td>{displayDate(request.pickupDate)}<small>{request.pickupTime?.slice(0, 5)}</small></td>
                    <td>
                      <span className={`status-badge ${request.status.toLowerCase()}`}>
                        {request.status.replaceAll('_', ' ').toLowerCase()}
                      </span>
                      {request.collectorName && <small>Collector: {request.collectorName}</small>}
                    </td>
                    <td className="request-action-cell">
                      {request.status === 'PENDING' ? (
                        <button className="btn btn-sm btn-outline-danger" type="button" onClick={() => cancelRequest(request.id)}>
                          <FiX aria-hidden="true" /> Cancel
                        </button>
                      ) : request.status === 'COLLECTED' && !sent[request.id] ? (
                        <button className="btn btn-sm btn-outline-echo" type="button" onClick={() => updateFeedback(request.id, { open: !feedback[request.id]?.open })}>
                          {feedback[request.id]?.open ? 'Hide feedback' : 'Rate pickup'}
                        </button>
                      ) : sent[request.id] ? <small>Feedback received</small> : <small>—</small>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {visibleRequests.filter(request => request.status === 'COLLECTED' && feedback[request.id]?.open && !sent[request.id]).map(request => (
              <div className="feedback-form" key={`feedback-${request.id}`}>
                <b>Rate pickup EW-{String(request.id).padStart(4, '0')}</b>
                <div className="row g-2 mt-1">
                  <div className="col-md-2">
                    <label className="visually-hidden" htmlFor={`rating-${request.id}`}>Pickup rating</label>
                    <select id={`rating-${request.id}`} className="form-select" value={feedback[request.id]?.rating || 5} onChange={event => updateFeedback(request.id, { rating: event.target.value })}>
                      {[5, 4, 3, 2, 1].map(rating => <option key={rating} value={rating}>{rating} / 5</option>)}
                    </select>
                  </div>
                  <div className="col-md-8">
                    <label className="visually-hidden" htmlFor={`comments-${request.id}`}>Optional feedback comment</label>
                    <input id={`comments-${request.id}`} className="form-control" maxLength="1000" placeholder="Share a comment (optional)" value={feedback[request.id]?.comments || ''} onChange={event => updateFeedback(request.id, { comments: event.target.value })} />
                  </div>
                  <div className="col-md-2">
                    <button className="btn btn-echo w-100" type="button" onClick={() => sendFeedback(request.id)}>Send rating</button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </>
  );
}
