import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  FiArrowDown,
  FiArrowUpRight,
  FiAlertCircle,
  FiCheck,
  FiCheckCircle,
  FiClipboard,
  FiCalendar,
  FiFeather,
  FiHome,
  FiMapPin,
  FiMenu,
  FiSearch,
  FiShield,
  FiTruck,
  FiX,
} from 'react-icons/fi';
import { useAuth } from '../context/AuthContext';

const portals = [
  {
    id: 'citizen',
    role: 'CITIZEN',
    title: 'Citizen portal',
    tag: 'FOR RESIDENTS',
    description: 'Request a pickup and stay informed from booking through collection.',
    actions: ['Book a waste collection', 'Track request updates', 'Report a service issue'],
    Icon: FiHome,
  },
  {
    id: 'collector',
    role: 'COLLECTOR',
    title: 'Collector portal',
    tag: 'FOR FIELD TEAMS',
    description: 'Keep daily collection work organized and residents up to date.',
    actions: ['See assigned pickups', 'Review collection details', 'Update job progress'],
    Icon: FiTruck,
  },
  {
    id: 'admin',
    role: 'ADMIN',
    title: 'Admin portal',
    tag: 'FOR SERVICE TEAMS',
    description: 'Coordinate requests and help the community get reliable service.',
    actions: ['Review incoming requests', 'Manage collector accounts', 'Resolve community issues'],
    Icon: FiShield,
  },
];

