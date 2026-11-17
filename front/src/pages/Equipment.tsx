
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import './Equipment.css'

type Statut = 'disponible' | 'emprunté' | 'en réparation'

interface Equipement {
  id: number
  nom: string
  categorie: string
  quantite: number
  prix: number
  statut: Statut
}

function Equipment() {
  const [equipements, setEquipements] = useState<Equipement[]>(() => {
    const saved = localStorage.getItem('equipements')
    return saved ? JSON.parse(saved) as Equipement[] : []
  })

  const [nom, setNom] = useState('')
  const [categorie, setCategorie] = useState('')
  const [quantite, setQuantite] = useState(1)
  const [prix, setPrix] = useState(0)
  const [statut, setStatut] = useState<Statut>('disponible')
  const [recherche, setRecherche] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)

  useEffect(() => {
    localStorage.setItem('equipements', JSON.stringify(equipements))
  }, [equipements])

  const resetForm = () => {
    setNom('')
    setCategorie('')
    setQuantite(1)
    setPrix(0)
    setStatut('disponible')
    setEditingId(null)
  }

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (!nom.trim() || !categorie.trim() || quantite < 1 || prix < 0) {
      return
    }

    if (editingId !== null) {
      setEquipements(prev =>
        prev.map(item =>
          item.id === editingId
            ? { ...item, nom, categorie, quantite, prix, statut }
            : item
        )
      )
    } else {
      const nouvelEquipement: Equipement = {
        id: Date.now(),
        nom: nom.trim(),
        categorie: categorie.trim(),
        quantite,
        prix,
        statut
      }

      setEquipements(prev => [...prev, nouvelEquipement])
    }

    resetForm()
  }

  const handleEdit = (item: Equipement) => {
    setEditingId(item.id)
    setNom(item.nom)
    setCategorie(item.categorie)
    setQuantite(item.quantite)
    setPrix(item.prix)
    setStatut(item.statut)
  }

  const equipementsFiltres = equipements.filter(item =>
    `${item.nom} ${item.categorie}`
      .toLowerCase()
      .includes(recherche.toLowerCase())
  )

  const totalAchats = equipements.reduce(
    (total, item) => total + item.prix * item.quantite,
    0
  )

  return (
    <main className="equipment-page">

      <header className="equipment-header">
        <h1>Gestion des équipements</h1>
        <p>Gérez le matériel et les ressources de votre guilde.</p>
      </header>

      <section className="equipment-stats">
        <div className="stat-card">
          <h3>Équipements</h3>
          <p>{equipements.length}</p>
        </div>

        <div className="stat-card">
          <h3>Disponibles</h3>
          <p>
            {equipements.filter(e => e.statut === 'disponible').length}
          </p>
        </div>

        <div className="stat-card">
          <h3>Empruntés</h3>
          <p>
            {equipements.filter(e => e.statut === 'emprunté').length}
          </p>
        </div>

        <div className="stat-card">
          <h3>En réparation</h3>
          <p>
            {equipements.filter(e => e.statut === 'en réparation').length}
          </p>
        </div>
      </section>

      <section className="equipment-form-section">
        <h2>
          {editingId !== null
            ? 'Modifier un équipement'
            : 'Ajouter / Acheter un équipement'}
        </h2>

        <form onSubmit={handleSubmit} className="equipment-form">

          <div className="form-group">
            <label htmlFor="equipment-name">Nom</label>
            <input
              id="equipment-name"
              type="text"
              value={nom}
              onChange={e => setNom(e.target.value)}
              placeholder="Ex : Épée en acier"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="equipment-category">Catégorie</label>
            <input
              id="equipment-category"
              type="text"
              value={categorie}
              onChange={e => setCategorie(e.target.value)}
              placeholder="Ex : Arme, Armure, Potion..."
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="equipment-quantity">Quantité</label>
            <input
              id="equipment-quantity"
              type="number"
              min="1"
              value={quantite}
              onChange={e => setQuantite(Number(e.target.value))}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="equipment-price">Prix unitaire (€)</label>
            <input
              id="equipment-price"
              type="number"
              min="0"
              step="0.01"
              value={prix}
              onChange={e => setPrix(Number(e.target.value))}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="equipment-status">Statut</label>
            <select
              id="equipment-status"
              value={statut}
              onChange={e => setStatut(e.target.value as Statut)}
            >
              <option value="disponible">Disponible</option>
              <option value="emprunté">Emprunté</option>
              <option value="en réparation">En réparation</option>
            </select>
          </div>

          <div className="equipment-form-actions">
            <button type="submit">
              {editingId !== null ? 'Enregistrer' : 'Ajouter'}
            </button>

            {editingId !== null && (
              <button type="button" onClick={resetForm}>
                Annuler
              </button>
            )}
          </div>

        </form>
      </section>

      <section className="equipment-list-section">
        <div className="equipment-list-header">
          <h2>Inventaire des équipements</h2>
          <p>Valeur totale des achats : {totalAchats.toFixed(2)} €</p>
        </div>

        <input
          className="equipment-search"
          type="search"
          placeholder="Rechercher un équipement..."
          value={recherche}
          onChange={e => setRecherche(e.target.value)}
        />

        {equipementsFiltres.length === 0 ? (
          <p className="empty-message">
            Aucun équipement trouvé.
          </p>
        ) : (
          <div className="equipment-table-container">
            <table className="equipment-table">
              <thead>
                <tr>
                  <th>Nom</th>
                  <th>Catégorie</th>
                  <th>Quantité</th>
                  <th>Prix unitaire</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {equipementsFiltres.map(item => (
                  <tr key={item.id}>
                    <td>{item.nom}</td>
                    <td>{item.categorie}</td>
                    <td>{item.quantite}</td>
                    <td>{item.prix.toFixed(2)} €</td>
                    <td>
                      <span className={`status-badge ${
                        item.statut === 'disponible'
                          ? 'available'
                          : item.statut === 'emprunté'
                            ? 'borrowed'
                            : 'repair'
                      }`}>
                        {item.statut}
                      </span>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="equipment-edit-button"
                        onClick={() => handleEdit(item)}
                      >
                        Modifier
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

    </main>
  )
}

export default Equipment
