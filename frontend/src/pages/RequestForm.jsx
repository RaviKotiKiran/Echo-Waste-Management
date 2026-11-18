import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { FiArrowLeft, FiArrowUpRight } from 'react-icons/fi';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const wasteTypes = [
  'Household Waste',
  'Plastic Waste',
  'Paper Waste',
  'E-Waste',
  'Garden Waste',
  'Metal Waste',
  'Organic Waste',
  'Construction Waste',
];

const initialForm = {
  wasteType: wasteTypes[0],
  quantity: '1',
  pickupAddress: '',
  pickupDate: '',
  pickupTime: '09:00',
  instructions: '',
};

function localToday() {
  const date = new Date();
  const offset = date.getTimezoneOffset() * 60_000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 10);
}

export default function RequestForm() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  function updateField(event) {
    const { name, value } = event.target;
    setForm(current => ({ ...current, [name]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError('');
    setBusy(true);

    try {
      await api.post('/requests', { ...form, quantity: Number(form.quantity) });
      navigate('/app/requests', {
        state: { notice: 'Your pickup request was submitted.' },
      });
    } catch (requestError) {
      setError(
        requestError.response?.data?.message ||
          'Could not submit request. Please try again.',
      );
    } finally {
      setBusy(false);
    }
  }

  if (user.role !== 'CITIZEN') return <Navigate to="/app" replace />;

  return (
    <div className="form-page">
      <div className="page-heading">
        <div>
          <Link to="/app" className="back-link">
            <FiArrowLeft aria-hidden="true" /> Dashboard
          </Link>
          <div className="eyebrow mt-4">WASTE COLLECTION</div>
          <h2>Request a pickup</h2>
          <p>Tell the collection team what to pick up and when.</p>
        </div>
      </div>

      <form className="panel edit-form" onSubmit={submit}>
        {error && <div className="alert alert-danger" role="alert">{error}</div>}

        <div className="row g-3">
          <div className="col-md-6">
            <label className="form-label">
              Waste category
              <select
                className="form-select"
                name="wasteType"
                required
                value={form.wasteType}
                onChange={updateField}
              >
                {wasteTypes.map(type => <option key={type}>{type}</option>)}
              </select>
            </label>
          </div>

          <div className="col-md-6">
            <label className="form-label">
              Approximate quantity (kg)
              <input
                className="form-control"
                name="quantity"
                required
                type="number"
                min="0.01"
                max="99999999"
                step="0.01"
                value={form.quantity}
                onChange={updateField}
              />
            </label>
          </div>

          <div className="col-12">
            <label className="form-label">
              Pickup address
              <input
                className="form-control"
                name="pickupAddress"
                required
                maxLength="500"
                placeholder="Street, neighbourhood, city"
                value={form.pickupAddress}
                onChange={updateField}
              />
            </label>
          </div>

          <div className="col-md-6">
            <label className="form-label">
              Preferred date
              <input
                className="form-control"
                name="pickupDate"
                required
                type="date"
                min={localToday()}
                value={form.pickupDate}
                onChange={updateField}
              />
            </label>
          </div>

          <div className="col-md-6">
            <label className="form-label">
              Preferred time
              <input
                className="form-control"
                name="pickupTime"
                required
                type="time"
                value={form.pickupTime}
                onChange={updateField}
              />
            </label>
          </div>

          <div className="col-12">
            <label className="form-label">
              Additional instructions <span className="optional-label">OPTIONAL</span>
              <textarea
                className="form-control"
                name="instructions"
                rows="3"
                maxLength="1000"
                placeholder="Gate details, access instructions, or other notes"
                value={form.instructions}
                onChange={updateField}
              />
            </label>
          </div>
        </div>

        <div className="form-actions">
          <Link className="btn btn-outline-echo" to="/app">Cancel</Link>
          <button className="btn btn-echo" type="submit" disabled={busy} aria-busy={busy}>
            {busy ? 'Submitting…' : 'Submit pickup request'}
            <FiArrowUpRight aria-hidden="true" />
          </button>
        </div>
      </form>
    </div>
  );
}
