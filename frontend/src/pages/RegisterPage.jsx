import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await register(email, password);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-sm mx-auto px-6 py-20">
      <h1 className="font-display text-4xl tracking-marquee text-center mb-1">
        CREATE ACCOUNT
      </h1>
      <p className="text-center text-paper-dim text-sm mb-10">
        Takes about ten seconds.
      </p>

      <form onSubmit={handleSubmit} className="space-y-5">
        <label className="block">
          <span className="block text-xs font-mono uppercase tracking-wide text-paper-faint mb-1.5">
            Email
          </span>
          <input
            type="email"
            required
            autoFocus
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full bg-ink-soft border border-ink-line rounded px-3 py-2.5 text-paper focus:border-marquee outline-none transition-colors"
          />
        </label>

        <label className="block">
          <span className="block text-xs font-mono uppercase tracking-wide text-paper-faint mb-1.5">
            Password
          </span>
          <input
            type="password"
            required
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full bg-ink-soft border border-ink-line rounded px-3 py-2.5 text-paper focus:border-marquee outline-none transition-colors"
          />
          <span className="block text-xs text-paper-faint mt-1.5">At least 8 characters.</span>
        </label>

        {error && <p className="text-sm text-velvet-bright font-mono">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full py-3 rounded bg-marquee text-ink font-semibold tracking-wide hover:bg-marquee-bright transition-colors disabled:opacity-50"
        >
          {loading ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <p className="text-center text-sm text-paper-faint mt-8">
        Already have an account?{' '}
        <Link to="/login" className="text-marquee hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}
