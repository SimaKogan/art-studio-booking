import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';
import Assistant from './Assistant';

function Logo() {
  return (
    <Link to="/" className="logo" aria-label="Atelier Nahal, home">
      <svg viewBox="0 0 32 32" width="30" height="30" aria-hidden="true">
        <circle cx="12" cy="12" r="9" fill="#2F43D8" />
        <circle cx="21" cy="13" r="8" fill="#F4C20D" fillOpacity=".9" />
        <circle cx="16" cy="21" r="8" fill="#E0486F" fillOpacity=".85" />
      </svg>
      <span>Atelier Nahal</span>
    </Link>
  );
}

export default function Layout() {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  return (
    <>
      <a className="skip" href="#main">Skip to content</a>
      <header className="nav">
        <div className="wrap nav-in">
          <Logo />
          <nav aria-label="Main">
            <NavLink to="/classes">Classes</NavLink>
            {user && <NavLink to="/bookings">My bookings</NavLink>}
            {user?.role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
          </nav>
          <div className="nav-user">
            {user ? (
              <>
                <span className="nav-name">{user.fullName}</span>
                <button className="btn btn-quiet" onClick={() => { logout(); nav('/'); }}>Log out</button>
              </>
            ) : (
              <>
                <Link className="btn btn-quiet" to="/login">Log in</Link>
                <Link className="btn btn-solid" to="/register">Create account</Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main id="main"><Outlet /></main>
      <footer className="foot">
        <div className="wrap foot-in">
          <Logo />
          <p>7 Indigo Lane, Rivertown. Small groups of up to 14, all materials included.</p>
          <p className="foot-note">Demo project: payments are simulated and no card is ever charged.</p>
        </div>
      </footer>
      <Assistant />
    </>
  );
}
