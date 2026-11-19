import { useState, type FormEvent } from 'react';
import { scheduleApi, type AvailableAdventurer } from '../services/scheduleApi';
import '../styles/schedule.css';

interface AvailabilitySearchPanelProps {
  onSelectAdventurer?: (adventurerId: number) => void;
}

export function AvailabilitySearchPanel({
  onSelectAdventurer,
}: AvailabilitySearchPanelProps) {
  const todayIso = new Date().toISOString().split('T')[0];
  const [from, setFrom] = useState<string>(todayIso);
  const [to, setTo] = useState<string>(todayIso);
  const [results, setResults] = useState<AvailableAdventurer[] | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (from > to) {
      setError('La date de début doit être antérieure à la date de fin.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const data = await scheduleApi.getAvailableAdventurers(from, to);
      setResults(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur lors de la recherche');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="schedule-card">
      <h3 className="schedule-card__name">Vérifier les disponibilités sur une période</h3>

      <form onSubmit={handleSearch} className="schedule-controls__dates">
        <div className="schedule-controls__field">
          <label htmlFor="avail-from">Début de mission</label>
          <input
            id="avail-from"
            type="date"
            required
            value={from}
            onChange={(e) => setFrom(e.target.value)}
          />
        </div>

        <div className="schedule-controls__field">
          <label htmlFor="avail-to">Fin (repos inclus)</label>
          <input
            id="avail-to"
            type="date"
            required
            min={from}
            value={to}
            onChange={(e) => setTo(e.target.value)}
          />
        </div>

        <button type="submit" disabled={loading} className="schedule-controls__btn">
          {loading ? 'Recherche...' : 'Trouver les disponibles'}
        </button>
      </form>

      {error && (
        <p className="feedback-banner feedback-banner--error" role="alert">
          {error}
        </p>
      )}

      {results !== null && (
        <div>
          {results.length === 0 ? (
            <p className="schedule-card__subtitle">
              Aucun aventurier disponible sur cette période.
            </p>
          ) : (
            <ul className="availability-list">
              {results.map((adv) => (
                <li key={adv.id} className="availability-list__item">
                  <div>
                    <p className="availability-list__name">{adv.name}</p>
                    {adv.specialty && (
                      <p className="availability-list__meta">{adv.specialty}</p>
                    )}
                  </div>
                  {onSelectAdventurer && (
                    <button
                      type="button"
                      onClick={() => onSelectAdventurer(adv.id)}
                      className="availability-list__action"
                    >
                      Voir le planning →
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </section>
  );
}