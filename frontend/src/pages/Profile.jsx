import { useState } from 'react';
import { FiSave } from 'react-icons/fi';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Profile() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({
    fullName: user.fullName || '',
    phone: user.phone || '',
    address: user.address || '',
  });
  const [error, setError] = useState('');
  const [saved, setSaved] = useState(false);
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setError('');
    setSaved(false);
    setBusy(true);

    try {
      const { data } = await api.put(`/users/${user.id}`, form);
      updateUser(data);
      setForm({ fullName: data.fullName, phone: data.phone || '', address: data.address || '' });
      setSaved(true);
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Could not save your profile. Please try again.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="form-page">
      <div className="page-heading">
        <div className="eyebrow">YOUR ACCOUNT</div>
        <h2>Profile settings</h2>
        <p>Keep your contact details up to date.</p>
      </div>
      <form className="panel edit-form" onSubmit={submit}>
        {error && <div className="alert alert-danger">{error}</div>}
        {saved && <div className="alert alert-success">Your profile was saved.</div>}
        <div className="row g-3">
          <div className="col-md-6">
            <label className="form-label">Full name
              <input className="form-control" required maxLength="120" value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} />
            </label>
          </div>
          <div className="col-md-6">
            <label className="form-label">Email address
              <input className="form-control" type="email" value={user.email} disabled />
            </label>
          </div>
          <div className="col-md-6">
            <label className="form-label">Phone
              <input className="form-control" maxLength="30" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} />
            </label>
          </div>
          <div className="col-12">
            <label className="form-label">Address
              <input className="form-control" maxLength="500" value={form.address} onChange={(event) => setForm({ ...form, address: event.target.value })} />
            </label>
          </div>
        </div>
        <div className="form-actions">
          <button className="btn btn-echo" disabled={busy}>
            {busy ? 'Saving…' : 'Save changes'} {!busy && <FiSave />}
          </button>
        </div>
      </form>
    </div>
  );
}
