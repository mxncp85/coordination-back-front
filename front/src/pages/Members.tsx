import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'

import {
  getAventuriers,
  createAventurier,
  deleteAventurier
} from '../services/aventuriers'

import { ApiError } from '../services/api'
import type { Aventurier, Specialite } from '../types/models'

import './Members.css'

const SPECIALITES: Specialite[] = [
  'ARCHER',
  'MAGE_ARCANIQUE'
]

function Members() {
  const [aventuriers, setAventuriers] = useState<Aventurier[]>([])
  const [nom, setNom] = useState('')
  const [specialites, setSpecialites] = useState<Specialite[]>([])
  const [experience, setExperience] = useState('')
  const [tauxBase, setTauxBase] = useState('')
  const [recherche, setRecherche] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function chargerAventuriers() {
    try {
      setError('')
      const data = await getAventuriers()
      setAventuriers(data)
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Impossible de charger les aventuriers.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void chargerAventuriers()
  }, [])

  function toggleSpecialite(value: Specialite) {
    setSpecialites(previous =>
      previous.includes(value)
        ? previous.filter(item => item !== value)
        : [...previous, value]
    )
  }

  function resetForm() {
    setNom('')
    setSpecialites([])
    setExperience('')
    setTauxBase('')
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()

    const nomNormalise = nom.trim()
    const experienceValue = Number(experience)
    const tauxValue = Number(tauxBase)

    if (!nomNormalise) {
      setError('Le nom est obligatoire.')
      return
    }

    if (specialites.length === 0) {
      setError('Sélectionnez au moins une spécialité.')
      return
    }

    if (
      experience.trim() === '' ||
      !Number.isInteger(experienceValue) ||
      experienceValue < 1
    ) {
      setError('L’expérience doit être un entier supérieur ou égal à 1.')
      return
    }

    if (
      tauxBase.trim() === '' ||
      !Number.isFinite(tauxValue) ||
      tauxValue < 0
    ) {
      setError('Le taux de base doit être un nombre positif ou nul.')
      return
    }

    try {
      setSaving(true)
      setError('')

      await createAventurier({
        nom: nomNormalise,
        specialites,
        experience: experienceValue,
        tauxBaseBronze: tauxValue
      })

      resetForm()
      await chargerAventuriers()
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Impossible de créer cet aventurier.'
      )
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: number, nomAventurier: string) {
    if (!window.confirm(`Supprimer ${nomAventurier} ?`)) return

    try {
      setError('')
      await deleteAventurier(id)

      setAventuriers(previous =>
        previous.filter(a => a.id !== id)
      )
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setError(
          'Cet aventurier appartient à une équipe et ne peut pas être supprimé.'
        )
      } else {
        setError(
          err instanceof Error
            ? err.message
            : 'La suppression a échoué.'
        )
      }
    }
  }

  const filtres = aventuriers.filter(a =>
    `${a.nom} ${a.specialites.join(' ')}`
      .toLowerCase()
      .includes(recherche.toLowerCase())
  )

  if (loading) {
    return <main className="members-page">Chargement...</main>
  }

  return (
    <main className="members-page">
      <header>
        <h1>Gestion des aventuriers</h1>
        <p>Gérez les membres de votre guilde.</p>
      </header>

      {error && <p role="alert" className="error-message">{error}</p>}

      <section className="members-form-section">
        <h2>Ajouter un aventurier</h2>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="nom">Nom</label>
            <input
              id="nom"
              value={nom}
              onChange={e => setNom(e.target.value)}
              required
            />
          </div>

          <fieldset>
            <legend>Spécialités</legend>

            {SPECIALITES.map(specialite => (
              <label key={specialite}>
                <input
                  type="checkbox"
                  checked={specialites.includes(specialite)}
                  onChange={() => toggleSpecialite(specialite)}
                />
                {specialite}
              </label>
            ))}
          </fieldset>

          <div className="form-group">
            <label htmlFor="experience">Expérience</label>
            <input
              id="experience"
              type="number"
              min="1"
              step="1"
              value={experience}
              onChange={e => setExperience(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="tauxBase">Taux de base (bronze)</label>
            <input
              id="tauxBase"
              type="number"
              min="0"
              step="any"
              value={tauxBase}
              onChange={e => setTauxBase(e.target.value)}
              required
            />
          </div>

          <button type="submit" disabled={saving}>
            {saving ? 'Enregistrement...' : 'Ajouter'}
          </button>
        </form>
      </section>

      <section className="members-list-section">
        <h2>Aventuriers ({aventuriers.length})</h2>

        <label htmlFor="members-search">
          Rechercher un aventurier
        </label>

        <input
          id="members-search"
          type="search"
          value={recherche}
          onChange={e => setRecherche(e.target.value)}
          placeholder="Nom ou spécialité"
        />

        {filtres.length === 0 ? (
          <p>Aucun résultat.</p>
        ) : (
          <div className="members-table-container">
            <table>
              <thead>
                <tr>
                  <th>Nom</th>
                  <th>Spécialités</th>
                  <th>Expérience</th>
                  <th>Taux de base</th>
                  <th>Taux journalier</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {filtres.map(a => (
                  <tr key={a.id}>
                    <td>{a.nom}</td>
                    <td>{a.specialites.join(', ')}</td>
                    <td>{a.experience}</td>
                    <td>{a.tauxBaseBronze} bronze</td>
                    <td>{a.tauxJournalierBronze} bronze</td>
                    <td>{a.statut}</td>
                    <td>
                      <button
                        type="button"
                        aria-label={`Supprimer ${a.nom}`}
                        onClick={() => handleDelete(a.id, a.nom)}
                      >
                        Supprimer
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
