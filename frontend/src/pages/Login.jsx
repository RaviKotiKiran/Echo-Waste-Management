import { useState } from 'react';
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { FiArrowLeft, FiArrowUpRight, FiEye, FiEyeOff } from 'react-icons/fi';
import { useAuth } from '../context/AuthContext';
import { apiErrorMessage } from '../services/api';

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const requestedPath = searchParams.get('next');
  const portalLabel = {
    citizen: 'citizen portal',
    collector: 'collector portal',
    admin: 'admin portal',
  }[searchParams.get('portal')];
  const [form, setForm] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/app" replace />;

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      await login({ email: form.email.trim(), password: form.password });
      const destination = requestedPath?.startsWith('/app/') ? requestedPath : '/app';
      navigate(destination, { replace: true });
    } catch (requestError) {
      setError(apiErrorMessage(requestError));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-screen">
      <Link className="back-home" to="/"><FiArrowLeft aria-hidden="true" /> Back to Echo</Link>
      <section className="auth-card" aria-labelledby="login-title">
        <Link className="echo-brand" to="/" aria-label="Echo home"><span className="brand-mark">e</span> echo<span className="brand-dot">.</span></Link>
        <p className="eyebrow mt-4">{portalLabel ? `WELCOME BACK · ${portalLabel.toUpperCase()}` : 'WELCOME BACK'}</p>
        <h1 id="login-title">{portalLabel ? `Sign in to the ${portalLabel}.` : <>Sign in to your<br className="d-none d-sm-block" /> Echo account.</>}</h1>
        <p className="muted-copy">{portalLabel ? 'Use the account for your role. Echo will open the workspace your account is allowed to access.' : 'Access your pickups and stay in the loop.'}</p>
        {error && <div className="alert alert-danger auth-error" role="alert">{error}</div>}
        <form onSubmit={submit} noValidate>
          <label className="form-label" htmlFor="login-email">Email address</label>
          <input id="login-email" className="form-control" required type="email" autoComplete="email" inputMode="email" autoCapitalize="none" spellCheck="false" placeholder="you@example.com" value={form.email} onChange={event => setForm({ ...form, email: event.target.value })} />
          <label className="form-label mt-3" htmlFor="login-password">Password</label>
          <div className="password-field">
            <input id="login-password" className="form-control" required type={showPassword ? 'text' : 'password'} autoComplete="current-password" placeholder="Enter your password" value={form.password} onChange={event => setForm({ ...form, password: event.target.value })} />
            <button className="password-toggle" type="button" onClick={() => setShowPassword(value => !value)} aria-label={showPassword ? 'Hide password' : 'Show password'} aria-pressed={showPassword}>{showPassword ? <FiEyeOff /> : <FiEye />}</button>
          </div>
          <button className="btn btn-echo w-100 mt-4 auth-submit" disabled={busy || !form.email.trim() || !form.password}>
            {busy ? <><span className="spinner-border spinner-border-sm" aria-hidden="true" /> Signing in…</> : <>Sign in <FiArrowUpRight aria-hidden="true" /></>}
          </button>
        </form>
        <p className="auth-switch">New to Echo? <Link to="/register">Create an account</Link></p>
      </section>
    </div>
  );
}
