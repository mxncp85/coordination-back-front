package fr.capuches.dopale.inventaire;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.capuches.dopale.api.model.StatutEquipement;
import fr.capuches.dopale.api.model.TypeTransaction;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;
import fr.capuches.dopale.compte.CompteService;
import fr.capuches.dopale.compte.TransactionEntity;

@Service
public class EquipementService {

	private final EquipementRepository equipementRepository;
	private final CompteService compteService;

	public EquipementService(EquipementRepository equipementRepository, CompteService compteService) {
		this.equipementRepository = equipementRepository;
		this.compteService = compteService;
	}

	// équipement et débit dans la même transaction : si le débit échoue, l'équipement n'est pas créé
	@Transactional
	public EquipementEntity acheter(String nom, String type, int durabiliteMax, long prixAchatBronze) {
		EquipementEntity equipement = equipementRepository
				.save(new EquipementEntity(nom, type, durabiliteMax, prixAchatBronze));
		// un équipement gratuit (don, trouvaille) ne crée pas de transaction
		if (prixAchatBronze > 0) {
			compteService.enregistrer(TransactionEntity
					.debit(prixAchatBronze, "Achat : " + nom, TypeTransaction.ACHAT)
					.pourEquipement(equipement.getId()));
		}
		return equipement;
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