export default function Home() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [guideQuery, setGuideQuery] = useState('');
  const guideLink = user ? '/app/guide' : '/guide';

  function portalAction(portal) {
    if (!user) {
      navigate(`/login?portal=${portal.id}`);
      return;
    }
    if (user.role === portal.role) {
      navigate('/app');
      return;
    }
    logout();
    navigate(`/login?portal=${portal.id}`);
  }

  function openCitizenAction(path) {
    if (user?.role === 'CITIZEN') {
      navigate(path);
      return;
    }
    if (user) logout();
    navigate(`/login?portal=citizen&next=${encodeURIComponent(path)}`);
  }

  function searchGuide(event) {
    event.preventDefault();
    const query = guideQuery.trim();
    navigate(`${guideLink}${query ? `?q=${encodeURIComponent(query)}` : ''}`);
  }

  function closeMenu() {
    setMenuOpen(false);
  }

  return (
    <div className="public-page">
      <header className="public-header">
        <nav className="public-nav container" aria-label="Main navigation">
          <Link className="echo-brand" to="/" aria-label="Echo home" onClick={closeMenu}>
            <span className="brand-mark">e</span> echo<span className="brand-dot">.</span>
          </Link>
          <button
            className="public-nav-toggle"
            type="button"
            aria-label={menuOpen ? 'Close navigation menu' : 'Open navigation menu'}
            aria-expanded={menuOpen}
            aria-controls="public-navigation"
            onClick={() => setMenuOpen(open => !open)}
          >
            {menuOpen ? <FiX aria-hidden="true" /> : <FiMenu aria-hidden="true" />}
          </button>
          <div id="public-navigation" className={`public-nav-menu ${menuOpen ? 'is-open' : ''}`}>
            <div className="public-nav-primary">
              <a className="nav-link" href="#portals" onClick={closeMenu}>Portals</a>
              <a className="nav-link" href="#how-it-works" onClick={closeMenu}>How it works</a>
              <Link className="nav-link" to={guideLink} onClick={closeMenu}>Waste guide</Link>
              <a className="nav-link" href="#about" onClick={closeMenu}>About Echo</a>
            </div>
            <div className="public-nav-account">
              {user ? (
                <Link className="btn btn-echo" to="/app" onClick={closeMenu}>Open workspace <FiArrowUpRight aria-hidden="true" /></Link>
              ) : (
                <>
                  <Link className="nav-link" to="/login" onClick={closeMenu}>Sign in</Link>
                  <Link className="btn btn-echo" to="/register" onClick={closeMenu}>Create account <FiArrowUpRight aria-hidden="true" /></Link>
                </>
              )}
            </div>
          </div>
        </nav>
      </header>

      <main>
        <section className="portal-home-hero container">
          <div className="portal-home-copy">
            <div className="eyebrow"><span className="live-dot" /> WASTE SERVICES, ONE CONNECTED PLACE</div>
            <h1>The right workspace for <em>everyone.</em></h1>
            <p>Residents, collection teams, and service administrators each have a clear place to get things done.</p>
            <a className="portal-home-jump" href="#portals">Choose your portal <FiArrowDown aria-hidden="true" /></a>
          </div>
          <div className="portal-home-aside" aria-label="One service with three connected teams">
            <div className="portal-orbit"><span><FiHome /></span><span><FiTruck /></span><span><FiShield /></span><i><FiFeather /></i></div>
            <div><b>One community.</b><small>Three teams moving service forward.</small></div>
          </div>
        </section>

        <section id="portals" className="portal-section container">
          <div className="section-heading portal-heading">
            <div><div className="eyebrow">YOUR ROLE, YOUR WORKSPACE</div><h2>Choose an Echo portal.</h2></div>
            <p>Sign in with your account. Echo uses your assigned role to show the right tools and service information.</p>
          </div>
          <div className="portal-grid">
            {portals.map(({ id, role, title, tag, description, actions, Icon }) => {
              const activePortal = user?.role === role;
              return (
                <article className={`portal-card portal-card-${id}`} key={id}>
                  <div className="portal-card-top"><span className="portal-icon"><Icon aria-hidden="true" /></span><span className="portal-tag">{tag}</span></div>
                  <h3>{title}</h3>
                  <p>{description}</p>
                  <ul>{actions.map(action => <li key={action}><FiCheck aria-hidden="true" />{action}</li>)}</ul>
                  <div className="portal-card-actions">
                    <button className="btn btn-echo" type="button" onClick={() => portalAction({ id, role })}>
                      {user ? (activePortal ? 'Open your workspace' : `Switch to ${id} sign in`) : `Sign in to ${id} portal`}
                      <FiArrowUpRight aria-hidden="true" />
                    </button>
                    {id === 'citizen' && !user && <Link className="portal-register-link" to="/register">New resident? Create an account</Link>}
                    {user && activePortal && <span className="current-portal-note"><FiCheckCircle aria-hidden="true" /> You’re signed in here</span>}
                  </div>
                </article>
              );
            })}
          </div>
          <p className="portal-security-note"><FiShield aria-hidden="true" /> Admin and collector access is managed by the service team.</p>
        </section>

        <section className="citizen-services-section">
          <div className="container">
            <div className="section-heading citizen-services-heading">
              <div><div className="eyebrow">QUICK HELP FOR RESIDENTS</div><h2>Everyday services, made simple.</h2></div>
              <p>Get started with the three things residents come here to do most.</p>
            </div>
            <div className="citizen-services-grid">
              <article className="citizen-service-card service-booking">
                <span className="citizen-service-icon"><FiCalendar aria-hidden="true" /></span>
                <span className="citizen-service-label">COLLECTIONS</span>
                <h3>Book a pickup</h3>
                <p>Choose your waste type, location, date, and preferred collection time.</p>
                <button type="button" className="service-action" onClick={() => openCitizenAction('/app/requests/new')}>
                  {user?.role === 'CITIZEN' ? 'Start a request' : 'Sign in to book'} <FiArrowUpRight aria-hidden="true" />
                </button>
              </article>
              <article className="citizen-service-card service-guide">
                <span className="citizen-service-icon"><FiSearch aria-hidden="true" /></span>
                <span className="citizen-service-label">SORTING GUIDE</span>
                <h3>Find where it goes</h3>
                <p>Search common items for a practical recycling, reuse, or disposal starting point.</p>
                <form className="home-guide-search" onSubmit={searchGuide}>
                  <label className="visually-hidden" htmlFor="home-guide-query">Search waste materials</label>
                  <input id="home-guide-query" value={guideQuery} onChange={event => setGuideQuery(event.target.value)} placeholder="Try batteries or cardboard" />
                  <button type="submit" aria-label="Search the waste guide"><FiArrowUpRight aria-hidden="true" /></button>
                </form>
                <div className="guide-suggestions"><span>Popular:</span>{['Batteries', 'Cardboard', 'Food scraps'].map(item => <button type="button" key={item} onClick={() => setGuideQuery(item)}>{item}</button>)}</div>
              </article>
              <article className="citizen-service-card service-report">
                <span className="citizen-service-icon"><FiAlertCircle aria-hidden="true" /></span>
                <span className="citizen-service-label">RESIDENT SUPPORT</span>
                <h3>Report a problem</h3>
                <p>Tell the service team about a missed pickup, damaged bin, or other issue.</p>
                <button type="button" className="service-action" onClick={() => openCitizenAction('/app/issues')}>
                  {user?.role === 'CITIZEN' ? 'Report an issue' : 'Sign in to get help'} <FiArrowUpRight aria-hidden="true" />
                </button>
              </article>
            </div>
            {!user && <p className="citizen-account-note">New to Echo? <Link to="/register?next=%2Fapp%2Frequests%2Fnew">Create a free resident account</Link> to book collections and track service updates.</p>}
          </div>
        </section>

        <section className="home-benefits-wrap" aria-label="Echo service benefits">
          <div className="container home-benefits">
            <div className="home-benefit"><span><FiCalendar /></span><div><b>Book a pickup</b><small>Choose a time that works.</small></div></div>
            <div className="home-benefit"><span><FiMapPin /></span><div><b>Follow progress</b><small>Know what happens next.</small></div></div>
            <div className="home-benefit"><span><FiClipboard /></span><div><b>Get support</b><small>Report a service issue.</small></div></div>
            <div className="home-benefit"><span><FiFeather /></span><div><b>Sort with confidence</b><small>Find the right disposal guide.</small></div></div>
          </div>
        </section>

        <section id="how-it-works" className="how-section">
          <div className="container">
            <div className="section-heading how-heading">
              <div><div className="eyebrow">A SIMPLE SERVICE JOURNEY</div><h2>From request to done.</h2></div>
              <p>Echo gives residents and service teams one shared view of the collection process.</p>
            </div>
            <div className="how-grid">
              <article className="how-card"><span className="how-number">01</span><span className="how-icon"><FiClipboard /></span><h3>Send a request</h3><p>Choose your waste type, add collection details, and pick a convenient time.</p></article>
              <article className="how-card"><span className="how-number">02</span><span className="how-icon"><FiTruck /></span><h3>Teams coordinate</h3><p>Service teams review the request and assign it to a collection team.</p></article>
              <article className="how-card"><span className="how-number">03</span><span className="how-icon"><FiCheckCircle /></span><h3>Track the outcome</h3><p>Follow status updates and let the team know if you need support.</p></article>
            </div>
          </div>
        </section>

        <section id="about" className="container about-band">
          <div className="about-mark"><FiFeather aria-hidden="true" /></div>
          <div><div className="eyebrow">BETTER SERVICE, LESS GUESSWORK</div><h2>Good waste habits start with clear information.</h2><p>Explore practical sorting tips, then use your portal to take the next step.</p></div>
          <Link to={guideLink} className="btn btn-outline-echo">Open waste guide <FiArrowUpRight aria-hidden="true" /></Link>
        </section>
      </main>

      <footer className="container public-footer">
        <Link className="echo-brand" to="/"><span className="brand-mark">e</span> echo<span className="brand-dot">.</span></Link>
        <span>Cleaner collections. Stronger communities.</span>
        <span>© 2026 Echo Waste Management</span>
      </footer>
    </div>
  );
}
