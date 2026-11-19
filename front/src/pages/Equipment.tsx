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

const NOMS_TYPES: Record<string, string> = {
  ARME: 'Arme',
  ARMURE: 'Armure',
  ACCESSOIRE: 'Accessoire',
  AUTRE: 'Autre'
}

const NOMS_STATUTS: Record<string, string> = {
  DISPONIBLE: 'Disponible',
  EMPRUNTE: 'Emprunté',
  EN_REPARATION: 'En réparation'
}

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
  const [repairingId, setRepairingId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

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
      setError('Le nom de l’équipement est obligatoire.')
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
      !Number.isInteger(prix) ||
      prix < 0
    ) {
      setError('Le prix doit être un entier positif ou nul en bronze.')
      return
    }

    try {
      setSaving(true)
      setError('')
      setSuccess('')

      await purchaseEquipement({
        nom: nomNormalise,
        type,
        durabiliteMax: durabilite,
        prixAchatBronze: prix
      })

      resetForm()
      await chargerEquipements()
      setSuccess(`${nomNormalise} a été ajouté à l’inventaire.`)
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

  async function handleRepair(id: number, nomEquipement: string) {
    if (
      !window.confirm(
        `Confirmer la réparation de « ${nomEquipement} » ?`
      )
    ) {
      return
    }

    try {
      setRepairingId(id)
      setError('')
      setSuccess('')

      await repairEquipement(id)
      await chargerEquipements()

      setSuccess(`${nomEquipement} a été réparé.`)
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'La réparation a échoué.'
      )
    } finally {
      setRepairingId(null)
    }
  }

  const rechercheNormalisee = recherche.trim().toLowerCase()

  const equipementsFiltres = equipements.filter(e => {
    const rechercheOK =
      `${e.nom} ${NOMS_TYPES[e.type] ?? e.type}`
        .toLowerCase()
        .includes(rechercheNormalisee)

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
    return (
      <main className="equipment-page">
        <div className="equipment-loading">
          <span>✧</span>
          <p>Inventaire des reliques et équipements...</p>
        </div>
      </main>
    )
  }

  return (
    <main className="equipment-page">
      <header className="equipment-header">
        <div>
          <p className="equipment-eyebrow">Trésorerie et intendance</p>
          <h1>Arsenal de la guilde</h1>
          <p className="equipment-intro">
            Gérez les armes, armures et équipements de vos aventuriers.
          </p>
        </div>

        <div className="equipment-total">
          <span>{equipements.length}</span>
          <small>équipement{equipements.length > 1 ? 's' : ''}</small>
        </div>
      </header>

      {error && (
        <p role="alert" className="equipment-message equipment-message-error">
          <span>⚠</span> {error}
        </p>
      )}

      {success && (
        <p role="status" className="equipment-message equipment-message-success">
          <span>✦</span> {success}
        </p>
      )}

      <section className="equipment-stats">
        <article className="equipment-stat-card stat-available">
          <span className="equipment-stat-icon">✦</span>
          <div>
            <h3>Disponibles</h3>
            <p>{disponibles}</p>
          </div>
        </article>

        <article className="equipment-stat-card stat-borrowed">
          <span className="equipment-stat-icon">⚔</span>
          <div>
            <h3>Empruntés</h3>
            <p>{empruntes}</p>
          </div>
        </article>

        <article className="equipment-stat-card stat-repair">
          <span className="equipment-stat-icon">⚒</span>
          <div>
            <h3>En réparation</h3>
            <p>{enReparation}</p>
          </div>
        </article>
      </section>

      <section className="equipment-form-section">
        <div className="equipment-section-heading">
          <div>
            <p className="equipment-section-kicker">Approvisionnement</p>
            <h2>Acquérir un équipement</h2>
          </div>
          <span className="equipment-heading-icon">⚒</span>
        </div>

        <form onSubmit={handleSubmit} className="equipment-form">
          <div className="equipment-form-grid">
            <div className="form-group">
              <label htmlFor="equipment-name">Nom de l'équipement</label>
              <input
                id="equipment-name"
                value={nom}
                onChange={e => setNom(e.target.value)}
                placeholder="Ex. : Épée d'Opale"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="equipment-type">Catégorie</label>
              <select
                id="equipment-type"
                value={type}
                onChange={e => setType(e.target.value)}
              >
                {TYPES_EQUIPEMENT.map(t => (
                  <option key={t} value={t}>
                    {NOMS_TYPES[t]}
                  </option>
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
                placeholder="Ex. : 100"
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
                step="1"
                value={prixAchat}
                onChange={e => setPrixAchat(e.target.value)}
                placeholder="Ex. : 500"
                required
              />
            </div>
          </div>

          <div className="equipment-form-footer">
            <p>
              Le montant sera automatiquement débité du compte de la guilde.
            </p>

            <button
              type="submit"
              className="equipment-primary-button"
              disabled={saving}
            >
              <span>✦</span>
              {saving ? 'Achat en cours...' : 'Acheter l’équipement'}
            </button>
          </div>
        </form>
      </section>

      <section className="equipment-list-section">
        <div className="equipment-section-heading">
          <div>
            <p className="equipment-section-kicker">Collection de la guilde</p>
            <h2>Inventaire</h2>
          </div>
          <span className="equipment-list-count">
            {equipementsFiltres.length} résultat{equipementsFiltres.length > 1 ? 's' : ''}
          </span>
        </div>

        <div className="equipment-filters">
          <div className="equipment-search">
            <label htmlFor="equipment-search">Rechercher</label>
            <input
              id="equipment-search"
              type="search"
              value={recherche}
              onChange={e => setRecherche(e.target.value)}
              placeholder="Nom ou catégorie..."
            />
          </div>

          <div className="equipment-status-filter">
            <label htmlFor="equipment-status">Statut</label>
            <select
              id="equipment-status"
              value={filtreStatut}
              onChange={e => setFiltreStatut(e.target.value)}
            >
              <option value="TOUS">Tous les statuts</option>
              <option value="DISPONIBLE">Disponibles</option>
              <option value="EMPRUNTE">Empruntés</option>
              <option value="EN_REPARATION">En réparation</option>
            </select>
          </div>
        </div>

        {equipementsFiltres.length === 0 ? (
          <div className="equipment-empty">
            <span>✧</span>
            <p>
              {equipements.length === 0
                ? 'Aucun équipement dans l’inventaire pour le moment.'
                : 'Aucun équipement ne correspond à vos filtres.'}
            </p>
          </div>
        ) : (
          <div className="equipment-table-container">
            <table className="equipment-table">
              <thead>
                <tr>
                  <th>Équipement</th>
                  <th>Catégorie</th>
                  <th>Durabilité</th>
                  <th>Prix d'achat</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {equipementsFiltres.map(e => {
                  const durabilitePourcentage =
                    e.durabiliteMax > 0
                      ? Math.max(
                          0,
                          Math.min(
                            100,
                            (e.durabiliteActuelle / e.durabiliteMax) * 100
                          )
                        )
                      : 0

                  return (
                    <tr key={e.id}>
                      <td className="equipment-name">{e.nom}</td>
                      <td>
                        <span className="equipment-type-tag">
                          {NOMS_TYPES[e.type] ?? e.type}
                        </span>
                      </td>
                      <td>
                        <div className="equipment-durability">
                          <span>
                            {e.durabiliteActuelle} / {e.durabiliteMax}
                          </span>
                          <div
                            className="durability-track"
                            role="progressbar"
                            aria-label={`Durabilité de ${e.nom}`}
                            aria-valuenow={e.durabiliteActuelle}
                            aria-valuemin={0}
                            aria-valuemax={e.durabiliteMax}
                          >
                            <div
                              className="durability-fill"
                              style={{ width: `${durabilitePourcentage}%` }}
                            />
                          </div>
                        </div>
                      </td>
                      <td>{e.prixAchatBronze} <small>bronze</small></td>
                      <td>
                        <span
                          className={`equipment-status equipment-status-${String(e.statut).toLowerCase()}`}
                        >
                          {NOMS_STATUTS[e.statut] ?? e.statut}
                        </span>
                      </td>
                      <td>
                        {e.statut === 'EN_REPARATION' ? (
                          <button
                            type="button"
                            className="equipment-repair-button"
                            disabled={repairingId === e.id}
                            onClick={() => handleRepair(e.id, e.nom)}
                          >
                            {repairingId === e.id
                              ? 'Réparation...'
                              : 'Réparer'}
                          </button>
                        ) : (
                          <span className="equipment-no-action">—</span>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  )
}

export default Equipment