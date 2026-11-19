
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import './Equipment.css'

type Statut = 'disponible' | 'emprunté' | 'en réparation'

interface Equipement {
  id: number
  nom: string
  categorie: string
  durabilite: number
  durabiliteMax: number
  prixAchat: number
  coutReparation: number
  statut: Statut
}

interface Transaction {
  id: number
  description: string
  montant: number
  type: 'achat'
  date: string
  equipementId: number
}

function Equipment() {
  const [equipements, setEquipements] = useState<Equipement[]>(() => {
    const saved = localStorage.getItem('equipements')
    return saved ? JSON.parse(saved) as Equipement[] : []
  })

  const [transactions, setTransactions] = useState<Transaction[]>(() => {
    const saved = localStorage.getItem('transactions')
    return saved ? JSON.parse(saved) as Transaction[] : []
  })

  const [nom, setNom] = useState('')
  const [categorie, setCategorie] = useState('')
  const [durabiliteMax, setDurabiliteMax] = useState(100)
  const [prixAchat, setPrixAchat] = useState(0)
  const [coutReparation, setCoutReparation] = useState(0)
  const [achete, setAchete] = useState(false)

  const [recherche, setRecherche] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)

  // Sauvegarde locale
  useEffect(() => {
    localStorage.setItem('equipements', JSON.stringify(equipements))
  }, [equipements])

  useEffect(() => {
    localStorage.setItem('transactions', JSON.stringify(transactions))
  }, [transactions])

  const resetForm = () => {
    setNom('')
    setCategorie('')
    setDurabiliteMax(100)
    setPrixAchat(0)
    setCoutReparation(0)
    setAchete(false)
    setEditingId(null)
  }

  // Création ou modification
  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (
      !nom.trim() ||
      !categorie.trim() ||
      durabiliteMax <= 0 ||
      prixAchat < 0 ||
      coutReparation < 0
    ) {
      return
    }

    if (editingId !== null) {
      // Modification : aucune nouvelle transaction d'achat
      setEquipements(prev =>
        prev.map(item =>
          item.id === editingId
            ? {
                ...item,
                nom: nom.trim(),
                categorie: categorie.trim(),
                durabiliteMax,
                prixAchat,
                coutReparation,
                durabilite: Math.min(item.durabilite, durabiliteMax)
              }
            : item
        )
      )
    } else {
      const nouvelId = Date.now()

      const nouvelEquipement: Equipement = {
        id: nouvelId,
        nom: nom.trim(),
        categorie: categorie.trim(),
        durabilite: durabiliteMax,
        durabiliteMax,
        prixAchat,
        coutReparation,
        statut: 'disponible'
      }

      setEquipements(prev => [...prev, nouvelEquipement])

      // Une transaction négative uniquement si acheté est coché
      if (achete) {
        const nouvelleTransaction: Transaction = {
          id: Date.now() + 1,
          description: `Achat : ${nom.trim()}`,
          montant: -prixAchat,
          type: 'achat',
          date: new Date().toISOString(),
          equipementId: nouvelId
        }

        setTransactions(prev => [...prev, nouvelleTransaction])
      }
    }

    resetForm()
  }

  // Préparer la modification
  const handleEdit = (item: Equipement) => {
    setEditingId(item.id)
    setNom(item.nom)
    setCategorie(item.categorie)
    setDurabiliteMax(item.durabiliteMax)
    setPrixAchat(item.prixAchat)
    setCoutReparation(item.coutReparation)
    setAchete(false)
  }

  // Retirer un équipement de l'inventaire
  const handleDelete = (id: number) => {
    const confirmation = window.confirm(
      'Voulez-vous vraiment retirer cet équipement de l’inventaire ?'
    )

    if (!confirmation) return

    setEquipements(prev => prev.filter(item => item.id !== id))

    if (editingId === id) {
      resetForm()
    }

    // On conserve les transactions pour garder l'historique financier.
  }

  // Recherche
  const equipementsFiltres = equipements.filter(item =>
    `${item.nom} ${item.categorie}`
      .toLowerCase()
      .includes(recherche.toLowerCase())
  )

  // Statistiques
  const totalEquipements = equipements.length

  const depensesAchats = transactions.reduce(
    (total, transaction) => total + transaction.montant,
    0
  )

  return (
    <main className="equipment-page">

      <header className="equipment-header">
        <h1>Gestion des équipements</h1>
        <p>Gérez le matériel et les ressources de votre guilde.</p>
      </header>

      {/* Statistiques */}
      <section className="equipment-stats">

        <div className="stat-card">
          <h3>Équipements</h3>
          <p>{totalEquipements}</p>
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

      {/* Formulaire */}
      <section className="equipment-form-section">

        <h2>
          {editingId !== null
            ? 'Modifier un équipement'
            : 'Ajouter un équipement'}
        </h2>

        <form onSubmit={handleSubmit} className="equipment-form">

          <div className="form-group">
            <label htmlFor="nom">Nom</label>
            <input
              id="nom"
              type="text"
              value={nom}
              onChange={e => setNom(e.target.value)}
              placeholder="Ex : Épée en acier"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="categorie">Catégorie</label>
            <input
              id="categorie"
              type="text"
              value={categorie}
              onChange={e => setCategorie(e.target.value)}
              placeholder="Ex : Arme, Armure, Potion..."
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="durabilite">
              Durabilité maximale (%)
            </label>
            <input
              id="durabilite"
              type="number"
              min="1"
              max="100"
              value={durabiliteMax}
              onChange={e => setDurabiliteMax(Number(e.target.value))}
              required
            />
            <small>
              La durabilité actuelle sera initialisée à cette valeur.
            </small>
          </div>

          <div className="form-group">
            <label htmlFor="prixAchat">Prix d'achat (€)</label>
            <input
              id="prixAchat"
              type="number"
              min="0"
              step="0.01"
              value={prixAchat}
              onChange={e => setPrixAchat(Number(e.target.value))}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="coutReparation">
              Coût de réparation (€)
            </label>
            <input
              id="coutReparation"
              type="number"
              min="0"
              step="0.01"
              value={coutReparation}
              onChange={e => setCoutReparation(Number(e.target.value))}
              required
            />
          </div>

          {editingId === null && (
            <div className="form-group checkbox-group">
              <label>
                <input
                  type="checkbox"
                  checked={achete}
                  onChange={e => setAchete(e.target.checked)}
                />
                Équipement acheté
              </label>
              <small>
                Crée automatiquement une transaction négative.
              </small>
            </div>
          )}

          <div className="equipment-form-actions">

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

      {/* Inventaire */}
      <section className="equipment-list-section">

        <h2>Inventaire des équipements</h2>

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
                  <th>Durabilité</th>
                  <th>Prix d'achat</th>
                  <th>Réparation</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {equipementsFiltres.map(item => (

                  <tr key={item.id}>

                    <td>{item.nom}</td>
                    <td>{item.categorie}</td>

                    <td>
                      {item.durabilite} / {item.durabiliteMax}
                    </td>

                    <td>{item.prixAchat.toFixed(2)} €</td>
                    <td>{item.coutReparation.toFixed(2)} €</td>

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
                      <div className="equipment-actions">

                        <button
                          type="button"
                          className="equipment-edit-button"
                          onClick={() => handleEdit(item)}
                        >
                          Modifier
                        </button>

                        <button
                          type="button"
                          className="equipment-delete-button"
                          onClick={() => handleDelete(item.id)}
                        >
                          Retirer
                        </button>

                      </div>
                    </td>

                  </tr>

                ))}
              </tbody>

            </table>

          </div>
        )}

      </section>

      {/* Historique financier */}
      <section className="equipment-list-section">

        <h2>Historique des achats</h2>

        <p>
          Total des dépenses d'achat :{' '}
          <strong>{depensesAchats.toFixed(2)} €</strong>
        </p>

        {transactions.length === 0 ? (
          <p className="empty-message">
            Aucune transaction enregistrée.
          </p>
        ) : (

          <div className="equipment-table-container">

            <table className="equipment-table">
              <thead>
                <tr>
                  <th>Description</th>
                  <th>Date</th>
                  <th>Montant</th>
                </tr>
              </thead>

              <tbody>
                {transactions.map(transaction => (
                  <tr key={transaction.id}>
                    <td>{transaction.description}</td>
                    <td>
                      {new Date(transaction.date).toLocaleDateString('fr-FR')}
                    </td>
                    <td className="expense-amount">
                      {transaction.montant.toFixed(2)} €
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