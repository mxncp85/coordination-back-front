package fr.capuches.dopale.aventurier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.capuches.dopale.api.model.AventurierDisponibilite;
import fr.capuches.dopale.api.model.DisponibilitePeriode;
import fr.capuches.dopale.api.model.EmploiDuTemps;
import fr.capuches.dopale.api.model.OccupationCalendrier;
import fr.capuches.dopale.api.model.PhaseMission;
import fr.capuches.dopale.api.model.StatutAventurier;
import fr.capuches.dopale.api.model.TypeOccupation;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;
import fr.capuches.dopale.commun.exception.ValidationMetierException;
import fr.capuches.dopale.requete.EquipeMembreEntity;
import fr.capuches.dopale.requete.EquipeMembreRepository;
import fr.capuches.dopale.requete.RequeteEntity;

@Service
public class DisponibiliteService {

	private final AventurierRepository aventurierRepository;
	private final EquipeMembreRepository equipeMembreRepository;

	public DisponibiliteService(AventurierRepository aventurierRepository,
			EquipeMembreRepository equipeMembreRepository) {
		this.aventurierRepository = aventurierRepository;
		this.equipeMembreRepository = equipeMembreRepository;
	}

	@Transactional(readOnly = true)
	public EmploiDuTemps emploiDuTemps(Long aventurierId, LocalDate from, LocalDate to) {
		AventurierEntity aventurier = requireAventurier(aventurierId);
		LocalDate aujourdHui = LocalDate.now();
		List<OccupationCalendrier> missions = missions(aventurierId, aujourdHui);
		List<OccupationCalendrier> repos = repos(aventurier, missions);
		Periode fenetre = resoudreFenetre(from, to, missions, repos, aujourdHui);

		List<OccupationCalendrier> calendrier = new ArrayList<>();
		calendrier.addAll(filtrerChevauchement(missions, fenetre));
		calendrier.addAll(filtrerChevauchement(repos, fenetre));
		calendrier.sort(Comparator.comparing(OccupationCalendrier::getDebut).thenComparing(o -> o.getType().getValue()));

		StatutDisponibilite statut = calculerStatut(aventurier, aujourdHui);
		return new EmploiDuTemps(aventurier.getId(), aventurier.getNom(), statut.statut(), missions, repos, calendrier)
				.disponibleLe(statut.disponibleLe());
	}

	@Transactional(readOnly = true)
	public DisponibilitePeriode disponibilites(LocalDate from, LocalDate to) {
		if (from == null || to == null) {
			throw new ValidationMetierException("from", "Les paramètres from et to sont obligatoires");
		}
		Periode periode = new Periode(from, to);
		LocalDate aujourdHui = LocalDate.now();

		List<AventurierDisponibilite> resultats = aventurierRepository.findBySupprimeLeIsNullOrderByNomAsc().stream()
				.map(aventurier -> {
					List<OccupationCalendrier> conflits = occupationsChevauchantes(aventurier, periode, aujourdHui);
					StatutDisponibilite statut = calculerStatut(aventurier, aujourdHui);
					return new AventurierDisponibilite(
							aventurier.getId(),
							aventurier.getNom(),
							conflits.isEmpty(),
							statut.statut())
							.disponibleLe(statut.disponibleLe())
							.conflits(conflits);
				})
				.toList();

		return new DisponibilitePeriode(from, to, resultats);
	}

	@Transactional(readOnly = true)
	public boolean estDisponibleSur(Long aventurierId, Periode periode) {
		AventurierEntity aventurier = requireAventurier(aventurierId);
		return occupationsChevauchantes(aventurier, periode, LocalDate.now()).isEmpty();
	}

	@Transactional(readOnly = true)
	public StatutDisponibilite calculerStatut(AventurierEntity aventurier, LocalDate jour) {
		List<EquipeMembreEntity> actives = equipeMembreRepository.findEquipesActives(aventurier.getId());
		if (!actives.isEmpty()) {
			LocalDate finMission = finMissionEstimee(actives.getFirst().getRequete());
			LocalDate disponibleLe = finDisponibiliteApresMission(actives.getFirst().getRequete(), aventurier);
			return new StatutDisponibilite(StatutAventurier.EN_MISSION, disponibleLe != null ? disponibleLe : finMission);
		}
		if (aventurier.estEnRepos(jour)) {
			return new StatutDisponibilite(StatutAventurier.EN_REPOS, CalculRepos.disponibleLe(aventurier.getReposJusquAu()));
		}
		return new StatutDisponibilite(StatutAventurier.DISPONIBLE, null);
	}

	/**
	 * Applique le repos US-21 après clôture d'une mission (à appeler dans la transaction de clôture).
	 *
	 * @return dernier jour de repos inclus
	 */
	@Transactional
	public LocalDate appliquerReposApresCloture(AventurierEntity aventurier, RequeteEntity requete, LocalDate dateCloture) {
		int jours = CalculRepos.jours(requete.getDureeJours(), requete.getExpRecommandee(), aventurier.getExperience());
		LocalDate jusquAu = CalculRepos.reposJusquAu(dateCloture, jours);
		if (aventurier.getReposJusquAu() == null || jusquAu.isAfter(aventurier.getReposJusquAu())) {
			aventurier.setReposJusquAu(jusquAu);
		}
		return jusquAu;
	}

