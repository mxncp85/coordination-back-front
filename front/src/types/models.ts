export type Specialite =
  | 'ARCHER'
  | 'BARBARE'
  | 'PALADIN'
  | 'MAGE_ARCANIQUE'
  | 'PRETRE'
  | 'GEOMANCIEN'
  | 'ALCHIMISTE'
  | 'FORGERON'
  | 'ENCHANTEUR'
  | 'MESSAGER'
  | 'AUTRE'

export type StatutAventurier =
  | 'DISPONIBLE'
  | 'EN_MISSION'
  | 'EN_REPOS'

export interface Aventurier {
  id: number
  nom: string
  specialites: Specialite[]
  experience: number
  tauxBaseBronze: number
  tauxJournalierBronze: number
  statut: StatutAventurier
}

export type StatutEquipement =
  | 'DISPONIBLE'
  | 'EMPRUNTE'
  | 'EN_REPARATION'

export interface Equipement {
  id: number
  nom: string
  type: string
  durabiliteMax: number
  durabiliteActuelle: number
  prixAchatBronze: number
  statut: StatutEquipement
}
