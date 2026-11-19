import './App.css'
import { AdventurerSchedulePage } from './pages/AdventurerSchedulePage'

function App() {
  return (
    <main className="app">
      <header>
        <h1>Les Capuches d'Opale</h1>
        <p>Guilde d'aventuriers — outil de gestion en construction.</p>
      </header>

      <AdventurerSchedulePage />
    </main>
  )
}

export default App