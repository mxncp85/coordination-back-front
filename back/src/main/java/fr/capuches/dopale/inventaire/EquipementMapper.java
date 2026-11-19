package fr.capuches.dopale.inventaire;

import org.springframework.stereotype.Component;

import fr.capuches.dopale.api.model.Equipement;

@Component
public class EquipementMapper {

	public Equipement versDto(EquipementEntity entity) {
		return new Equipement()
				.id(entity.getId())
				.nom(entity.getNom())
				.type(entity.getType())
				.durabiliteMax(entity.getDurabiliteMax())
				.durabiliteActuelle(entity.getDurabiliteActuelle())
				.prixAchatBronze(entity.getPrixAchatBronze())
				.statut(entity.getStatut());
	}
}
