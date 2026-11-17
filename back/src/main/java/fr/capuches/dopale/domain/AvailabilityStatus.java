package fr.capuches.dopale.domain;

import java.time.LocalDate;

/**
 * Statut courant d'un aventurier : disponible, en mission jusqu'au…, ou au repos jusqu'au…
 */
public record AvailabilityStatus(AvailabilityState state, LocalDate until) {

	public static AvailabilityStatus available() {
		return new AvailabilityStatus(AvailabilityState.AVAILABLE, null);
	}

	public static AvailabilityStatus onMissionUntil(LocalDate until) {
		return new AvailabilityStatus(AvailabilityState.ON_MISSION, until);
	}

	public static AvailabilityStatus restingUntil(LocalDate until) {
		return new AvailabilityStatus(AvailabilityState.RESTING, until);
	}
}
