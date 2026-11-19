import {
  computeIndividualChance,
  computeQuestSuccess,
  formatChance,
} from '../utils/questSuccess';
import type { TeamMember } from '../utils/teamProfitability';
import '../styles/team.css';

interface QuestSuccessIndicatorProps {
  members: TeamMember[];
  recommendedExperience: number;
}

export function QuestSuccessIndicator({ members, recommendedExperience }: QuestSuccessIndicatorProps) {
  const chance = computeQuestSuccess(members, recommendedExperience);
  const invalidRecommended = members.length > 0 && chance === null;

  return (
    <section className="profitability" aria-live="polite">
      <h3 className="profitability__title">Chance de réussite de la quête</h3>

      <p className="quest-success__value" data-testid="quest-success">
        {formatChance(chance)}
      </p>

      {members.length === 0 && (
        <p className="profitability__note">Ajoutez des membres pour estimer la chance de réussite.</p>
      )}
      {invalidRecommended && (
        <p className="profitability__note">
          Renseignez une expérience recommandée supérieure à 0.
        </p>
      )}

      {chance !== null && (
        <ul className="quest-success__members">
          {members.map((m) => (
            <li key={m.id}>
              <span>{m.name}</span>
              <span>Ti = {formatChance(computeIndividualChance(m.experience, recommendedExperience))}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
