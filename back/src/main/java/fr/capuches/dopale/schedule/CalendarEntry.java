package fr.capuches.dopale.schedule;

import java.time.LocalDate;
import java.util.UUID;

import fr.capuches.dopale.domain.MissionPhase;

public record CalendarEntry(
		CalendarEntryType type,
		UUID id,
		String label,
		LocalDate startDate,
		LocalDate endDate,
		MissionPhase missionPhase) {
}
