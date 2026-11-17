package fr.capuches.dopale.schedule;

import java.util.List;
import java.util.UUID;

import fr.capuches.dopale.domain.AvailabilityStatus;

public record AdventurerAvailability(
		UUID id,
		String name,
		boolean available,
		AvailabilityStatus status,
		List<CalendarEntry> conflicts) {
}
