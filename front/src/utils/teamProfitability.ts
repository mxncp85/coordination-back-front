export interface TeamMember {
  id: number;
  name: string;
  /** Tarif journalier actuel (J_actuel), en pièces d'or. */
  dailyRate: number;
}

/** Seuil au-delà duquel la mission n'est pas rentable (ratio en %). */
export const PROFITABILITY_THRESHOLD = 100;

/** Coût = somme des (J_actuel × durée de la quête) des membres. */
export function computeTeamCost(members: TeamMember[], durationDays: number): number {
  const total = members.reduce((sum, m) => sum + m.dailyRate * durationDays, 0);
  // Arrondi au centime pour éviter les artefacts de virgule flottante.
  return Math.round(total * 100) / 100;
}

/**
 * Ratio = coût / prime, en pourcentage.
 * Retourne null si la prime est nulle ou invalide (pas de division par zéro).
 */
export function computeCostRatio(cost: number, bounty: number): number | null {
  if (!Number.isFinite(bounty) || bounty <= 0) return null;
  return (cost / bounty) * 100;
}

export function isUnprofitable(ratio: number | null): boolean {
  return ratio !== null && ratio > PROFITABILITY_THRESHOLD;
}

const percentFormatter = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});

const goldFormatter = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/** Ex. 33.407 → « 33,4 % » */
export function formatRatio(ratio: number): string {
  return `${percentFormatter.format(ratio)} %`;
}

/** Ex. 501.1 → « 501,10 PO » */
export function formatGold(amount: number): string {
  return `${goldFormatter.format(amount)} PO`;
}
