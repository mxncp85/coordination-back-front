
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'

import {
  getEquipements,
  purchaseEquipement,
  repairEquipement
} from '../services/equipements'

import type { Equipement } from '../types/models'

import './Equipment.css'

const TYPES_EQUIPEMENT = [
  'ARME',
  'ARMURE',
  'ACCESSOIRE',
  'AUTRE'
]

function Equipment() {
  const [equipements, setEquipements] = useState<Equipement[]>([])
  const [nom, setNom] = useState('')
  const [type, setType] = useState(TYPES_EQUIPEMENT[0])
  const [durabiliteMax, setDurabiliteMax] = useState('')
  const [prixAchat, setPrixAchat] = useState('')
  const [recherche, setRecherche] = useState('')
  const [filtreStatut, setFiltreStatut] = useState('TOUS')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function chargerEquipements() {
    try {
      setError('')
      const data = await getEquipements()
      setEquipements(data)
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Impossible de charger les équipements.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void chargerEquipements()
  }, [])

  function resetForm() {
    setNom('')
    setType(TYPES_EQUIPEMENT[0])
    setDurabiliteMax('')
    setPrixAchat('')
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()

    const nomNormalise = nom.trim()
    const durabilite = Number(durabiliteMax)
    const prix = Number(prixAchat)

    if (!nomNormalise) {
      setError('Le nom est obligatoire.')
      return
    }

    if (
      durabiliteMax.trim() === '' ||
      !Number.isInteger(durabilite) ||
      durabilite < 1
    ) {
      setError('La durabilité maximale doit être un entier supérieur à zéro.')
      return
    }

    if (
      prixAchat.trim() === '' ||
      !Number.isFinite(prix) ||
      prix < 0
    ) {
      setError('Le prix doit être un nombre positif ou nul.')
      return
    }

    try {
      setSaving(true)
      setError('')

      await purchaseEquipement({
        nom: nomNormalise,
        type,
        durabiliteMax: durabilite,
        prixAchatBronze: prix
      })

      resetForm()
      await chargerEquipements()
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'L’achat a échoué.'
      )
    } finally {
      setSaving(false)
    }
  }

  async function handleRepair(id: number) {
    if (!window.confirm('Confirmer la réparation de cet équipement ?')) {
      return
    }

    try {
      setError('')
      await repairEquipement(id)
      await chargerEquipements()
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'La réparation a échoué.'
      )
    }
  }

  const equipementsFiltres = equipements.filter(e => {
    const rechercheOK =
      `${e.nom} ${e.type}`
        .toLowerCase()
        .includes(recherche.toLowerCase())

    const statutOK =
      filtreStatut === 'TOUS' || e.statut === filtreStatut

    return rechercheOK && statutOK
  })

  const disponibles = equipements.filter(
    e => e.statut === 'DISPONIBLE'
  ).length

  const empruntes = equipements.filter(
    e => e.statut === 'EMPRUNTE'
  ).length

  const enReparation = equipements.filter(
    e => e.statut === 'EN_REPARATION'
  ).length

  if (loading) {
    return <main className="equipment-page">Chargement...</main>
  }

  return (
    <main className="equipment-page">
      <header>
        <h1>Gestion des équipements</h1>
        <p>Inventaire des équipements de la guilde.</p>
      </header>

      {error && <p role="alert" className="error-message">{error}</p>}

      <section className="equipment-stats">
        <article>
          <h3>Disponibles</h3>
          <p>{disponibles}</p>
        </article>

        <article>
          <h3>Empruntés</h3>
          <p>{empruntes}</p>
        </article>

        <article>
          <h3>En réparation</h3>
          <p>{enReparation}</p>
        </article>
      </section>

      <section className="equipment-form-section">
        <h2>Acheter un équipement</h2>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="equipment-name">Nom</label>
            <input
              id="equipment-name"
              value={nom}
              onChange={e => setNom(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="equipment-type">Type</label>
            <select
              id="equipment-type"
              value={type}
              onChange={e => setType(e.target.value)}
            >
              {TYPES_EQUIPEMENT.map(t => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="equipment-durability">
              Durabilité maximale
            </label>
            <input
              id="equipment-durability"
              type="number"
              min="1"
              step="1"
              value={durabiliteMax}
              onChange={e => setDurabiliteMax(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="equipment-price">
              Prix d'achat (bronze)
            </label>
            <input
              id="equipment-price"
              type="number"
              min="0"
              step="any"
              value={prixAchat}
              onChange={e => setPrixAchat(e.target.value)}
              required
            />
          </div>

          <button type="submit" disabled={saving}>
            {saving ? 'Achat en cours...' : 'Acheter'}
          </button>
        </form>
      </section>

      <section className="equipment-list-section">
        <h2>Inventaire ({equipements.length})</h2>

        <div className="equipment-filters">
          <div>
            <label htmlFor="equipment-search">
              Rechercher un équipement
            </label>
            <input
              id="equipment-search"
              type="search"
              value={recherche}
              onChange={e => setRecherche(e.target.value)}
              placeholder="Nom ou type"
            />
          </div>

          <div>
            <label htmlFor="equipment-status">Filtrer par statut</label>
            <select
              id="equipment-status"
              value={filtreStatut}
              onChange={e => setFiltreStatut(e.target.value)}
            >
              <option value="TOUS">Tous</option>
              <option value="DISPONIBLE">Disponibles</option>
              <option value="EMPRUNTE">Empruntés</option>
              <option value="EN_REPARATION">En réparation</option>
            </select>
          </div>
        </div>

        {equipementsFiltres.length === 0 ? (
          <p>Aucun équipement trouvé.</p>
        ) : (
          <div className="equipment-table-container">
            <table>
              <thead>
                <tr>
                  <th>Nom</th>
                  <th>Type</th>
                  <th>Durabilité</th>
                  <th>Prix d'achat</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {equipementsFiltres.map(e => (
                  <tr key={e.id}>
                    <td>{e.nom}</td>
                    <td>{e.type}</td>
                    <td>
                      {e.durabiliteActuelle} / {e.durabiliteMax}
                    </td>
                    <td>{e.prixAchatBronze} bronze</td>
                    <td>{e.statut}</td>
                    <td>
                      {e.statut === 'EN_REPARATION' && (
                        <button
                          type="button"
                          onClick={() => handleRepair(e.id)}
                        >
                          Réparer
                        </button>
                      )}
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
