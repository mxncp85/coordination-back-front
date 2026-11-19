import { describe, expect, it } from 'vitest';
import { computeCostRatio, computeTeamCost, formatRatio, isUnprofitable } from './teamProfitability';

const team = [
  { id: 1, name: 'Aria', dailyRate: 40.1, experience: 150 },
  { id: 2, name: 'Bor', dailyRate: 35, experience: 80 },
  { id: 3, name: 'Cael', dailyRate: 25.12, experience: 100 },
];

describe('ratio coût / prime', () => {
  it('reproduit l’exemple : 501,10 PO pour 1 500 PO → 33,4 %', () => {
    const cost = computeTeamCost(team, 5);
    expect(cost).toBe(501.1);
    expect(formatRatio(computeCostRatio(cost, 1500)!)).toBe('33,4 %');
  });

  it('ne divise pas par zéro quand la prime vaut 0', () => {
    expect(computeCostRatio(501.1, 0)).toBeNull();
  });

  it('alerte uniquement au-delà de 100 %', () => {
    expect(isUnprofitable(computeCostRatio(2000, 1500))).toBe(true);
    expect(isUnprofitable(computeCostRatio(1500, 1500))).toBe(false);
    expect(isUnprofitable(null)).toBe(false);
  });
});
