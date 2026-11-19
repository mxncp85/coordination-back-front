package fr.capuches.dopale.aventurier;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fr.capuches.dopale.api.model.Aventurier;
import fr.capuches.dopale.api.model.AventurierInput;
import fr.capuches.dopale.api.model.Specialite;
import fr.capuches.dopale.api.model.StatutAventurier;
import fr.capuches.dopale.aventurier.DisponibiliteService.StatutDisponibilite;

@Component
public class AventurierMapper {

	public void appliquer(AventurierInput input, AventurierEntity entity) {
		entity.setNom(input.getNom());
		entity.setExperience(input.getExperience());
		entity.setTauxBaseBronze(input.getTauxBaseBronze());
		entity.setSpecialites(input.getSpecialites().stream()
				.map(Specialite::getValue)
				.collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	public Aventurier versDto(AventurierEntity entity, StatutDisponibilite statut) {
		List<Specialite> specialites = entity.getSpecialites().stream()
				.map(Specialite::fromValue)
				.toList();
		long tauxJournalier = entity.getTauxJournalierBronze() != null
				? entity.getTauxJournalierBronze()
				: Math.round(entity.getTauxBaseBronze() * (1 + 0.5 * Math.log(entity.getExperience())));

		return new Aventurier(
				entity.getNom(),
				specialites,
				entity.getExperience(),
				entity.getTauxBaseBronze(),
				entity.getId(),
				tauxJournalier,
				statut.statut())
				.disponibleLe(statut.disponibleLe());
	}

	public static boolean estDisponible(StatutAventurier statut) {
		return statut == StatutAventurier.DISPONIBLE;
	}
}
