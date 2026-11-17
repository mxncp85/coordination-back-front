package fr.capuches.dopale.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Repos obligatoire après une mission (US-21).
 * L'aventurier est indisponible pendant toute cette période.
 */
public record RestPeriod(
		UUID id,
		UUID adventurerId,
		UUID missionId,
		LocalDate startDate,
		LocalDate endDate) {

	public RestPeriod {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(adventurerId, "adventurerId");
		Objects.requireNonNull(missionId, "missionId");
		Objects.requireNonNull(startDate, "startDate");
		Objects.requireNonNull(endDate, "endDate");
		if (endDate.isBefore(startDate)) {
			throw new IllegalArgumentException("La fin de repos doit être après ou égale au début");
		}
	}

	public DateRange period() {
		return new DateRange(startDate, endDate);
	}

	/**
	 * Repos qui démarre le lendemain de la fin de mission, pendant {@code restDays} jours entiers.
	 */
	public static RestPeriod afterMission(Mission mission, int restDays) {
		if (restDays < 1) {
			throw new IllegalArgumentException("Le repos doit durer au moins 1 jour");
		}
		LocalDate start = mission.endDate().plusDays(1);
		LocalDate end = start.plusDays(restDays - 1L);
		return new RestPeriod(UUID.randomUUID(), mission.adventurerId(), mission.id(), start, end);
	}
}
