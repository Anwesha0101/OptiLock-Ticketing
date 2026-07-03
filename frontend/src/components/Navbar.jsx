import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();

  return (
    <header className="border-b border-ink-line bg-ink/95 backdrop-blur sticky top-0 z-20">
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <Link
          to="/"
          className="font-display text-3xl tracking-marquee text-marquee leading-none"
        >
          OPTILOCK
        </Link>

        <nav className="flex items-center gap-6 font-mono text-sm text-paper-dim">
          {user ? (
            <>
              <span className="hidden sm:inline">{user.email}</span>
              <button
                onClick={logout}
                className="px-3 py-1.5 rounded border border-ink-line hover:border-marquee hover:text-marquee transition-colors"
              >
                Sign out
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="px-3 py-1.5 rounded border border-ink-line hover:border-marquee hover:text-marquee transition-colors"
            >
              Sign in
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}
