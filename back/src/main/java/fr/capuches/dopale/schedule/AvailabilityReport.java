package fr.capuches.dopale.schedule;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityReport(
		LocalDate from,
		LocalDate to,
		List<AdventurerAvailability> adventurers) {
}
