package fr.capuches.dopale.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.capuches.dopale.domain.Adventurer;
import fr.capuches.dopale.domain.AvailabilityState;
import fr.capuches.dopale.domain.Mission;
import fr.capuches.dopale.domain.MissionPhase;
import fr.capuches.dopale.domain.RestPeriod;
import fr.capuches.dopale.repository.AdventurerRepository;

class ScheduleServiceTest {

	private static final UUID LYRA = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID BORIN = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SELENE = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private LocalDate today;
	private ScheduleService service;

	@BeforeEach
	void setUp() {
		today = LocalDate.of(2026, 11, 17);
		service = new ScheduleService(new FixedRepository(today));
	}

	@Test
	void statusShowsOnMissionUntilEndDate() {
		AdventurerSchedule schedule = service.getSchedule(LYRA, null, null);
		assertEquals(AvailabilityState.ON_MISSION, schedule.status().state());
		assertEquals(today.plusDays(3), schedule.status().until());
		assertTrue(schedule.missions().stream()
				.anyMatch(m -> m.phaseOn(today) == MissionPhase.IN_PROGRESS));
	}

	@Test
	void statusShowsRestingUntilEndDate() {
		AdventurerSchedule schedule = service.getSchedule(BORIN, null, null);
		assertEquals(AvailabilityState.RESTING, schedule.status().state());
		assertEquals(today.plusDays(1), schedule.status().until());
	}

	@Test
	void statusShowsAvailable() {
		AdventurerSchedule schedule = service.getSchedule(SELENE, null, null);
		assertEquals(AvailabilityState.AVAILABLE, schedule.status().state());
	}

	@Test
	void calendarContainsMissionsAndRestsInWindow() {
		AdventurerSchedule schedule = service.getSchedule(
				LYRA,
				today.minusDays(5),
				today.plusDays(10));

		assertTrue(schedule.calendar().stream().anyMatch(e -> e.type() == CalendarEntryType.MISSION));
		assertTrue(schedule.calendar().stream().anyMatch(e -> e.type() == CalendarEntryType.REST));
	}

	@Test
	void availabilityDetectsWhoIsFreeOnPeriod() {
		AvailabilityReport report = service.findAvailability(today, today.plusDays(2));

		AdventurerAvailability lyra = byId(report, LYRA);
		AdventurerAvailability borin = byId(report, BORIN);
		AdventurerAvailability selene = byId(report, SELENE);

		assertFalse(lyra.available());
		assertFalse(borin.available());
		assertTrue(selene.available());
		assertFalse(lyra.conflicts().isEmpty());
	}

	@Test
	void availabilityRejectsOverlapWithPlannedMission() {
		AvailabilityReport report = service.findAvailability(
				today.plusDays(20),
				today.plusDays(22));

		assertFalse(byId(report, LYRA).available());
		assertTrue(byId(report, BORIN).available());
		assertTrue(byId(report, SELENE).available());
	}

	private static AdventurerAvailability byId(AvailabilityReport report, UUID id) {
		return report.adventurers().stream()
				.filter(a -> a.id().equals(id))
				.findFirst()
				.orElseThrow();
	}

	private static final class FixedRepository implements AdventurerRepository {

		private final Adventurer lyra = new Adventurer(LYRA, "Lyra Venteclair");
		private final Adventurer borin = new Adventurer(BORIN, "Borin Marteaufer");
		private final Adventurer selene = new Adventurer(SELENE, "Sélène Ombrelune");
		private final Mission lyraMission;
		private final RestPeriod lyraRest;
		private final Mission lyraPlanned;
		private final RestPeriod lyraPlannedRest;
		private final Mission borinPast;
		private final RestPeriod borinRest;
		private final Mission seleneFar;
		private final RestPeriod seleneRest;

		FixedRepository(LocalDate today) {
			lyraMission = new Mission(
					UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
					LYRA,
					"Escorte vers Port-Brume",
					today.minusDays(2),
					today.plusDays(3));
			lyraRest = RestPeriod.afterMission(lyraMission, 2);
			lyraPlanned = new Mission(
					UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab"),
					LYRA,
					"Cartographie des marais",
					today.plusDays(20),
					today.plusDays(25));
			lyraPlannedRest = RestPeriod.afterMission(lyraPlanned, 3);
			borinPast = new Mission(
					UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
					BORIN,
					"Chasse au sanglier des collines",
					today.minusDays(10),
					today.minusDays(4));
			borinRest = new RestPeriod(
					UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01"),
					BORIN,
					borinPast.id(),
					today.minusDays(3),
					today.plusDays(1));
			seleneFar = new Mission(
					UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
					SELENE,
					"Relique du temple d'Opale",
					today.plusDays(40),
					today.plusDays(47));
			seleneRest = RestPeriod.afterMission(seleneFar, 4);
		}

		@Override
		public List<Adventurer> findAll() {
			return List.of(borin, lyra, selene);
		}

		@Override
		public Optional<Adventurer> findById(UUID id) {
			return findAll().stream().filter(a -> a.id().equals(id)).findFirst();
		}

		@Override
		public List<Mission> findMissionsByAdventurerId(UUID adventurerId) {
			if (adventurerId.equals(LYRA)) {
				return List.of(lyraMission, lyraPlanned);
			}
			if (adventurerId.equals(BORIN)) {
				return List.of(borinPast);
			}
			if (adventurerId.equals(SELENE)) {
				return List.of(seleneFar);
			}
			return List.of();
		}

		@Override
		public List<RestPeriod> findRestPeriodsByAdventurerId(UUID adventurerId) {
			if (adventurerId.equals(LYRA)) {
				return List.of(lyraRest, lyraPlannedRest);
			}
			if (adventurerId.equals(BORIN)) {
				return List.of(borinRest);
			}
			if (adventurerId.equals(SELENE)) {
				return List.of(seleneRest);
			}
			return List.of();
		}
	}
}
