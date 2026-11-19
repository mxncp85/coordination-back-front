package fr.capuches.dopale.inventaire;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.capuches.dopale.api.model.StatutEquipement;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;

@Service
public class EquipementService {

	private final EquipementRepository equipementRepository;

	public EquipementService(EquipementRepository equipementRepository) {
		this.equipementRepository = equipementRepository;
	}

	// TODO débiter le prix (transaction ACHAT) quand les transactions seront en place
	@Transactional
	public EquipementEntity acheter(String nom, String type, int durabiliteMax, long prixAchatBronze) {
		return equipementRepository.save(new EquipementEntity(nom, type, durabiliteMax, prixAchatBronze));
	}

	@Transactional(readOnly = true)
	public EquipementEntity trouver(Long id) {
		return equipementRepository.findById(id)
				.orElseThrow(() -> new RessourceIntrouvableException("Equipement " + id + " introuvable"));
	}

	@Transactional(readOnly = true)
	public Page<EquipementEntity> lister(StatutEquipement statut, Pageable pageable) {
		if (statut == null) {
			return equipementRepository.findAll(pageable);
		}
		return equipementRepository.findByStatut(statut, pageable);
	}

	// appelé à la validation d'une équipe (EQP-05)
	@Transactional
	public EquipementEntity emprunter(Long id) {
		EquipementEntity equipement = trouver(id);
		equipement.emprunter();
		return equipement;
	}

	// TODO coût de réparation à voir avec le client
	@Transactional
	public EquipementEntity reparer(Long id) {
		EquipementEntity equipement = trouver(id);
		equipement.reparer();
		return equipement;
	}
}
