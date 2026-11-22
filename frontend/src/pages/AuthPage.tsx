import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { errorMessage } from '../api';
import { useAuth } from '../auth';

export default function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const { login, register } = useAuth();
  const nav = useNavigate();
  const from = (useLocation().state as { from?: string } | null)?.from ?? '/';
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const isLogin = mode === 'login';

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const u = isLogin ? await login(email, password) : await register(name, email, password);
      nav(u.role === 'ADMIN' && from === '/' ? '/admin' : from, { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="wrap page narrow">
      <h1>{isLogin ? 'Log in' : 'Create your account'}</h1>
      <form className="form" onSubmit={submit}>
        {!isLogin && (
          <label>Full name
            <input required maxLength={80} value={name} onChange={(e) => setName(e.target.value)} autoComplete="name" />
          </label>
        )}
        <label>Email
          <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
        </label>
        <label>Password
          <input type="password" required minLength={isLogin ? 1 : 8} value={password} onChange={(e) => setPassword(e.target.value)}
            autoComplete={isLogin ? 'current-password' : 'new-password'} />
          {!isLogin && <span className="hint">At least 8 characters.</span>}
        </label>
        {error && <p className="alert" role="alert">{error}</p>}
        <button className="btn btn-solid btn-wide" disabled={busy}>{busy ? 'One moment…' : isLogin ? 'Log in' : 'Create account'}</button>
      </form>

      {isLogin && (
        <div className="demo-box">
          <p><b>Try the demo</b></p>
          <div className="chips">
            <button onClick={() => { setEmail('demo@atelier.local'); setPassword('Demo12345!'); }}>Student account</button>
            <button onClick={() => { setEmail('admin@atelier.local'); setPassword('Admin123!'); }}>Admin account</button>
          </div>
        </div>
      )}
      <p className="muted">
        {isLogin ? <>New here? <Link to="/register">Create an account</Link></> : <>Already registered? <Link to="/login">Log in</Link></>}
      </p>
    </div>
  );
}
