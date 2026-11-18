import { useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { FiArrowLeft, FiBookOpen, FiCheckCircle, FiSearch, FiShield, FiTrash2 } from 'react-icons/fi';
import { useAuth } from '../context/AuthContext';

const materials = [
  { item: 'Plastic bottles', category: 'Recycling', bin: 'Recycling', icon: '♻️', detail: 'Empty and rinse the bottle. Check local rules for caps and labels before placing it in a recycling bin.' },
  { item: 'Cardboard boxes', category: 'Recycling', bin: 'Paper & cardboard', icon: '📦', detail: 'Keep cardboard clean and dry, flatten boxes, and remove plastic wrap or foam.' },
  { item: 'Glass bottles and jars', category: 'Recycling', bin: 'Glass recycling', icon: '🫙', detail: 'Empty and rinse containers. Broken glass often needs special handling; follow your local collection guidance.' },
  { item: 'Food scraps', category: 'Organic', bin: 'Food / organics', icon: '🥬', detail: 'Use a community compost or organics collection if available. Avoid putting food scraps in recycling.' },
  { item: 'Garden leaves', category: 'Organic', bin: 'Garden waste', icon: '🍂', detail: 'Bundle or bag garden material according to your local service. Do not include soil, stones, or treated wood.' },
  { item: 'Batteries', category: 'Special handling', bin: 'Battery drop-off', icon: '🔋', detail: 'Do not put loose batteries in household recycling. Use a designated battery collection point or hazardous-waste service.' },
  { item: 'Phones and electronics', category: 'Special handling', bin: 'E-waste drop-off', icon: '📱', detail: 'Take electronics to an approved e-waste collection point. Remove personal data before handing in a device.' },
  { item: 'Paint and chemicals', category: 'Special handling', bin: 'Hazardous waste', icon: '🧪', detail: 'Keep products in their original containers and use a local hazardous-waste facility. Never pour chemicals down a drain.' },
  { item: 'Greasy pizza boxes', category: 'General waste', bin: 'Check local organics rules', icon: '🍕', detail: 'Grease can contaminate paper recycling. Separate clean cardboard; check whether your local organics program accepts the greasy part.' },
  { item: 'Clothing and textiles', category: 'Reuse', bin: 'Donate or textile drop-off', icon: '👕', detail: 'Donate clean wearable items or use a textile collection point. Keep textiles out of paper and container recycling.' },
];
const filters = ['All items', 'Recycling', 'Organic', 'Special handling', 'General waste', 'Reuse'];

export default function WasteGuide() {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const [search, setSearch] = useState(() => searchParams.get('q') || '');
  const [filter, setFilter] = useState('All items');
  const results = useMemo(() => materials.filter((material) => {
    const matchesFilter = filter === 'All items' || material.category === filter;
    const query = search.trim().toLowerCase();
    return matchesFilter && (!query || `${material.item} ${material.category} ${material.detail}`.toLowerCase().includes(query));
  }), [filter, search]);
  const appLink = user ? '/app' : '/';

  return (
    <main className="guide-page">
      <header className="guide-topbar"><Link className="echo-brand" to={appLink}><span className="brand-mark">e</span> echo<span className="brand-dot">.</span></Link><Link className="back-link" to={appLink}><FiArrowLeft /> {user ? 'Back to workspace' : 'Back to home'}</Link></header>
      <section className="guide-hero">
        <div className="guide-kicker"><FiBookOpen /> ECHO MATERIAL GUIDE</div>
        <h1>Find the right next step<br /><em>for everyday items.</em></h1>
        <p>Search common materials and get a simple starting point for reuse, recycling, organics, and special disposal.</p>
        <label className="guide-search"><FiSearch /><span className="visually-hidden">Search materials</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Try batteries, cardboard, food scraps…" /></label>
        <div className="guide-note"><FiShield /><span><b>Local rules can differ.</b> This is general guidance. Check with your local collection provider for accepted materials and drop-off locations.</span></div>
      </section>
      <section className="guide-results" aria-live="polite">
        <div className="guide-results-head"><div><span className="eyebrow">QUICK DISPOSAL LOOKUP</span><h2>{results.length} helpful {results.length === 1 ? 'guide' : 'guides'}</h2></div><span className="guide-count"><FiCheckCircle /> Updated starter guide</span></div>
        <div className="guide-filters" aria-label="Filter by material type">{filters.map((value) => <button key={value} className={filter === value ? 'active' : ''} onClick={() => setFilter(value)}>{value}</button>)}</div>
        {results.length ? <div className="guide-grid">{results.map((material, index) => <article className="guide-card" key={material.item} style={{ '--card-index': index }}><div className="guide-card-top"><span className="guide-item-icon" aria-hidden="true">{material.icon}</span><span className="guide-category">{material.category}</span></div><h3>{material.item}</h3><p>{material.detail}</p><div className="guide-disposal"><FiTrash2 /><span><small>START HERE</small><b>{material.bin}</b></span></div></article>)}</div> : <div className="guide-empty"><FiSearch /><h3>No matching materials yet</h3><p>Try a shorter search or choose another category.</p><button className="btn-outline-echo" onClick={() => { setSearch(''); setFilter('All items'); }}>Clear filters</button></div>}
      </section>
    </main>
  );
}
