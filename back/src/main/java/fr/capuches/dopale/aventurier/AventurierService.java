package fr.capuches.dopale.aventurier;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.capuches.dopale.api.model.Aventurier;
import fr.capuches.dopale.api.model.AventurierInput;
import fr.capuches.dopale.api.model.ListAventuriers200Response;
import fr.capuches.dopale.api.model.Specialite;
import fr.capuches.dopale.aventurier.DisponibiliteService.StatutDisponibilite;
import fr.capuches.dopale.commun.exception.ConflitMetierException;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;
import fr.capuches.dopale.commun.exception.ValidationMetierException;
import fr.capuches.dopale.commun.pagination.PaginationUtils;
import fr.capuches.dopale.requete.EquipeMembreRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

@Service
public class AventurierService {

	private static final Map<String, String> COLONNES_TRI = Map.of(
			"nom", "nom",
			"experience", "experience",
			"tauxJournalier", "tauxJournalierBronze");

	private final AventurierRepository repository;
	private final AventurierMapper mapper;
	private final DisponibiliteService disponibiliteService;
	private final EquipeMembreRepository equipeMembreRepository;

	public AventurierService(
			AventurierRepository repository,
			AventurierMapper mapper,
			DisponibiliteService disponibiliteService,
			EquipeMembreRepository equipeMembreRepository) {
		this.repository = repository;
		this.mapper = mapper;
		this.disponibiliteService = disponibiliteService;
		this.equipeMembreRepository = equipeMembreRepository;
	}

	@Transactional(readOnly = true)
	public Aventurier get(Long id) {
		AventurierEntity entity = require(id);
		return mapper.versDto(entity, disponibiliteService.calculerStatut(entity, LocalDate.now()));
	}

	@Transactional
	public Aventurier create(AventurierInput input) {
		AventurierEntity entity = new AventurierEntity();
		mapper.appliquer(input, entity);
		AventurierEntity sauve = repository.saveAndFlush(entity);
		return mapper.versDto(sauve, disponibiliteService.calculerStatut(sauve, LocalDate.now()));
	}

	@Transactional
	public Aventurier update(Long id, AventurierInput input) {
		AventurierEntity entity = require(id);
		mapper.appliquer(input, entity);
		AventurierEntity sauve = repository.saveAndFlush(entity);
		return mapper.versDto(sauve, disponibiliteService.calculerStatut(sauve, LocalDate.now()));
	}

	/**
	 * Archive (soft-delete) : disparaît des listes actives, conserve l'historique des missions.
	 * 409 s'il est dans une équipe réservée ({@code EQUIPE_VALIDEE}) ou en mission ({@code EN_COURS}).
	 */
	@Transactional
	public void archive(Long id) {
		AventurierEntity entity = require(id);
		if (equipeMembreRepository.estDansEquipeActive(id)) {
			throw new ConflitMetierException(
					"Impossible d'archiver l'aventurier " + id + " : il est réservé ou en mission");
		}
		entity.setSupprimeLe(Instant.now());
	}

	/** Alias OpenAPI {@code deleteAventurier} → archive métier. */
	@Transactional
	public void delete(Long id) {
		archive(id);
	}

	@Transactional(readOnly = true)
	public ListAventuriers200Response list(
			String nom,
			Specialite specialite,
			Integer expMin,
			Integer expMax,
			Boolean disponible,
			String tri,
			String ordre,
			Integer page,
			Integer size) {
		if (expMin != null && expMax != null && expMin > expMax) {
			throw new ValidationMetierException("expMin", "expMin doit être inférieur ou égal à expMax");
		}

		String colonne = COLONNES_TRI.getOrDefault(tri == null ? "nom" : tri, "nom");
		Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(PaginationUtils.direction(ordre), colonne));
		Specification<AventurierEntity> spec = construireSpec(nom, specialite, expMin, expMax);
		Page<AventurierEntity> resultat = repository.findAll(spec, pageable);

		LocalDate aujourdHui = LocalDate.now();
		Page<AventurierEntity> filtree = resultat;
		if (disponible != null) {
			var filtrés = resultat.getContent().stream()
					.filter(e -> {
						StatutDisponibilite statut = disponibiliteService.calculerStatut(e, aujourdHui);
						return disponible.equals(AventurierMapper.estDisponible(statut.statut()));
					})
					.toList();
			return PaginationUtils.versReponse(
					new org.springframework.data.domain.PageImpl<>(filtrés, pageable, filtrés.size()),
					e -> mapper.versDto(e, disponibiliteService.calculerStatut(e, aujourdHui)),
					ListAventuriers200Response::new);
		}

		return PaginationUtils.versReponse(
				filtree,
				e -> mapper.versDto(e, disponibiliteService.calculerStatut(e, aujourdHui)),
				ListAventuriers200Response::new);
	}

	private Specification<AventurierEntity> construireSpec(String nom, Specialite specialite, Integer expMin,
			Integer expMax) {
		return (root, query, cb) -> {
			Predicate pred = cb.isNull(root.get("supprimeLe"));
			if (nom != null && !nom.isBlank()) {
				pred = cb.and(pred, cb.like(cb.lower(root.get("nom")), "%" + nom.toLowerCase() + "%"));
			}
			if (expMin != null) {
				pred = cb.and(pred, cb.ge(root.get("experience"), expMin));
			}
			if (expMax != null) {
				pred = cb.and(pred, cb.le(root.get("experience"), expMax));
			}
			if (specialite != null) {
				var join = root.join("specialites", JoinType.INNER);
				pred = cb.and(pred, cb.equal(join, specialite.getValue()));
				query.distinct(true);
			}
			return pred;
		};
	}

	private AventurierEntity require(Long id) {
		return repository.findByIdAndSupprimeLeIsNull(id)
				.orElseThrow(() -> new RessourceIntrouvableException("Aventurier " + id + " introuvable"));
	}
}
