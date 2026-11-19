package fr.capuches.dopale.aventurier;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import fr.capuches.dopale.api.AventuriersApi;
import fr.capuches.dopale.api.model.Aventurier;
import fr.capuches.dopale.api.model.AventurierInput;
import fr.capuches.dopale.api.model.DisponibilitePeriode;
import fr.capuches.dopale.api.model.EmploiDuTemps;
import fr.capuches.dopale.api.model.ListAventuriers200Response;
import fr.capuches.dopale.api.model.Specialite;

@RestController
public class AventurierController implements AventuriersApi {

	private final AventurierService aventurierService;
	private final DisponibiliteService disponibiliteService;

	public AventurierController(AventurierService aventurierService, DisponibiliteService disponibiliteService) {
		this.aventurierService = aventurierService;
		this.disponibiliteService = disponibiliteService;
	}

	@Override
	public ResponseEntity<Aventurier> createAventurier(AventurierInput aventurierInput) {
		return ResponseEntity.status(HttpStatus.CREATED).body(aventurierService.create(aventurierInput));
	}

	@Override
	public ResponseEntity<Void> deleteAventurier(Long id) {
		aventurierService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Aventurier> getAventurier(Long id) {
		return ResponseEntity.ok(aventurierService.get(id));
	}

	@Override
	public ResponseEntity<EmploiDuTemps> getEmploiDuTemps(Long id, LocalDate from, LocalDate to) {
		return ResponseEntity.ok(disponibiliteService.emploiDuTemps(id, from, to));
	}

	@Override
	public ResponseEntity<ListAventuriers200Response> listAventuriers(
			String nom,
			Specialite specialite,
			Integer expMin,
			Integer expMax,
			Boolean disponible,
			String tri,
			String ordre,
			Integer page,
			Integer size) {
		return ResponseEntity.ok(aventurierService.list(nom, specialite, expMin, expMax, disponible, tri, ordre, page, size));
	}

	@Override
	public ResponseEntity<DisponibilitePeriode> listDisponibilites(LocalDate from, LocalDate to) {
		return ResponseEntity.ok(disponibiliteService.disponibilites(from, to));
	}

	@Override
	public ResponseEntity<Aventurier> updateAventurier(Long id, AventurierInput aventurierInput) {
		return ResponseEntity.ok(aventurierService.update(id, aventurierInput));
	}
}