	private List<OccupationCalendrier> occupationsChevauchantes(AventurierEntity aventurier, Periode periode,
			LocalDate aujourdHui) {
		List<OccupationCalendrier> occupations = new ArrayList<>();
		occupations.addAll(filtrerChevauchement(missions(aventurier.getId(), aujourdHui), periode));
		occupations.addAll(filtrerChevauchement(repos(aventurier, missions(aventurier.getId(), aujourdHui)), periode));
		occupations.sort(Comparator.comparing(OccupationCalendrier::getDebut));
		return List.copyOf(occupations);
	}

	private List<OccupationCalendrier> missions(Long aventurierId, LocalDate aujourdHui) {
		return equipeMembreRepository.findOccupationsByAventurierId(aventurierId).stream()
				.map(EquipeMembreEntity::getRequete)
				.filter(r -> r.estActive() || "REUSSIE".equals(r.getStatut()) || "ECHOUEE".equals(r.getStatut()))
				.map(r -> versMission(r, aujourdHui))
				.toList();
	}

	private List<OccupationCalendrier> repos(AventurierEntity aventurier, List<OccupationCalendrier> missions) {
		if (aventurier.getReposJusquAu() == null) {
			return List.of();
		}
		LocalDate fin = aventurier.getReposJusquAu();
		LocalDate debut = missions.stream()
				.filter(m -> m.getFin() != null && !m.getFin().isAfter(fin))
				.map(OccupationCalendrier::getFin)
				.max(LocalDate::compareTo)
				.map(d -> d.plusDays(1))
				.orElse(fin);
		if (debut.isAfter(fin)) {
			debut = fin;
		}
		return List.of(new OccupationCalendrier(TypeOccupation.REPOS, debut, fin).libelle("Repos"));
	}

	private OccupationCalendrier versMission(RequeteEntity requete, LocalDate aujourdHui) {
		LocalDate debut = requete.getDateDepart() != null ? requete.getDateDepart() : aujourdHui;
		LocalDate fin = finMissionEstimee(requete);
		PhaseMission phase = phaseMission(requete, aujourdHui, debut, fin);
		return new OccupationCalendrier(TypeOccupation.MISSION, debut, fin)
				.id(requete.getId())
				.libelle(requete.getNom())
				.phaseMission(phase);
	}

	private PhaseMission phaseMission(RequeteEntity requete, LocalDate aujourdHui, LocalDate debut, LocalDate fin) {
		if ("EN_COURS".equals(requete.getStatut()) || (debut != null && fin != null && new Periode(debut, fin).contient(aujourdHui)
				&& requete.estActive())) {
			return PhaseMission.EN_COURS;
		}
		if ("EQUIPE_VALIDEE".equals(requete.getStatut()) || (debut != null && aujourdHui.isBefore(debut))) {
			return PhaseMission.PREVUE;
		}
		return PhaseMission.TERMINEE;
	}

	private LocalDate finMissionEstimee(RequeteEntity requete) {
		if (requete.getDateCloture() != null) {
			return requete.getDateCloture();
		}
		if (requete.getDateDepart() != null) {
			return requete.getDateDepart().plusDays(requete.getDureeJours() - 1L);
		}
		return null;
	}

	private LocalDate finDisponibiliteApresMission(RequeteEntity requete, AventurierEntity aventurier) {
		LocalDate finMission = finMissionEstimee(requete);
		if (finMission == null) {
			return null;
		}
		int jours = CalculRepos.jours(requete.getDureeJours(), requete.getExpRecommandee(), aventurier.getExperience());
		return CalculRepos.disponibleLe(CalculRepos.reposJusquAu(finMission, jours));
	}

	private List<OccupationCalendrier> filtrerChevauchement(List<OccupationCalendrier> occupations, Periode fenetre) {
		return occupations.stream()
				.filter(o -> o.getDebut() != null && o.getFin() != null)
				.filter(o -> new Periode(o.getDebut(), o.getFin()).chevauche(fenetre))
				.toList();
	}

	private Periode resoudreFenetre(LocalDate from, LocalDate to, List<OccupationCalendrier> missions,
			List<OccupationCalendrier> repos, LocalDate aujourdHui) {
		if (from != null && to != null) {
			return new Periode(from, to);
		}
		if (from != null) {
			return new Periode(from, from.plusMonths(1));
		}
		if (to != null) {
			return new Periode(to.minusMonths(1), to);
		}
		LocalDate min = aujourdHui;
		LocalDate max = aujourdHui.plusMonths(1);
		for (OccupationCalendrier o : missions) {
			if (o.getDebut() != null && o.getDebut().isBefore(min)) {
				min = o.getDebut();
			}
			if (o.getFin() != null && o.getFin().isAfter(max)) {
				max = o.getFin();
			}
		}
		for (OccupationCalendrier o : repos) {
			if (o.getDebut() != null && o.getDebut().isBefore(min)) {
				min = o.getDebut();
			}
			if (o.getFin() != null && o.getFin().isAfter(max)) {
				max = o.getFin();
			}
		}
		return new Periode(min, max);
	}

	private AventurierEntity requireAventurier(Long id) {
		return aventurierRepository.findByIdAndSupprimeLeIsNull(id)
				.orElseThrow(() -> new RessourceIntrouvableException("Aventurier " + id + " introuvable"));
	}

	public record StatutDisponibilite(StatutAventurier statut, LocalDate disponibleLe) {
	}
}
