package fr.capuches.dopale.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fr.capuches.dopale.domain.Adventurer;
import fr.capuches.dopale.domain.Mission;
import fr.capuches.dopale.domain.RestPeriod;

public interface AdventurerRepository {

	List<Adventurer> findAll();

	Optional<Adventurer> findById(UUID id);

	List<Mission> findMissionsByAdventurerId(UUID adventurerId);

	List<RestPeriod> findRestPeriodsByAdventurerId(UUID adventurerId);
}
