import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const redirectTo = location.state?.from ?? '/';

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-sm mx-auto px-6 py-20">
      <h1 className="font-display text-4xl tracking-marquee text-center mb-1">
        WELCOME BACK
      </h1>
      <p className="text-center text-paper-dim text-sm mb-10">
        Sign in to reserve your seat.
      </p>

      <form onSubmit={handleSubmit} className="space-y-5">
        <Field label="Email" type="email" value={email} onChange={setEmail} autoFocus />
        <Field label="Password" type="password" value={password} onChange={setPassword} />

        {error && (
          <p className="text-sm text-velvet-bright font-mono">{error}</p>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full py-3 rounded bg-marquee text-ink font-semibold tracking-wide hover:bg-marquee-bright transition-colors disabled:opacity-50"
        >
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p className="text-center text-sm text-paper-faint mt-8">
        No account yet?{' '}
        <Link to="/register" className="text-marquee hover:underline">
          Register
        </Link>
      </p>
    </div>
  );
}

function Field({ label, type, value, onChange, autoFocus }) {
  return (
    <label className="block">
      <span className="block text-xs font-mono uppercase tracking-wide text-paper-faint mb-1.5">
        {label}
      </span>
      <input
        type={type}
        required
        autoFocus={autoFocus}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full bg-ink-soft border border-ink-line rounded px-3 py-2.5 text-paper placeholder:text-paper-faint focus:border-marquee outline-none transition-colors"
      />
    </label>
  );
}
