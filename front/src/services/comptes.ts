import { apiRequest } from './api'
import type {
  SoldeCompte,
  Transaction,
  Page,
  CreateTransactionInput
} from '../types/comptes'

export function getSolde(): Promise<SoldeCompte> {
  return apiRequest<SoldeCompte>('/api/comptes/solde')
}

export function getTransactions(
  page = 0,
  size = 20,
  type?: string
): Promise<Page<Transaction>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size)
  })

  if (type) {
    params.set('type', type)
  }

  return apiRequest<Page<Transaction>>(
    `/api/comptes/transactions?${params.toString()}`
  )
}

export function createTransaction(
  data: CreateTransactionInput
): Promise<Transaction> {
  return apiRequest<Transaction>('/api/comptes/transactions', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}
