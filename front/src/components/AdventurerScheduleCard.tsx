import { useState, useEffect, useMemo } from 'react';
import {
  scheduleApi,
  calculateRemainingDays,
  type AdventurerSchedule,
} from '../services/scheduleApi';
import '../styles/schedule.css';

interface AdventurerScheduleCardProps {
  adventurerId: number;
}

export function AdventurerScheduleCard({ adventurerId }: AdventurerScheduleCardProps) {
  const [fromDate, setFromDate] = useState<string>(() => {
    const d = new Date();
    return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().split('T')[0];
  });
  const [toDate, setToDate] = useState<string>(() => {
    const d = new Date();
    return new Date(d.getFullYear(), d.getMonth() + 1, 0).toISOString().split('T')[0];
  });

  const [schedule, setSchedule] = useState<AdventurerSchedule | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadSchedule = async () => {
      if (fromDate > toDate) {
        setError('La date de début doit être antérieure ou égale à la date de fin.');
        return;
      }

      setLoading(true);
      setError(null);
      try {
        const data = await scheduleApi.getAdventurerSchedule(
          adventurerId,
          fromDate,
          toDate
        );
        if (isMounted) setSchedule(data);
      } catch (err) {
        if (isMounted) {
          setError(err instanceof Error ? err.message : 'Erreur de chargement');
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadSchedule();
    return () => {
      isMounted = false;
    };
  }, [adventurerId, fromDate, toDate]);

  const daysInRange = useMemo(() => {
    if (!fromDate || !toDate || fromDate > toDate) return [];
    const list: string[] = [];
    const cursor = new Date(`${fromDate}T00:00:00`);
    const end = new Date(`${toDate}T00:00:00`);

    while (cursor <= end) {
      list.push(cursor.toISOString().split('T')[0]);
      cursor.setDate(cursor.getDate() + 1);
    }
    return list;
  }, [fromDate, toDate]);

  const formatDate = (iso: string) =>
    new Date(`${iso}T00:00:00`).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    });

  const remainingRestDays = useMemo(() => {
    if (!schedule || schedule.currentStatus !== 'RESTING') return 0;
    return schedule.remainingRestDays ?? calculateRemainingDays(schedule.unavailableUntil);
  }, [schedule]);

  if (loading) {
    return (
      <div className="feedback-banner feedback-banner--loading" role="status">
        Chargement de l'emploi du temps de l'aventurier...
      </div>
    );
  }

  if (error) {
    return (
      <div className="feedback-banner feedback-banner--error" role="alert">
        {error}
      </div>
    );
  }

  if (!schedule) return null;

  return (
    <article className="schedule-card">
      <header className="schedule-card__header">
        <div>
          <h2 className="schedule-card__name">{schedule.adventurerName}</h2>
          <p className="schedule-card__subtitle">Suivi des missions et jours de repos</p>
        </div>

        <div className="schedule-card__status-group">
          {schedule.currentStatus === 'AVAILABLE' && (
            <span className="status-badge status-badge--available">Disponible</span>
          )}

          {schedule.currentStatus === 'ON_MISSION' && (
            <span className="status-badge status-badge--mission">
              En mission jusqu'au{' '}
              {schedule.unavailableUntil ? formatDate(schedule.unavailableUntil) : '—'}
            </span>
          )}

          {schedule.currentStatus === 'RESTING' && (
            <>
              <span className="status-badge status-badge--resting">
                Au repos jusqu'au{' '}
                {schedule.unavailableUntil ? formatDate(schedule.unavailableUntil) : '—'}
              </span>
              <span className="schedule-card__rest-counter">
                Repos restant :{' '}
                <strong>
                  {remainingRestDays} jour{remainingRestDays > 1 ? 's' : ''}
                </strong>
              </span>
            </>
          )}
        </div>
      </header>

      <div className="schedule-controls">
        <div className="schedule-controls__dates">
          <div className="schedule-controls__field">
            <label htmlFor="schedule-from">Du</label>
            <input
              id="schedule-from"
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
            />
          </div>
          <div className="schedule-controls__field">
            <label htmlFor="schedule-to">Au</label>
            <input
              id="schedule-to"
              type="date"
              value={toDate}
              min={fromDate}
              onChange={(e) => setToDate(e.target.value)}
            />
          </div>
        </div>

        <div className="schedule-legend">
          <span className="schedule-legend__item">
            <span className="schedule-legend__dot schedule-legend__dot--available" />
            Disponible
          </span>
          <span className="schedule-legend__item">
            <span className="schedule-legend__dot schedule-legend__dot--mission" />
            En mission
          </span>
          <span className="schedule-legend__item">
            <span className="schedule-legend__dot schedule-legend__dot--resting" />
            Repos
          </span>
        </div>
      </div>

      <div className="calendar-grid">
        {daysInRange.map((dayIso) => {
          const block = schedule.blocks.find(
            (b) => dayIso >= b.startDate && dayIso <= b.endDate
          );

          let modifierClass = 'calendar-cell--available';
          let statusLabel = 'Disponible';

          if (block?.type === 'MISSION') {
            modifierClass = 'calendar-cell--mission';
            statusLabel = block.missionTitle;
          } else if (block?.type === 'REST') {
            modifierClass = 'calendar-cell--resting';
            statusLabel = 'Repos';
          }

          return (
            <div key={dayIso} className={`calendar-cell ${modifierClass}`}>
              <span className="calendar-cell__date">
                {new Date(`${dayIso}T00:00:00`).toLocaleDateString('fr-FR', {
                  weekday: 'short',
                  day: '2-digit',
                  month: '2-digit',
                })}
              </span>
              <span className="calendar-cell__label" title={statusLabel}>
                {statusLabel}
              </span>
            </div>
          );
        })}
      </div>
    </article>
  );
}