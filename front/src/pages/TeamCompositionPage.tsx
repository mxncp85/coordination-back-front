import { useState, type FormEvent } from 'react';
import { QuestSuccessIndicator } from '../components/QuestSuccessIndicator';
import { TeamProfitabilityIndicator } from '../components/TeamProfitabilityIndicator';
import { formatGold, type TeamMember } from '../utils/teamProfitability';
import '../styles/team.css';

// Données locales en attendant l'API équipe / mission côté back.
const INITIAL_MEMBERS: TeamMember[] = [
  { id: 1, name: 'Aria', dailyRate: 40.1, experience: 150 },
  { id: 2, name: 'Bor', dailyRate: 35, experience: 80 },
  { id: 3, name: 'Cael', dailyRate: 25.12, experience: 100 },
];

function toNumber(value: string): number {
  const n = Number.parseFloat(value.replace(',', '.'));
  return Number.isFinite(n) && n >= 0 ? n : 0;
}

export function TeamCompositionPage() {
  const [members, setMembers] = useState<TeamMember[]>(INITIAL_MEMBERS);
  const [durationDays, setDurationDays] = useState('5');
  const [bounty, setBounty] = useState('1500');
  const [recommendedExperience, setRecommendedExperience] = useState('100');
  const [newName, setNewName] = useState('');
  const [newRate, setNewRate] = useState('');
  const [newExperience, setNewExperience] = useState('');

  const handleAdd = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const name = newName.trim();
    if (!name) return;
    setMembers((prev) => [
      ...prev,
      {
        id: Math.max(0, ...prev.map((m) => m.id)) + 1,
        name,
        dailyRate: toNumber(newRate),
        experience: toNumber(newExperience),
      },
    ]);
    setNewName('');
    setNewRate('');
    setNewExperience('');
  };

  const handleRemove = (id: number) => {
    setMembers((prev) => prev.filter((m) => m.id !== id));
  };

  return (
    <div className="team-page">
      <section className="team-card">
        <h2 className="team-card__title">Composition de l'équipe</h2>

        <div className="team-form__row">
          <label className="team-form__field">
            Durée de la quête (jours)
            <input
              type="number"
              min="0"
              step="1"
              value={durationDays}
              onChange={(e) => setDurationDays(e.target.value)}
            />
          </label>
          <label className="team-form__field">
            Prime promise (PO)
            <input
              type="number"
              min="0"
              step="0.01"
              value={bounty}
              onChange={(e) => setBounty(e.target.value)}
            />
          </label>
          <label className="team-form__field">
            Expérience recommandée
            <input
              type="number"
              min="0"
              step="1"
              value={recommendedExperience}
              onChange={(e) => setRecommendedExperience(e.target.value)}
            />
          </label>
        </div>

        {members.length === 0 ? (
          <p className="team-card__empty">Aucun membre dans l'équipe.</p>
        ) : (
          <ul className="team-list">
            {members.map((m) => (
              <li key={m.id} className="team-list__item">
                <span>{m.name}</span>
                <span className="team-list__rate">{m.experience} XP</span>
                <span className="team-list__rate">{formatGold(m.dailyRate)} / jour</span>
                <button
                  type="button"
                  className="team-list__remove"
                  onClick={() => handleRemove(m.id)}
                  aria-label={`Retirer ${m.name}`}
                >
                  Retirer
                </button>
              </li>
            ))}
          </ul>
        )}

        <form onSubmit={handleAdd} className="team-form__row">
          <label className="team-form__field">
            Nom
            <input value={newName} onChange={(e) => setNewName(e.target.value)} required />
          </label>
          <label className="team-form__field">
            Tarif journalier (PO)
            <input
              type="number"
              min="0"
              step="0.01"
              value={newRate}
              onChange={(e) => setNewRate(e.target.value)}
              required
            />
          </label>
          <label className="team-form__field">
            Expérience
            <input
              type="number"
              min="0"
              step="1"
              value={newExperience}
              onChange={(e) => setNewExperience(e.target.value)}
              required
            />
          </label>
          <button type="submit" className="team-form__btn">Ajouter</button>
        </form>
      </section>

      <QuestSuccessIndicator
        members={members}
        recommendedExperience={toNumber(recommendedExperience)}
      />

      <TeamProfitabilityIndicator
        members={members}
        durationDays={toNumber(durationDays)}
        bounty={toNumber(bounty)}
      />
    </div>
  );
}
