import { apiRequest } from './api'
import type { Aventurier } from '../types/models'

export interface AventurierPayload {
  nom: string
  specialites: string[]
  experience: number
  tauxBaseBronze: number
}

export function getAventuriers() {
  return apiRequest<Aventurier[]>('/api/aventuriers')
}

export function createAventurier(payload: AventurierPayload) {
  return apiRequest<Aventurier>('/api/aventuriers', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function deleteAventurier(id: number) {
  return apiRequest<void>(`/api/aventuriers/${id}`, {
    method: 'DELETE'
  })
}
