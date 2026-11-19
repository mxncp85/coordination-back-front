package fr.capuches.dopale.inventaire;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import fr.capuches.dopale.api.EquipementsApi;
import fr.capuches.dopale.api.model.Equipement;
import fr.capuches.dopale.api.model.EquipementInput;
import fr.capuches.dopale.api.model.ListEquipements200Response;
import fr.capuches.dopale.api.model.StatutEquipement;
import fr.capuches.dopale.commun.pagination.PaginationUtils;

@RestController
public class EquipementController implements EquipementsApi {

	private final EquipementService equipementService;
	private final EquipementMapper equipementMapper;

	public EquipementController(EquipementService equipementService, EquipementMapper equipementMapper) {
		this.equipementService = equipementService;
		this.equipementMapper = equipementMapper;
	}

	@Override
	public ResponseEntity<ListEquipements200Response> listEquipements(StatutEquipement statut, Integer page,
			Integer size) {
		Page<EquipementEntity> equipements = equipementService.lister(statut,
				PaginationUtils.pageable(page, size, Sort.by("nom")));
		return ResponseEntity.ok(
				PaginationUtils.versReponse(equipements, equipementMapper::versDto, ListEquipements200Response::new));
	}

	@Override
	public ResponseEntity<Equipement> acheterEquipement(EquipementInput input) {
		EquipementEntity equipement = equipementService.acheter(input.getNom(), input.getType(),
				input.getDurabiliteMax(), input.getPrixAchatBronze());
		return ResponseEntity.status(HttpStatus.CREATED).body(equipementMapper.versDto(equipement));
	}

	@Override
	public ResponseEntity<Equipement> getEquipement(Long id) {
		return ResponseEntity.ok(equipementMapper.versDto(equipementService.trouver(id)));
	}

	@Override
	public ResponseEntity<Equipement> reparerEquipement(Long id) {
		return ResponseEntity.ok(equipementMapper.versDto(equipementService.reparer(id)));
	}
}
