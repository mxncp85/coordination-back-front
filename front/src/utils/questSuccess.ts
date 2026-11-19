/** Coefficient de prudence appliqué à la moyenne des chances individuelles. */
export const QUEST_SUCCESS_FACTOR = 0.8;

export interface ExperiencedMember {
  /** Expérience réelle du membre (Exp_r). */
  experience: number;
}

/**
 * Chance individuelle Ti = min(Exp_r, Exp_rec) / Exp_rec (formule corrigée, cf. Q1 du ticket #47).
 * Plafonnée à 1 : un membre plus expérimenté que requis n'augmente pas la chance au-delà de 100 %.
 * Retourne null si l'expérience recommandée est nulle ou invalide.
 */
export function computeIndividualChance(experience: number, recommendedExperience: number): number | null {
  if (!Number.isFinite(recommendedExperience) || recommendedExperience <= 0) return null;
  const exp = Number.isFinite(experience) ? Math.max(0, experience) : 0;
  return Math.min(exp, recommendedExperience) / recommendedExperience;
}

/**
 * Chance de réussite de la quête Tq = (Σ Ti / n) × 0,8, entre 0 et 1.
 * Retourne null pour une équipe vide ou une expérience recommandée invalide.
 */
export function computeQuestSuccess(
  members: ExperiencedMember[],
  recommendedExperience: number,
): number | null {
  if (members.length === 0) return null;
  let sum = 0;
  for (const m of members) {
    const ti = computeIndividualChance(m.experience, recommendedExperience);
    if (ti === null) return null;
    sum += ti;
  }
  return (sum / members.length) * QUEST_SUCCESS_FACTOR;
}

const percentFormatter = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});

/** Ex. 0.74666 → « 74,7 % » ; null → « — » */
export function formatChance(chance: number | null): string {
  return chance === null ? '—' : `${percentFormatter.format(chance * 100)} %`;
}
