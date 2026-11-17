import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'

import Home from './pages/Home'
import Members from './pages/Members'
import Equipment from './pages/Equipment'

import './App.css'

function App() {
  return (
    <BrowserRouter>
      <nav className="navbar">
        <Link to="/">Accueil</Link>
        <Link to="/members">Aventuriers</Link>
        <Link to="/equipment">Équipements</Link>
      </nav>

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/members" element={<Members />} />
        <Route path="/equipment" element={<Equipment />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App