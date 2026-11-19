import { apiRequest } from './api'
import type { Equipement } from '../types/models'

export interface EquipementPayload {
  nom: string
  type: string
  durabiliteMax: number
  prixAchatBronze: number
}

export function getEquipements() {
  return apiRequest<Equipement[]>('/api/equipements')
}

export function purchaseEquipement(payload: EquipementPayload) {
  return apiRequest<Equipement>('/api/equipements', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function repairEquipement(id: number) {
  return apiRequest<Equipement>(
    `/api/equipements/${id}/reparation`,
    { method: 'POST' }
  )
}
