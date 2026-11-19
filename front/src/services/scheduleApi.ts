export type AvailabilityStatus = 'AVAILABLE' | 'ON_MISSION' | 'RESTING';

export interface ScheduleBlock {
  missionId: number;
  missionTitle: string;
  type: 'MISSION' | 'REST';
  startDate: string; // YYYY-MM-DD
  endDate: string;   // YYYY-MM-DD
}

export interface AdventurerSchedule {
  adventurerId: number;
  adventurerName: string;
  currentStatus: AvailabilityStatus;
  unavailableUntil: string | null; // YYYY-MM-DD
  remainingRestDays?: number;
  blocks: ScheduleBlock[];
}

export interface AvailableAdventurer {
  id: number;
  name: string;
  specialty?: string;
  level?: number;
}

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

export const scheduleApi = {
  async getAdventurerSchedule(
    adventurerId: number,
    from?: string,
    to?: string
  ): Promise<AdventurerSchedule> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);

    const queryString = params.toString() ? `?${params.toString()}` : '';
    const response = await fetch(
      `${API_BASE_URL}/adventurers/${adventurerId}/schedule${queryString}`
    );

    if (!response.ok) {
      throw new Error(`Erreur ${response.status} lors de la récupération du planning.`);
    }
    return response.json();
  },

  async getAvailableAdventurers(
    from: string,
    to: string
  ): Promise<AvailableAdventurer[]> {
    const params = new URLSearchParams({ from, to });
    const response = await fetch(
      `${API_BASE_URL}/adventurers/availability?${params.toString()}`
    );

    if (!response.ok) {
      throw new Error(`Erreur ${response.status} lors de la recherche de disponibilité.`);
    }
    return response.json();
  },
};

export function calculateRemainingDays(endDateIso: string | null): number {
  if (!endDateIso) return 0;
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const end = new Date(`${endDateIso}T00:00:00`);
  const diffTime = end.getTime() - today.getTime();
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1;

  return Math.max(0, diffDays);
}