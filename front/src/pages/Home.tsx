import './Home.css'

function Home() {
  return (
    <main className="home-page">
      <section className="home-hero">
        <div className="home-emblem" aria-hidden="true">
          <span>✦</span>
        </div>

        <p className="home-eyebrow">Registre de la guilde</p>

        <h1>Les Capuches d'Opale</h1>

        <div className="home-divider">
          <span />
          <span className="home-divider-symbol">✧</span>
          <span />
        </div>

        <p className="home-subtitle">
          Guilde d'aventuriers
        </p>

        <p className="home-description">
          Dans les terres où les légendes prennent vie,
          chaque aventurier a son rôle à jouer.
          Gérez vos membres, préparez vos expéditions
          et veillez sur les ressources de votre guilde.
        </p>

        <div className="home-status">
          <span className="home-status-indicator" />
          <span>Votre guilde vous attend</span>
        </div>
      </section>
    </main>
  )
}

export default Home