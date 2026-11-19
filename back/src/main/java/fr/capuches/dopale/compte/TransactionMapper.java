package fr.capuches.dopale.compte;

import org.springframework.stereotype.Component;

import fr.capuches.dopale.api.model.Transaction;

@Component
public class TransactionMapper {

	public Transaction versDto(TransactionEntity entity) {
		return new Transaction()
				.id(entity.getId())
				.date(entity.getDateTransaction())
				.montantBronze(entity.getMontantBronze())
				.libelle(entity.getLibelle())
				.type(entity.getType())
				.requeteId(entity.getRequeteId())
				.aventurierId(entity.getAventurierId())
				.equipementId(entity.getEquipementId());
	}
}
