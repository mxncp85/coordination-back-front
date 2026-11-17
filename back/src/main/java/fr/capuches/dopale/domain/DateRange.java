package fr.capuches.dopale.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Période inclusive en jours entiers [start, end].
 */
public record DateRange(LocalDate start, LocalDate end) {

	public DateRange {
		Objects.requireNonNull(start, "start");
		Objects.requireNonNull(end, "end");
		if (end.isBefore(start)) {
			throw new IllegalArgumentException("La fin doit être après ou égale au début");
		}
	}

	public boolean contains(LocalDate day) {
		return !day.isBefore(start) && !day.isAfter(end);
	}

	public boolean overlaps(DateRange other) {
		return !start.isAfter(other.end) && !other.start.isAfter(end);
	}
}
