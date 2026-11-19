import {
  computeCostRatio,
  computeTeamCost,
  formatGold,
  formatRatio,
  isUnprofitable,
  type TeamMember,
} from '../utils/teamProfitability';
import '../styles/team.css';

interface TeamProfitabilityIndicatorProps {
  members: TeamMember[];
  durationDays: number;
  bounty: number;
}

export function TeamProfitabilityIndicator({
  members,
  durationDays,
  bounty,
}: TeamProfitabilityIndicatorProps) {
  const cost = computeTeamCost(members, durationDays);
  const ratio = computeCostRatio(cost, bounty);
  const unprofitable = isUnprofitable(ratio);

  return (
    <section
      className={`profitability${unprofitable ? ' profitability--alert' : ''}`}
      aria-live="polite"
    >
      <h3 className="profitability__title">Rentabilité de la mission</h3>

      <dl className="profitability__figures">
        <div>
          <dt>Coût de l'équipe</dt>
          <dd>{formatGold(cost)}</dd>
        </div>
        <div>
          <dt>Prime promise</dt>
          <dd>{formatGold(bounty)}</dd>
        </div>
        <div>
          <dt>Ratio coût / prime</dt>
          <dd className="profitability__ratio" data-testid="cost-ratio">
            {ratio === null ? '—' : formatRatio(ratio)}
          </dd>
        </div>
      </dl>

      {ratio === null && (
        <p className="profitability__note">
          Renseignez une prime supérieure à 0 pour calculer le ratio.
        </p>
      )}

      {unprofitable && (
        <p className="profitability__warning" role="alert">
          ⚠ Mission non rentable : le coût dépasse la prime.
        </p>
      )}
    </section>
  );
}
