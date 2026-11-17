package fr.capuches.dopale.schedule;

import java.util.List;
import java.util.UUID;

import fr.capuches.dopale.domain.AvailabilityStatus;
import fr.capuches.dopale.domain.Mission;
import fr.capuches.dopale.domain.RestPeriod;

public record AdventurerSchedule(
		UUID adventurerId,
		String name,
		AvailabilityStatus status,
		List<Mission> missions,
		List<RestPeriod> restPeriods,
		List<CalendarEntry> calendar) {
}
