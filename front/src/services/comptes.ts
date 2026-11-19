import { apiRequest } from './api'

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
  requeteId?: number | null
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

export function getSolde() {
  return apiRequest<SoldeCompte>('/api/comptes/solde')
}

export function getTransactions(page = 0, size = 20) {
  return apiRequest<Page<Transaction>>(
    `/api/comptes/transactions?page=${page}&size=${size}`
  )
}

export function createTransaction(data: CreateTransactionInput) {
  return apiRequest<Transaction>('/api/comptes/transactions', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}