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
  'BARBARE',
  'PALADIN',
  'MAGE_ARCANIQUE',
  'PRETRE',
  'GEOMANCIEN',
  'ALCHIMISTE',
  'FORGERON',
  'ENCHANTEUR',
  'MESSAGER',
  'AUTRE'
]

const NOMS_SPECIALITES: Record<Specialite, string> = {
  ARCHER: 'Archer',
  BARBARE: 'Barbare',
  PALADIN: 'Paladin',
  MAGE_ARCANIQUE: 'Mage arcanique',
  PRETRE: 'Prêtre',
  GEOMANCIEN: 'Géomancien',
  ALCHIMISTE: 'Alchimiste',
  FORGERON: 'Forgeron',
  ENCHANTEUR: 'Enchanteur',
  MESSAGER: 'Messager',
  AUTRE: 'Autre'
}

const NOMS_STATUTS: Record<string, string> = {
  DISPONIBLE: 'Disponible',
  EN_MISSION: 'En mission',
  EN_REPOS: 'En repos'
}

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
  const [success, setSuccess] = useState('')

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
      !Number.isInteger(tauxValue) ||
      tauxValue < 1
    ) {
      setError('Le taux de base doit être un entier supérieur ou égal à 1 bronze.')
      return
    }

    try {
      setSaving(true)
      setError('')
      setSuccess('')

      await createAventurier({
        nom: nomNormalise,
        specialites,
        experience: experienceValue,
        tauxBaseBronze: tauxValue
      })

      resetForm()
      await chargerAventuriers()
      setSuccess(`${nomNormalise} a rejoint la guilde.`)
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
    if (!window.confirm(`Supprimer ${nomAventurier} de la guilde ?`)) return

    try {
      setError('')
      setSuccess('')
      await deleteAventurier(id)

      setAventuriers(previous =>
        previous.filter(a => a.id !== id)
      )

      setSuccess(`${nomAventurier} a quitté la guilde.`)
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setError(
          'Cet aventurier appartient à une équipe ou est en mission. Il ne peut pas être supprimé.'
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

  const rechercheNormalisee = recherche.trim().toLowerCase()

  const filtres = aventuriers.filter(a =>
    `${a.nom} ${a.specialites
      .map(specialite => NOMS_SPECIALITES[specialite] ?? specialite)
      .join(' ')}`
      .toLowerCase()
      .includes(rechercheNormalisee)
  )

  if (loading) {
    return (
      <main className="members-page">
        <div className="members-loading">
          <span className="loading-rune">✧</span>
          <p>Consultation du registre de la guilde...</p>
        </div>
      </main>
    )
  }

  return (
    <main className="members-page">
      <header className="members-header">
        <div>
          <p className="members-eyebrow">Registre de la guilde</p>
          <h1>Les aventuriers</h1>
          <p className="members-intro">
            Recrutez, consultez et gérez les membres de votre guilde.
          </p>
        </div>

        <div className="members-count">
          <span className="members-count-number">{aventuriers.length}</span>
          <span className="members-count-label">
            {aventuriers.length > 1 ? 'aventuriers' : 'aventurier'}
          </span>
        </div>
      </header>

      {error && (
        <p role="alert" className="members-message members-message-error">
          <span>⚠</span> {error}
        </p>
      )}

      {success && (
        <p role="status" className="members-message members-message-success">
          <span>✦</span> {success}
        </p>
      )}

      <section className="members-form-section">
        <div className="members-section-heading">
          <div>
            <p className="members-section-kicker">Recrutement</p>
            <h2>Accueillir un aventurier</h2>
          </div>
          <span className="members-section-icon" aria-hidden="true">⚔</span>
        </div>

        <form onSubmit={handleSubmit} className="members-form">
          <div className="members-form-grid">
            <div className="form-group">
              <label htmlFor="nom">Nom de l'aventurier</label>
              <input
                id="nom"
                value={nom}
                onChange={e => setNom(e.target.value)}
                placeholder="Ex. : Aldric le Vaillant"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="experience">Expérience</label>
              <input
                id="experience"
                type="number"
                min="1"
                step="1"
                value={experience}
                onChange={e => setExperience(e.target.value)}
                placeholder="Ex. : 5"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="tauxBase">Taux de base journalier (bronze)</label>
              <input
                id="tauxBase"
                type="number"
                min="1"
                step="1"
                value={tauxBase}
                onChange={e => setTauxBase(e.target.value)}
                placeholder="Ex. : 100"
                required
              />
            </div>
          </div>

          <fieldset className="members-specialities">
            <legend>Spécialités</legend>
            <p className="members-field-description">
              Sélectionnez au moins une compétence.
            </p>

            <div className="specialities-grid">
              {SPECIALITES.map(specialite => (
                <label
                  key={specialite}
                  className={`speciality-option ${
                    specialites.includes(specialite) ? 'selected' : ''
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={specialites.includes(specialite)}
                    onChange={() => toggleSpecialite(specialite)}
                  />
                  <span className="speciality-check">
                    {specialites.includes(specialite) ? '✓' : ''}
                  </span>
                  <span>{NOMS_SPECIALITES[specialite]}</span>
                </label>
              ))}
            </div>
          </fieldset>

          <div className="members-form-actions">
            <p className="members-form-hint">
              Les champs marqués sont nécessaires au recrutement.
            </p>

            <button
              type="submit"
              className="members-primary-button"
              disabled={saving}
            >
              <span aria-hidden="true">✦</span>
              {saving ? 'Recrutement...' : 'Recruter l’aventurier'}
            </button>
          </div>
        </form>
      </section>

      <section className="members-list-section">
        <div className="members-section-heading members-list-heading">
          <div>
            <p className="members-section-kicker">Effectifs</p>
            <h2>Registre des membres</h2>
          </div>
          <span className="members-total">
            {aventuriers.length} inscrit{aventuriers.length > 1 ? 's' : ''}
          </span>
        </div>

        <div className="members-search">
          <label htmlFor="members-search">Rechercher dans le registre</label>
          <input
            id="members-search"
            type="search"
            value={recherche}
            onChange={e => setRecherche(e.target.value)}
            placeholder="Nom ou spécialité..."
          />
        </div>

        {filtres.length === 0 ? (
          <div className="members-empty">
            <span aria-hidden="true">✧</span>
            <p>
              {aventuriers.length === 0
                ? 'Aucun aventurier dans la guilde pour le moment.'
                : 'Aucun aventurier ne correspond à votre recherche.'}
            </p>
          </div>
        ) : (
          <div className="members-table-container">
            <table className="members-table">
              <thead>
                <tr>
                  <th>Aventurier</th>
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
                    <td className="member-name">{a.nom}</td>
                    <td>
                      <div className="member-specialities">
                        {a.specialites.map(specialite => (
                          <span className="member-speciality-tag" key={specialite}>
                            {NOMS_SPECIALITES[specialite] ?? specialite}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td>{a.experience}</td>
                    <td>{a.tauxBaseBronze} <small>bronze</small></td>
                    <td>{a.tauxJournalierBronze} <small>bronze</small></td>
                    <td>
                      <span
                        className={`member-status member-status-${String(a.statut).toLowerCase()}`}
                      >
                        {NOMS_STATUTS[a.statut] ?? a.statut}
                      </span>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="members-delete-button"
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