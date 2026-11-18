import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { FiArrowLeft, FiArrowRight, FiArrowUpRight, FiCalendar, FiCheckCircle, FiClock, FiPlus, FiRefreshCw, FiTruck } from 'react-icons/fi';
import api, { apiErrorMessage } from '../services/api';

const weekdays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const statusInfo = {
  PENDING: { label: 'Awaiting review', tone: 'pending', icon: <FiClock /> },
  APPROVED: { label: 'Approved', tone: 'approved', icon: <FiCheckCircle /> },
  ASSIGNED: { label: 'Collector assigned', tone: 'assigned', icon: <FiTruck /> },
  IN_PROGRESS: { label: 'Collection in progress', tone: 'in-progress', icon: <FiTruck /> },
  COLLECTED: { label: 'Collected', tone: 'collected', icon: <FiCheckCircle /> },
  CANCELLED: { label: 'Cancelled', tone: 'cancelled', icon: <FiClock /> },
  REJECTED: { label: 'Not approved', tone: 'cancelled', icon: <FiClock /> },
};

function monthKey(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
}

function localDateKey(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function displayDate(date, options = { weekday: 'long', month: 'long', day: 'numeric' }) {
  return new Date(`${date}T12:00:00`).toLocaleDateString(undefined, options);
}

export default function CollectionCalendar() {
  const [month, setMonth] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });
  const [selectedDate, setSelectedDate] = useState(() => localDateKey(new Date()));
  const [pickups, setPickups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const key = monthKey(month);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    api.get(`/citizen/calendar?month=${key}`)
      .then(({ data }) => { if (active) setPickups(data.pickups || []); })
      .catch(requestError => { if (active) setError(apiErrorMessage(requestError)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [key, reloadKey]);

  const { cells, title } = useMemo(() => {
    const year = month.getFullYear();
    const monthIndex = month.getMonth();
    const days = new Date(year, monthIndex + 1, 0).getDate();
    const offset = (new Date(year, monthIndex, 1).getDay() + 6) % 7;
    return {
      title: month.toLocaleDateString(undefined, { month: 'long', year: 'numeric' }),
      cells: [...Array(offset).fill(null), ...Array.from({ length: days }, (_, index) => index + 1)],
    };
  }, [month]);

  const pickupsByDate = useMemo(() => pickups.reduce((result, pickup) => {
    (result[pickup.pickupDate] ||= []).push(pickup);
    return result;
  }, {}), [pickups]);
  const selectedPickups = pickupsByDate[selectedDate] || [];
  const today = localDateKey(new Date());
  const upcoming = pickups
    .filter(pickup => pickup.pickupDate >= today && !['CANCELLED', 'REJECTED', 'COLLECTED'].includes(pickup.status))
    .sort((a, b) => `${a.pickupDate}T${a.pickupTime}`.localeCompare(`${b.pickupDate}T${b.pickupTime}`))
    .slice(0, 4);
  const plannedWeight = pickups
    .filter(pickup => !['CANCELLED', 'REJECTED', 'COLLECTED'].includes(pickup.status))
    .reduce((total, pickup) => total + Number(pickup.quantity || 0), 0);

  function changeMonth(amount) {
    const next = new Date(month.getFullYear(), month.getMonth() + amount, 1);
    setMonth(next);
    const now = new Date();
    setSelectedDate(monthKey(next) === monthKey(now) ? localDateKey(now) : localDateKey(next));
  }

  function selectDay(day) {
    setSelectedDate(`${key}-${String(day).padStart(2, '0')}`);
  }

  return (
    <div className="collection-calendar-page">
      <div className="calendar-page-heading">
        <div>
          <Link to="/app" className="back-link"><FiArrowLeft aria-hidden="true" /> Back to overview</Link>
          <div className="eyebrow calendar-eyebrow"><FiCalendar aria-hidden="true" /> YOUR COLLECTION PLANNER</div>
          <h2>Collection calendar</h2>
          <p>See your scheduled pickups and their latest status, all in one place.</p>
        </div>
        <Link to="/app/requests/new" className="btn btn-echo"><FiPlus aria-hidden="true" /> Request a pickup</Link>
      </div>

      {error && <div className="alert alert-danger" role="alert">{error} <button className="calendar-retry" onClick={() => setReloadKey(value => value + 1)}><FiRefreshCw /> Try again</button></div>}

      <div className="calendar-summary-grid">
        <article className="calendar-summary-card summary-green"><span><FiCalendar /></span><div><small>THIS MONTH</small><b>{pickups.length} {pickups.length === 1 ? 'pickup' : 'pickups'}</b><p>Across your schedule</p></div></article>
        <article className="calendar-summary-card summary-violet"><span><FiTruck /></span><div><small>UP NEXT</small><b>{upcoming.length ? displayDate(upcoming[0].pickupDate, { month: 'short', day: 'numeric' }) : 'No upcoming pickup'}</b><p>{upcoming[0] ? upcoming[0].wasteType : 'Create a request to plan a collection'}</p></div></article>
        <article className="calendar-summary-card summary-amber"><span><FiCheckCircle /></span><div><small>PLANNED WASTE</small><b>{plannedWeight.toFixed(1)} kg</b><p>For active requests this month</p></div></article>
      </div>

      <div className="calendar-layout">
        <section className="calendar-panel" aria-label={`${title} collection calendar`}>
          <div className="calendar-toolbar">
            <div><span className="eyebrow">MONTHLY SCHEDULE</span><h3>{title}</h3></div>
            <div className="calendar-controls">
              <button className="calendar-nav-button" aria-label="Previous month" onClick={() => changeMonth(-1)}><FiArrowLeft /></button>
              <button className="calendar-today-button" onClick={() => { const now = new Date(); setMonth(new Date(now.getFullYear(), now.getMonth(), 1)); setSelectedDate(localDateKey(now)); }}>Today</button>
              <button className="calendar-nav-button" aria-label="Next month" onClick={() => changeMonth(1)}><FiArrowRight /></button>
            </div>
          </div>
          <div className="calendar-weekdays" aria-hidden="true">{weekdays.map(day => <span key={day}>{day}</span>)}</div>
          <div className={`calendar-days ${loading ? 'is-loading' : ''}`} aria-live="polite">
            {cells.map((day, index) => {
              if (day === null) return <span className="calendar-day calendar-day-empty" key={`empty-${index}`} aria-hidden="true" />;
              const dateKey = `${key}-${String(day).padStart(2, '0')}`;
              const events = pickupsByDate[dateKey] || [];
              const isToday = dateKey === today;
              const isSelected = dateKey === selectedDate;
              return (
                <button
                  type="button"
                  key={dateKey}
                  className={`calendar-day ${isToday ? 'is-today' : ''} ${isSelected ? 'is-selected' : ''} ${events.length ? 'has-events' : ''}`}
                  onClick={() => selectDay(day)}
                  aria-pressed={isSelected}
                  aria-label={`${displayDate(dateKey)}${events.length ? `, ${events.length} ${events.length === 1 ? 'pickup' : 'pickups'}` : ', no pickups'}`}
                >
                  <span className="calendar-day-number">{day}</span>
                  {events.length > 0 && <span className="calendar-event-indicators"><span className={`event-dot ${statusInfo[events[0].status]?.tone || 'pending'}`} />{events.length > 1 && <small>+{events.length - 1}</small>}</span>}
                </button>
              );
            })}
          </div>
          <div className="calendar-legend"><span><i className="event-dot pending" /> Awaiting review</span><span><i className="event-dot assigned" /> Scheduled / assigned</span><span><i className="event-dot collected" /> Collected</span></div>
        </section>

        <aside className="calendar-side-panel">
          <section className="calendar-selected-day">
            <div className="eyebrow">SELECTED DAY</div>
            <h3>{displayDate(selectedDate)}</h3>
            {selectedPickups.length ? <div className="calendar-event-list">{selectedPickups.map(pickup => {
              const status = statusInfo[pickup.status] || statusInfo.PENDING;
              return <article className="calendar-event-card" key={pickup.id}><div className="calendar-event-card-head"><span className="calendar-event-time">{pickup.pickupTime?.slice(0, 5)}</span><span className={`calendar-status ${status.tone}`}>{status.icon}{status.label}</span></div><h4>{pickup.wasteType}</h4><p>{pickup.quantity} kg · {pickup.pickupAddress}</p><Link to="/app/requests">View request <FiArrowUpRight /></Link></article>;
            })}</div> : <div className="calendar-empty"><span><FiCalendar /></span><b>No pickups on this day</b><small>Choose another date or schedule a new collection.</small><Link to="/app/requests/new" className="btn btn-outline-echo">Request a pickup <FiArrowUpRight /></Link></div>}
          </section>
          <section className="calendar-upcoming">
            <div className="calendar-upcoming-heading"><div><div className="eyebrow">COMING UP</div><h3>Next collections</h3></div><span>{upcoming.length}</span></div>
            {upcoming.length ? upcoming.map(pickup => <button className="upcoming-row" type="button" key={pickup.id} onClick={() => { setSelectedDate(pickup.pickupDate); if (!pickup.pickupDate.startsWith(key)) setMonth(new Date(`${pickup.pickupDate}T12:00:00`)); }}><span className="upcoming-date"><b>{new Date(`${pickup.pickupDate}T12:00:00`).getDate()}</b><small>{new Date(`${pickup.pickupDate}T12:00:00`).toLocaleDateString(undefined, { month: 'short' })}</small></span><span className="upcoming-details"><b>{pickup.wasteType}</b><small>{pickup.pickupTime?.slice(0, 5)} · {pickup.quantity} kg</small></span><span className={`event-dot ${statusInfo[pickup.status]?.tone || 'pending'}`} /></button>) : <p className="upcoming-empty">No upcoming collections. You can request one whenever you need it.</p>}
          </section>
        </aside>
      </div>
    </div>
  );
}
