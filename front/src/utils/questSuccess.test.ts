import { describe, expect, it } from 'vitest';
import { computeIndividualChance, computeQuestSuccess, formatChance } from './questSuccess';

describe('computeIndividualChance (Ti)', () => {
  it('vaut Exp_r / Exp_rec quand le membre est sous l’expérience recommandée', () => {
    expect(computeIndividualChance(80, 100)).toBe(0.8);
  });

  it('est plafonnée à 1 quand le membre dépasse l’expérience recommandée', () => {
    expect(computeIndividualChance(150, 100)).toBe(1);
  });

  it('vaut 1 quand l’expérience est exactement celle recommandée', () => {
    expect(computeIndividualChance(100, 100)).toBe(1);
  });

  it('vaut 0 pour une expérience nulle ou négative', () => {
    expect(computeIndividualChance(0, 100)).toBe(0);
    expect(computeIndividualChance(-20, 100)).toBe(0);
  });

  it('retourne null si l’expérience recommandée est nulle (pas de division par zéro)', () => {
    expect(computeIndividualChance(50, 0)).toBeNull();
    expect(computeIndividualChance(50, -10)).toBeNull();
  });
});

describe('computeQuestSuccess (Tq)', () => {
  it('reproduit l’exemple du ticket : Aria 150, Bor 80, Cael 100, Exp_rec 100 → 74,7 %', () => {
    const team = [{ experience: 150 }, { experience: 80 }, { experience: 100 }];
    const tq = computeQuestSuccess(team, 100);
    expect(tq).toBeCloseTo(0.7467, 4);
    expect(formatChance(tq)).toBe('74,7 %');
  });

  it('retourne null pour une équipe vide, affichée « — »', () => {
    const tq = computeQuestSuccess([], 100);
    expect(tq).toBeNull();
    expect(formatChance(tq)).toBe('—');
  });

  it('plafonne à 80 % une équipe entièrement qualifiée', () => {
    expect(computeQuestSuccess([{ experience: 200 }, { experience: 100 }], 100)).toBeCloseTo(0.8);
  });

  it('change quand un membre est ajouté ou retiré', () => {
    const team = [{ experience: 100 }];
    const before = computeQuestSuccess(team, 100);
    const after = computeQuestSuccess([...team, { experience: 50 }], 100);
    expect(before).toBeCloseTo(0.8);
    expect(after).toBeCloseTo(0.6);
  });

  it('retourne null si l’expérience recommandée est invalide', () => {
    expect(computeQuestSuccess([{ experience: 100 }], 0)).toBeNull();
  });
});
