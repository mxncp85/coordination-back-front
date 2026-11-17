package fr.capuches.dopale.web;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.capuches.dopale.schedule.AdventurerSchedule;
import fr.capuches.dopale.schedule.AvailabilityReport;
import fr.capuches.dopale.schedule.ScheduleService;

@RestController
@RequestMapping("/api/adventurers")
public class AdventurerController {

	private final ScheduleService scheduleService;

	public AdventurerController(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	/**
	 * Emploi du temps d'un aventurier : statut, missions (prévues / en cours), repos, calendrier.
	 */
	@GetMapping("/{id}/schedule")
	public AdventurerSchedule schedule(
			@PathVariable UUID id,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return scheduleService.getSchedule(id, from, to);
	}

	/**
	 * Qui est disponible sur une période [from, to] (jours entiers, inclusifs).
	 * Un aventurier est indisponible s'il a une mission ou un repos qui chevauche la période.
	 */
	@GetMapping("/availability")
	public AvailabilityReport availability(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return scheduleService.findAvailability(from, to);
	}
}
