package fr.capuches.dopale.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Mission assignée à un aventurier. L'aventurier est indisponible pendant toute la période.
 * Le repos suivant (US-21) est modélisé à part ({@link RestPeriod}).
 */
public record Mission(
		UUID id,
		UUID adventurerId,
		String title,
		LocalDate startDate,
		LocalDate endDate) {

	public Mission {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(adventurerId, "adventurerId");
		Objects.requireNonNull(title, "title");
		Objects.requireNonNull(startDate, "startDate");
		Objects.requireNonNull(endDate, "endDate");
		if (endDate.isBefore(startDate)) {
			throw new IllegalArgumentException("La fin de mission doit être après ou égale au début");
		}
	}

	public DateRange period() {
		return new DateRange(startDate, endDate);
	}

	public MissionPhase phaseOn(LocalDate day) {
		if (day.isBefore(startDate)) {
			return MissionPhase.PLANNED;
		}
		if (day.isAfter(endDate)) {
			return MissionPhase.COMPLETED;
		}
		return MissionPhase.IN_PROGRESS;
	}
}
