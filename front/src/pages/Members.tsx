
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import './Members.css'

interface Aventurier {
  id: number
  nom: string
  specialite: string
  experience: number
}

function Members() {
  const [aventuriers, setAventuriers] = useState<Aventurier[]>(() => {
    const saved = localStorage.getItem('aventuriers')
    return saved ? JSON.parse(saved) as Aventurier[] : []
  })

  const [nom, setNom] = useState('')
  const [specialite, setSpecialite] = useState('')
  const [experience, setExperience] = useState<number>(0)
  const [recherche, setRecherche] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)

  // Sauvegarde locale des aventuriers
  useEffect(() => {
    localStorage.setItem('aventuriers', JSON.stringify(aventuriers))
  }, [aventuriers])

  // Ajouter ou modifier un aventurier
  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (!nom.trim() || !specialite.trim() || experience < 0) return

    if (editingId !== null) {
      setAventuriers(prev =>
        prev.map(a =>
          a.id === editingId
            ? { ...a, nom, specialite, experience }
            : a
        )
      )
    } else {
      const nouvelAventurier: Aventurier = {
        id: Date.now(),
        nom: nom.trim(),
        specialite: specialite.trim(),
        experience
      }

      setAventuriers(prev => [...prev, nouvelAventurier])
    }

    resetForm()
  }

  // Préparer la modification
  const handleEdit = (aventurier: Aventurier) => {
    setEditingId(aventurier.id)
    setNom(aventurier.nom)
    setSpecialite(aventurier.specialite)
    setExperience(aventurier.experience)
  }

  // Réinitialiser le formulaire
  const resetForm = () => {
    setNom('')
    setSpecialite('')
    setExperience(0)
    setEditingId(null)
  }

  // Rechercher par nom ou spécialité
  const aventuriersFiltres = aventuriers.filter(a =>
    `${a.nom} ${a.specialite}`
      .toLowerCase()
      .includes(recherche.toLowerCase())
  )

  return (
    <main className="members-page">
      <header className="members-header">
        <h1>Gestion des aventuriers</h1>
        <p>Gérez les membres de votre guilde.</p>
      </header>

      <section className="members-form-section">
        <h2>
          {editingId !== null
            ? 'Modifier un aventurier'
            : 'Ajouter un aventurier'}
        </h2>

        <form onSubmit={handleSubmit} className="members-form">
          <div className="form-group">
            <label htmlFor="nom">Nom de l'aventurier</label>
            <input
              id="nom"
              type="text"
              value={nom}
              onChange={e => setNom(e.target.value)}
              placeholder="Ex : Arthus"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="specialite">Spécialité</label>
            <input
              id="specialite"
              type="text"
              value={specialite}
              onChange={e => setSpecialite(e.target.value)}
              placeholder="Ex : Guerrier, Mage, Archer..."
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="experience">Expérience (années)</label>
            <input
              id="experience"
              type="number"
              min="0"
              value={experience}
              onChange={e => setExperience(Number(e.target.value))}
              required
            />
          </div>

          <div className="form-actions">
            <button type="submit">
              {editingId !== null ? 'Enregistrer les modifications' : 'Ajouter'}
            </button>

            {editingId !== null && (
              <button type="button" onClick={resetForm}>
                Annuler
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="members-list-section">
        <h2>Liste des aventuriers ({aventuriers.length})</h2>

        <input
          className="search-input"
          type="search"
          placeholder="Rechercher par nom ou spécialité..."
          value={recherche}
          onChange={e => setRecherche(e.target.value)}
        />

        {aventuriersFiltres.length === 0 ? (
          <p className="empty-message">
            {aventuriers.length === 0
              ? 'Aucun aventurier enregistré.'
              : 'Aucun résultat trouvé.'}
          </p>
        ) : (
          <div className="members-table-container">
            <table className="members-table">
              <thead>
                <tr>
                  <th>Nom</th>
                  <th>Spécialité</th>
                  <th>Expérience</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {aventuriersFiltres.map(aventurier => (
                  <tr key={aventurier.id}>
                    <td>{aventurier.nom}</td>
                    <td>{aventurier.specialite}</td>
                    <td>{aventurier.experience} ans</td>
                    <td>
                      <button
                        type="button"
                        className="edit-button"
                        onClick={() => handleEdit(aventurier)}
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

export default Members
