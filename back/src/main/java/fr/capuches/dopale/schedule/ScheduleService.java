package fr.capuches.dopale.schedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import fr.capuches.dopale.domain.Adventurer;
import fr.capuches.dopale.domain.AvailabilityStatus;
import fr.capuches.dopale.domain.DateRange;
import fr.capuches.dopale.domain.Mission;
import fr.capuches.dopale.domain.RestPeriod;
import fr.capuches.dopale.repository.AdventurerRepository;

@Service
public class ScheduleService {

	private final AdventurerRepository repository;

	public ScheduleService(AdventurerRepository repository) {
		this.repository = repository;
	}

	public AdventurerSchedule getSchedule(UUID adventurerId, LocalDate from, LocalDate to) {
		Adventurer adventurer = requireAdventurer(adventurerId);
		LocalDate today = LocalDate.now();
		List<Mission> missions = repository.findMissionsByAdventurerId(adventurerId).stream()
				.sorted(Comparator.comparing(Mission::startDate))
				.toList();
		List<RestPeriod> rests = repository.findRestPeriodsByAdventurerId(adventurerId).stream()
				.sorted(Comparator.comparing(RestPeriod::startDate))
				.toList();

		DateRange window = resolveWindow(from, to, missions, rests, today);
		List<CalendarEntry> calendar = buildCalendar(missions, rests, window, today);

		return new AdventurerSchedule(
				adventurer.id(),
				adventurer.name(),
				resolveStatus(missions, rests, today),
				missions,
				rests,
				calendar);
	}

	public AvailabilityReport findAvailability(LocalDate from, LocalDate to) {
		if (from == null || to == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les paramètres from et to sont obligatoires");
		}
		DateRange requested = new DateRange(from, to);
		LocalDate today = LocalDate.now();

		List<AdventurerAvailability> results = repository.findAll().stream()
				.map(adventurer -> toAvailability(adventurer, requested, today))
				.toList();

		return new AvailabilityReport(from, to, results);
	}

	private AdventurerAvailability toAvailability(Adventurer adventurer, DateRange requested, LocalDate today) {
		List<Mission> missions = repository.findMissionsByAdventurerId(adventurer.id());
		List<RestPeriod> rests = repository.findRestPeriodsByAdventurerId(adventurer.id());
		List<CalendarEntry> conflicts = overlappingEntries(missions, rests, requested, today);
		boolean available = conflicts.isEmpty();

		return new AdventurerAvailability(
				adventurer.id(),
				adventurer.name(),
				available,
				resolveStatus(missions, rests, today),
				conflicts);
	}

	AvailabilityStatus resolveStatus(List<Mission> missions, List<RestPeriod> rests, LocalDate today) {
		for (Mission mission : missions) {
			if (mission.period().contains(today)) {
				return AvailabilityStatus.onMissionUntil(mission.endDate());
			}
		}
		for (RestPeriod rest : rests) {
			if (rest.period().contains(today)) {
				return AvailabilityStatus.restingUntil(rest.endDate());
			}
		}
		return AvailabilityStatus.available();
	}

	private List<CalendarEntry> overlappingEntries(
			List<Mission> missions,
			List<RestPeriod> rests,
			DateRange window,
			LocalDate today) {
		List<CalendarEntry> entries = new ArrayList<>();
		for (Mission mission : missions) {
			if (mission.period().overlaps(window)) {
				entries.add(toMissionEntry(mission, today));
			}
		}
		for (RestPeriod rest : rests) {
			if (rest.period().overlaps(window)) {
				entries.add(toRestEntry(rest));
			}
		}
		entries.sort(Comparator.comparing(CalendarEntry::startDate).thenComparing(CalendarEntry::type));
		return List.copyOf(entries);
	}

	private List<CalendarEntry> buildCalendar(
			List<Mission> missions,
			List<RestPeriod> rests,
			DateRange window,
			LocalDate today) {
		return overlappingEntries(missions, rests, window, today);
	}

	private CalendarEntry toMissionEntry(Mission mission, LocalDate today) {
		return new CalendarEntry(
				CalendarEntryType.MISSION,
				mission.id(),
				mission.title(),
				mission.startDate(),
				mission.endDate(),
				mission.phaseOn(today));
	}

	private CalendarEntry toRestEntry(RestPeriod rest) {
		return new CalendarEntry(
				CalendarEntryType.REST,
				rest.id(),
				"Repos",
				rest.startDate(),
				rest.endDate(),
				null);
	}

	private DateRange resolveWindow(
			LocalDate from,
			LocalDate to,
			List<Mission> missions,
			List<RestPeriod> rests,
			LocalDate today) {
		if (from != null && to != null) {
			return new DateRange(from, to);
		}
		if (from != null) {
			LocalDate end = to != null ? to : from.plusMonths(1);
			return new DateRange(from, end);
		}
		if (to != null) {
			return new DateRange(to.minusMonths(1), to);
		}

		LocalDate min = today;
		LocalDate max = today.plusMonths(1);
		for (Mission mission : missions) {
			if (mission.startDate().isBefore(min)) {
				min = mission.startDate();
			}
			if (mission.endDate().isAfter(max)) {
				max = mission.endDate();
			}
		}
		for (RestPeriod rest : rests) {
			if (rest.startDate().isBefore(min)) {
				min = rest.startDate();
			}
			if (rest.endDate().isAfter(max)) {
				max = rest.endDate();
			}
		}
		return new DateRange(min, max);
	}

	private Adventurer requireAdventurer(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Aventurier introuvable : " + id));
	}
}
