import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'

import {
  getSolde,
  getTransactions,
  createTransaction
} from '../services/comptes'

import type {
  Transaction,
  SensTransaction
} from '../services/comptes'

import './Accounts.css'

const TYPES_LIBELLES: Record<string, string> = {
  ACOMPTE: 'Acompte de mission',
  SOLDE: 'Solde de mission',
  PAIEMENT_AVENTURIER: 'Paiement aventurier',
  ACHAT: 'Achat',
  MANUEL: 'Opération manuelle'
}

function formaterBronze(montant: number) {
  return `${Math.abs(montant).toLocaleString('fr-FR')} bronze`
}

function formaterDate(date: string) {
  const valeur = new Date(date)

  if (Number.isNaN(valeur.getTime())) {
    return date
  }

  return valeur.toLocaleString('fr-FR', {
    dateStyle: 'short',
    timeStyle: 'short'
  })
}

function Accounts() {
  const [solde, setSolde] = useState<number | null>(null)
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [montant, setMontant] = useState('')
  const [sens, setSens] = useState<SensTransaction>('CREDIT')
  const [libelle, setLibelle] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  async function chargerCompte() {
    try {
      setError('')

      const [soldeData, transactionsData] = await Promise.all([
        getSolde(),
        getTransactions(0, 50)
      ])

      setSolde(soldeData.soldeBronze)
      setTransactions(transactionsData.content)
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Impossible de charger les informations du compte.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void chargerCompte()
  }, [])

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()

    const montantValue = Number(montant)
    const libelleNormalise = libelle.trim()

    if (
      montant.trim() === '' ||
      !Number.isSafeInteger(montantValue) ||
      montantValue <= 0
    ) {
      setError('Le montant doit être un entier strictement positif en bronze.')
      return
    }

    if (!libelleNormalise) {
      setError('Le libellé de la transaction est obligatoire.')
      return
    }

    try {
      setSaving(true)
      setError('')
      setSuccess('')

      await createTransaction({
        montantBronze: montantValue,
        sens,
        libelle: libelleNormalise
      })

      setMontant('')
      setLibelle('')

      await chargerCompte()
      setSuccess(
        sens === 'CREDIT'
          ? 'Le crédit a bien été enregistré.'
          : 'Le débit a bien été enregistré.'
      )
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Impossible d’enregistrer cette transaction.'
      )
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <main className="accounts-page">
        <div className="accounts-loading">
          <span>✧</span>
          <p>Ouverture du coffre de la guilde...</p>
        </div>
      </main>
    )
  }

  return (
    <main className="accounts-page">
      <header className="accounts-header">
        <div>
          <p className="accounts-eyebrow">Trésorerie de la guilde</p>
          <h1>Le coffre de la guilde</h1>
          <p className="accounts-intro">
            Consultez vos réserves et suivez les mouvements financiers.
          </p>
        </div>
        <span className="accounts-header-icon" aria-hidden="true">♜</span>
      </header>

      {error && (
        <p role="alert" className="accounts-message accounts-message-error">
          <span>⚠</span> {error}
        </p>
      )}

      {success && (
        <p role="status" className="accounts-message accounts-message-success">
          <span>✦</span> {success}
        </p>
      )}

      <section className="accounts-balance-card">
        <div>
          <p className="accounts-balance-label">Trésor disponible</p>
          <h2>
            {solde === null
              ? 'Indisponible'
              : solde.toLocaleString('fr-FR')}
            {solde !== null && <span> bronze</span>}
          </h2>
          <p className="accounts-balance-note">
            Solde actuel du compte de la guilde
          </p>
        </div>
        <span className="accounts-coin" aria-hidden="true">◉</span>
      </section>

      <section className="accounts-form-section">
        <div className="accounts-section-heading">
          <div>
            <p className="accounts-section-kicker">Gestion du trésor</p>
            <h2>Enregistrer une opération</h2>
          </div>
          <span aria-hidden="true">⚖</span>
        </div>

        <form onSubmit={handleSubmit} className="accounts-form">
          <div className="accounts-form-grid">
            <div className="accounts-form-group">
              <label htmlFor="transaction-sens">Type d'opération</label>
              <select
                id="transaction-sens"
                value={sens}
                onChange={e => setSens(e.target.value as SensTransaction)}
              >
                <option value="CREDIT">Ajouter au compte</option>
                <option value="DEBIT">Retirer du compte</option>
              </select>
            </div>

            <div className="accounts-form-group">
              <label htmlFor="transaction-montant">Montant (bronze)</label>
              <input
                id="transaction-montant"
                type="number"
                min="1"
                step="1"
                value={montant}
                onChange={e => setMontant(e.target.value)}
                placeholder="Ex. : 500"
                required
              />
            </div>

            <div className="accounts-form-group accounts-form-group-full">
              <label htmlFor="transaction-libelle">Libellé</label>
              <input
                id="transaction-libelle"
                value={libelle}
                onChange={e => setLibelle(e.target.value)}
                placeholder="Ex. : Récompense exceptionnelle"
                required
              />
            </div>
          </div>

          <p className="accounts-form-hint">
            Les opérations enregistrées sont définitives. Une correction
            doit être effectuée par une nouvelle transaction.
          </p>

          <button
            type="submit"
            className="accounts-submit-button"
            disabled={saving}
          >
            {saving
              ? 'Enregistrement...'
              : sens === 'CREDIT'
                ? 'Ajouter au trésor'
                : 'Retirer du trésor'}
          </button>
        </form>
      </section>

      <section className="accounts-history-section">
        <div className="accounts-section-heading">
          <div>
            <p className="accounts-section-kicker">Registre financier</p>
            <h2>Historique des transactions</h2>
          </div>
          <span className="accounts-history-count">
            {transactions.length} opération{transactions.length > 1 ? 's' : ''}
          </span>
        </div>

        {transactions.length === 0 ? (
          <div className="accounts-empty">
            <span>✧</span>
            <p>Aucune transaction enregistrée.</p>
          </div>
        ) : (
          <div className="accounts-table-container">
            <table className="accounts-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Libellé</th>
                  <th>Catégorie</th>
                  <th>Montant</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map(transaction => (
                  <tr key={transaction.id}>
                    <td>{formaterDate(transaction.date)}</td>
                    <td className="accounts-transaction-label">
                      {transaction.libelle}
                    </td>
                    <td>
                      <span className="accounts-type-tag">
                        {TYPES_LIBELLES[transaction.type] ?? transaction.type}
                      </span>
                    </td>
                    <td
                      className={
                        transaction.montantBronze >= 0
                          ? 'accounts-amount-positive'
                          : 'accounts-amount-negative'
                      }
                    >
                      {transaction.montantBronze > 0 ? '+' : ''}
                      {formaterBronze(transaction.montantBronze)}
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

export default Accounts