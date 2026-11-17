package fr.capuches.dopale.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import fr.capuches.dopale.domain.Adventurer;
import fr.capuches.dopale.domain.Mission;
import fr.capuches.dopale.domain.RestPeriod;
import jakarta.annotation.PostConstruct;

@Repository
public class InMemoryAdventurerRepository implements AdventurerRepository {

	private final Map<UUID, Adventurer> adventurers = new ConcurrentHashMap<>();
	private final Map<UUID, List<Mission>> missionsByAdventurer = new ConcurrentHashMap<>();
	private final Map<UUID, List<RestPeriod>> restsByAdventurer = new ConcurrentHashMap<>();

	@PostConstruct
	void seed() {
		LocalDate today = LocalDate.now();

		Adventurer lyra = new Adventurer(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Lyra Venteclair");
		Adventurer borin = new Adventurer(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Borin Marteaufer");
		Adventurer selene = new Adventurer(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Sélène Ombrelune");

		save(lyra);
		save(borin);
		save(selene);

		// Lyra : en mission aujourd'hui, puis repos US-21
		Mission lyraMission = new Mission(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				lyra.id(),
				"Escorte vers Port-Brume",
				today.minusDays(2),
				today.plusDays(3));
		RestPeriod lyraRest = RestPeriod.afterMission(lyraMission, 2);
		addMission(lyraMission);
		addRest(lyraRest);

		// Mission prévue plus tard pour Lyra
		Mission lyraPlanned = new Mission(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab"),
				lyra.id(),
				"Cartographie des marais",
				today.plusDays(20),
				today.plusDays(25));
		RestPeriod lyraPlannedRest = RestPeriod.afterMission(lyraPlanned, 3);
		addMission(lyraPlanned);
		addRest(lyraPlannedRest);

		// Borin : au repos aujourd'hui (mission déjà terminée)
		Mission borinPast = new Mission(
				UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
				borin.id(),
				"Chasse au sanglier des collines",
				today.minusDays(10),
				today.minusDays(4));
		RestPeriod borinRest = new RestPeriod(
				UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01"),
				borin.id(),
				borinPast.id(),
				today.minusDays(3),
				today.plusDays(1));
		addMission(borinPast);
		addRest(borinRest);

		// Sélène : disponible (aucune occupation autour d'aujourd'hui)
		Mission seleneFar = new Mission(
				UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
				selene.id(),
				"Relique du temple d'Opale",
				today.plusDays(40),
				today.plusDays(47));
		RestPeriod seleneRest = RestPeriod.afterMission(seleneFar, 4);
		addMission(seleneFar);
		addRest(seleneRest);
	}

	private void save(Adventurer adventurer) {
		adventurers.put(adventurer.id(), adventurer);
		missionsByAdventurer.putIfAbsent(adventurer.id(), new ArrayList<>());
		restsByAdventurer.putIfAbsent(adventurer.id(), new ArrayList<>());
	}

	private void addMission(Mission mission) {
		missionsByAdventurer
				.computeIfAbsent(mission.adventurerId(), id -> new ArrayList<>())
				.add(mission);
	}

	private void addRest(RestPeriod rest) {
		restsByAdventurer
				.computeIfAbsent(rest.adventurerId(), id -> new ArrayList<>())
				.add(rest);
	}

	@Override
	public List<Adventurer> findAll() {
		return adventurers.values().stream()
				.sorted(Comparator.comparing(Adventurer::name))
				.toList();
	}

	@Override
	public Optional<Adventurer> findById(UUID id) {
		return Optional.ofNullable(adventurers.get(id));
	}

	@Override
	public List<Mission> findMissionsByAdventurerId(UUID adventurerId) {
		return List.copyOf(missionsByAdventurer.getOrDefault(adventurerId, List.of()));
	}

	@Override
	public List<RestPeriod> findRestPeriodsByAdventurerId(UUID adventurerId) {
		return List.copyOf(restsByAdventurer.getOrDefault(adventurerId, List.of()));
	}
}
