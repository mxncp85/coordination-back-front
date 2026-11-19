export type SensTransaction = 'CREDIT' | 'DEBIT'

export type TypeTransaction =
  | 'ACOMPTE'
  | 'SOLDE'
  | 'PAIEMENT_AVENTURIER'
  | 'ACHAT'
  | 'MANUEL'

export interface SoldeCompte {
  soldeBronze: number
}

export interface Transaction {
  id: number
  date: string
  montantBronze: number
  libelle: string
  type: TypeTransaction
  requeteId: number | null
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface CreateTransactionInput {
  montantBronze: number
  sens: SensTransaction
  libelle: string
}
