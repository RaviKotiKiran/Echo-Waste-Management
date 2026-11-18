import { useState } from 'react';
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { FiArrowLeft, FiArrowUpRight, FiEye, FiEyeOff } from 'react-icons/fi';
import { useAuth } from '../context/AuthContext';
import { apiErrorMessage } from '../services/api';

const emptyForm = { fullName: '', email: '', password: '', confirmPassword: '', phone: '', address: '' };

export default function Register() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState(emptyForm);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/app" replace />;

  async function submit(event) {
    event.preventDefault();
    setError('');
    if (form.password !== form.confirmPassword) {
      setError('The passwords do not match. Check both fields and try again.');
      return;
    }
    setBusy(true);
    try {
      const { confirmPassword, ...account } = form;
      await register({ ...account, fullName: account.fullName.trim(), email: account.email.trim().toLowerCase(), phone: account.phone.trim(), address: account.address.trim() });
      const requestedPath = searchParams.get('next');
      navigate(requestedPath?.startsWith('/app/') ? requestedPath : '/app', { replace: true });
    } catch (requestError) {
      setError(apiErrorMessage(requestError));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-screen register-screen">
      <Link className="back-home" to="/"><FiArrowLeft aria-hidden="true" /> Back to Echo</Link>
      <section className="auth-card register-card" aria-labelledby="register-title">
        <Link className="echo-brand" to="/" aria-label="Echo home"><span className="brand-mark">e</span> echo<span className="brand-dot">.</span></Link>
        <p className="eyebrow mt-4">JOIN YOUR COMMUNITY</p>
        <h1 id="register-title">Create your Echo account.</h1>
        <p className="muted-copy">Set up your resident account to book and follow pickups.</p>
        {error && <div className="alert alert-danger auth-error" role="alert">{error}</div>}
        <form onSubmit={submit} noValidate>
          <label className="form-label" htmlFor="register-name">Full name</label>
          <input id="register-name" className="form-control" required maxLength="120" autoComplete="name" placeholder="Your name" value={form.fullName} onChange={event => setForm({ ...form, fullName: event.target.value })} />
          <label className="form-label mt-3" htmlFor="register-email">Email address</label>
          <input id="register-email" className="form-control" required type="email" autoComplete="email" inputMode="email" autoCapitalize="none" spellCheck="false" placeholder="you@example.com" value={form.email} onChange={event => setForm({ ...form, email: event.target.value })} />
          <div className="row g-3 mt-0">
            <div className="col-sm-6">
              <label className="form-label mt-3" htmlFor="register-phone">Phone <span className="optional-label">OPTIONAL</span></label>
              <input id="register-phone" className="form-control" maxLength="30" autoComplete="tel" inputMode="tel" placeholder="Phone number" value={form.phone} onChange={event => setForm({ ...form, phone: event.target.value })} />
            </div>
            <div className="col-sm-6">
              <label className="form-label mt-3" htmlFor="register-address">Address <span className="optional-label">OPTIONAL</span></label>
              <input id="register-address" className="form-control" maxLength="500" autoComplete="street-address" placeholder="Street or area" value={form.address} onChange={event => setForm({ ...form, address: event.target.value })} />
            </div>
          </div>
          <label className="form-label mt-3" htmlFor="register-password">Password</label>
          <div className="password-field">
            <input id="register-password" className="form-control" required type={showPassword ? 'text' : 'password'} minLength="8" maxLength="72" autoComplete="new-password" placeholder="At least 8 characters" value={form.password} onChange={event => setForm({ ...form, password: event.target.value })} aria-describedby="password-help" />
            <button className="password-toggle" type="button" onClick={() => setShowPassword(value => !value)} aria-label={showPassword ? 'Hide password' : 'Show password'} aria-pressed={showPassword}>{showPassword ? <FiEyeOff /> : <FiEye />}</button>
          </div>
          <small id="password-help" className="form-hint">Use 8–72 characters. Passwords are stored securely.</small>
          <label className="form-label mt-3" htmlFor="register-confirm">Confirm password</label>
          <div className="password-field">
            <input id="register-confirm" className="form-control" required type={showConfirm ? 'text' : 'password'} autoComplete="new-password" placeholder="Type the password again" value={form.confirmPassword} onChange={event => setForm({ ...form, confirmPassword: event.target.value })} />
            <button className="password-toggle" type="button" onClick={() => setShowConfirm(value => !value)} aria-label={showConfirm ? 'Hide confirmation' : 'Show confirmation'} aria-pressed={showConfirm}>{showConfirm ? <FiEyeOff /> : <FiEye />}</button>
          </div>
          <button className="btn btn-echo w-100 mt-4 auth-submit" disabled={busy || !form.fullName.trim() || !form.email.trim() || !form.password || !form.confirmPassword}>
            {busy ? <><span className="spinner-border spinner-border-sm" aria-hidden="true" /> Creating account…</> : <>Create account <FiArrowUpRight aria-hidden="true" /></>}
          </button>
        </form>
        <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
      </section>
    </div>
  );
}
