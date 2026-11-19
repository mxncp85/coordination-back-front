import { useState } from 'react';
import { AvailabilitySearchPanel } from '../components/AvailabilitySearchPanel';
import { AdventurerScheduleCard } from '../components/AdventurerScheduleCard';
import '../styles/schedule.css';

export function AdventurerSchedulePage() {
  const [selectedAdventurerId, setSelectedAdventurerId] = useState<number>(1);

  return (
    <main className="schedule-page">
      <h1 className="schedule-page__title">
        Gestion des emplois du temps de la Guilde
      </h1>

      <AvailabilitySearchPanel
        onSelectAdventurer={(id) => setSelectedAdventurerId(id)}
      />

      <AdventurerScheduleCard adventurerId={selectedAdventurerId} />
    </main>
  );
}