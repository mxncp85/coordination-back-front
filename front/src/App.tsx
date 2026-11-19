import { NavLink, Route, Routes } from 'react-router-dom'
import Home from './pages/Home'
import Members from './pages/Members'
import Equipment from './pages/Equipment'
import Accounts from './pages/Accounts'
import './App.css'

const navigation = [
  { to: '/', label: 'Tableau de bord', icon: '⌂', end: true },
  { to: '/members', label: 'Aventuriers', icon: '⚔' },
  { to: '/equipment', label: 'Équipements', icon: '🛡' },
  { to: '/accounts', label: 'Trésorerie', icon: '◈' }
]

function App() {
  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="brand-emblem">CO</div>
          <div>
            <div className="brand-title">Les Capuches</div>
            <div className="brand-subtitle">D'OPALE · GUILDE</div>
          </div>
        </div>

        <div className="sidebar-section-label">GESTION DE GUILDE</div>

        <nav className="sidebar-nav" aria-label="Navigation principale">
          {navigation.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `sidebar-link${isActive ? ' active' : ''}`
              }
            >
              <span className="sidebar-icon" aria-hidden="true">
                {item.icon}
              </span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="sidebar-footer-mark">✦</div>
          <div>
            <div className="sidebar-footer-title">La guilde vous salue</div>
            <div className="sidebar-footer-text">Que l'or et la gloire vous accompagnent.</div>
          </div>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="topbar-context">
            <span className="status-indicator" />
            <span>Registre de la guilde</span>
          </div>
          <span className="topbar-version">CAPUCHES D'OPALE · 0.1</span>
        </header>

        <div className="page-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/members" element={<Members />} />
            <Route path="/equipment" element={<Equipment />} />
            <Route path="/accounts" element={<Accounts />} />
          </Routes>
        </div>
      </main>
    </div>
  )
}

export default App