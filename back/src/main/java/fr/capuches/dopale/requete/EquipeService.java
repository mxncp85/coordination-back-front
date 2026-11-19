package fr.capuches.dopale.requete;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.capuches.dopale.api.model.Equipe;
import fr.capuches.dopale.api.model.EquipeMembresInner;
import fr.capuches.dopale.api.model.StatutEquipement;
import fr.capuches.dopale.api.model.StatutRequete;
import fr.capuches.dopale.aventurier.AventurierEntity;
import fr.capuches.dopale.aventurier.AventurierRepository;
import fr.capuches.dopale.aventurier.DisponibiliteService;
import fr.capuches.dopale.aventurier.Periode;
import fr.capuches.dopale.commun.exception.ConflitMetierException;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;
import fr.capuches.dopale.inventaire.EquipementEntity;
import fr.capuches.dopale.inventaire.EquipementRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipeService {

    private final RequeteRepository requeteRepository;
    private final EquipeMembreRepository equipeMembreRepository;
    private final AventurierRepository aventurierRepository;
    private final DisponibiliteService disponibiliteService;
    private final EquipementRepository equipementRepository;

    public Equipe consulter(Long requeteId) {
        RequeteEntity requete = trouverRequete(requeteId);
        List<EquipeMembreEntity> membres = equipeMembreRepository.findByRequeteId(requeteId);
        return versEquipe(requete, charger(membres));
    }

    /** Remplace la composition de l'équipe (brouillon). */
    @Transactional
    public Equipe composer(Long requeteId, Collection<Long> aventurierIds, Collection<Long> equipementIds) {
        RequeteEntity requete = trouverRequete(requeteId);
        if (requete.getStatut() != StatutRequete.NOUVELLE) {
            throw new ConflitMetierException("L'équipe ne peut être composée que pour une requête NOUVELLE");
        }

        Periode periode = periodeMission(requete);
        List<AventurierEntity> aventuriers = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(aventurierIds)) {
            AventurierEntity aventurier = aventurierRepository.findByIdAndSupprimeLeIsNull(id)
                    .orElseThrow(() -> new RessourceIntrouvableException("Aventurier " + id + " introuvable"));
            verifierDisponible(aventurier, periode);
            aventuriers.add(aventurier);
        }

        Set<Long> equipements = new LinkedHashSet<>(equipementIds == null ? Set.of() : equipementIds);
        equipements.forEach(this::trouverEquipementDisponible);

        Map<Long, EquipeMembreEntity> actuels = equipeMembreRepository.findByRequeteId(requeteId).stream()
                .collect(Collectors.toMap(EquipeMembreEntity::getAventurierId, Function.identity()));
        List<EquipeMembreEntity> membres = new ArrayList<>();
        for (AventurierEntity aventurier : aventuriers) {
            EquipeMembreEntity membre = actuels.remove(aventurier.getId());
            if (membre == null) {
                membre = new EquipeMembreEntity();
                membre.setRequeteId(requeteId);
                membre.setRequete(requete);
                membre.setAventurierId(aventurier.getId());
            }
            membre.setTauxJournalierFigeBronze(aventurier.getTauxJournalierBronze());
            membres.add(membre);
        }

        // ce qui reste dans "actuels" a été retiré de l'équipe
        equipeMembreRepository.deleteAll(List.copyOf(actuels.values()));
        equipeMembreRepository.saveAll(membres);
        requete.getEquipementIds().clear();
        requete.getEquipementIds().addAll(equipements);
        return versEquipe(requete, aventuriers);
    }

    /** Valide l'équipe : la requête passe en EQUIPE_VALIDEE (tout ou rien). */
    @Transactional
    public Equipe valider(Long requeteId) {
        RequeteEntity requete = trouverRequete(requeteId);
        if (requete.getStatut() != StatutRequete.NOUVELLE) {
            throw new ConflitMetierException("Seule une requête NOUVELLE peut avoir son équipe validée");
        }
        if (requete.getDatePeremption().isBefore(LocalDate.now())) {
            throw new ConflitMetierException("La requête est expirée");
        }

        List<EquipeMembreEntity> membres = equipeMembreRepository.findByRequeteId(requeteId);
        List<AventurierEntity> aventuriers = charger(membres);
        if (!estComplete(aventuriers)) {
            throw new ConflitMetierException("L'équipe est incomplète");
        }

        Periode periode = periodeMission(requete);
        aventuriers.forEach(aventurier -> verifierDisponible(aventurier, periode));
        List<EquipementEntity> equipements = requete.getEquipementIds().stream()
                .map(this::trouverEquipementDisponible)
                .toList();

        Map<Long, AventurierEntity> parId = aventuriers.stream()
                .collect(Collectors.toMap(AventurierEntity::getId, Function.identity()));
        membres.forEach(membre -> membre.setTauxJournalierFigeBronze(
                parId.get(membre.getAventurierId()).getTauxJournalierBronze()));

        equipements.forEach(EquipementEntity::emprunter);
        requete.setStatut(StatutRequete.EQUIPE_VALIDEE);
        return versEquipe(requete, aventuriers);
    }

    private RequeteEntity trouverRequete(Long id) {
        return requeteRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Requête " + id + " introuvable"));
    }

    private List<AventurierEntity> charger(List<EquipeMembreEntity> membres) {
        List<Long> ids = membres.stream().map(EquipeMembreEntity::getAventurierId).toList();
        return aventurierRepository.findAllById(ids).stream()
                .sorted(Comparator.comparing(AventurierEntity::getId))
                .toList();
    }

    private EquipementEntity trouverEquipementDisponible(Long id) {
        EquipementEntity equipement = equipementRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Équipement " + id + " introuvable"));
        if (equipement.getStatut() != StatutEquipement.DISPONIBLE) {
            throw new ConflitMetierException("L'équipement " + id + " n'est pas disponible");
        }
        return equipement;
    }

    /** Tant que la mission n'a pas de date de départ : de aujourd'hui jusqu'à la fin prévue. */
    private Periode periodeMission(RequeteEntity requete) {
        LocalDate debut = LocalDate.now();
        return new Periode(debut, debut.plusDays(requete.getDureeJours() - 1L));
    }

    private void verifierDisponible(AventurierEntity aventurier, Periode periode) {
        if (!disponibiliteService.estDisponibleSur(aventurier.getId(), periode)) {
            throw new ConflitMetierException(
                    "L'aventurier " + aventurier.getNom() + " n'est pas disponible sur la durée de la requête");
        }
    }

    /** Critère de complétude à confirmer avec le client (question Q4) : pour l'instant, au moins un membre. */
    private boolean estComplete(List<AventurierEntity> aventuriers) {
        return !aventuriers.isEmpty();
    }

    /** Indice individuel : min(experience, expRecommandee) / experience. */
    private double ti(AventurierEntity aventurier, RequeteEntity requete) {
        if (requete.getExpRecommandee() == 0) {
            return 1.0; // aucune expérience demandée : personne n'est en dessous du niveau
        }
        return (double) Math.min(aventurier.getExperience(), requete.getExpRecommandee()) / aventurier.getExperience();
    }

    private Equipe versEquipe(RequeteEntity requete, List<AventurierEntity> aventuriers) {
        List<EquipeMembresInner> membres = aventuriers.stream()
                .map(a -> new EquipeMembresInner()
                        .aventurierId(a.getId())
                        .nom(a.getNom())
                        .experience(a.getExperience())
                        .tauxJournalierBronze(a.getTauxJournalierBronze())
                        .ti(arrondi(ti(a, requete))))
                .toList();

        long cout = aventuriers.stream()
                .mapToLong(a -> a.getTauxJournalierBronze() * requete.getDureeJours())
                .sum();
        boolean vide = aventuriers.isEmpty();
        // Tq = moyenne des ti (formule du client à confirmer)
        double tq = aventuriers.stream().mapToDouble(a -> ti(a, requete)).average().orElse(0);

        return new Equipe()
                .requeteId(requete.getId())
                .membres(membres)
                .equipementIds(requete.getEquipementIds().stream().sorted().toList())
                .chanceReussite(vide ? null : arrondi(tq))
                .coutBronze(cout)
                .ratioCoutPrime(vide ? null : arrondi((double) cout / requete.getPrimeBronze()))
                .complete(estComplete(aventuriers))
                .validee(requete.getStatut() != StatutRequete.NOUVELLE && requete.getStatut() != StatutRequete.REJETEE);
    }

    private BigDecimal arrondi(double valeur) {
        return BigDecimal.valueOf(valeur).setScale(2, RoundingMode.HALF_UP);
    }
}
