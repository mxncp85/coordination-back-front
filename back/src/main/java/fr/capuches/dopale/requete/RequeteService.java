package fr.capuches.dopale.requete;

import fr.capuches.dopale.api.model.RequeteInput;
import fr.capuches.dopale.api.model.StatutRequete;
import fr.capuches.dopale.commun.exception.ConflitMetierException;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;
import fr.capuches.dopale.commun.exception.ValidationMetierException;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequeteService {

    // Selon le contrat. TODO volet équipes : changer la durée d'une requête EQUIPE_VALIDEE doit annuler l'équipe.
    private static final Set<StatutRequete> STATUTS_MODIFIABLES =
            EnumSet.of(StatutRequete.NOUVELLE, StatutRequete.EQUIPE_VALIDEE);

    private final RequeteRepository repository;
    private final RequeteMapper mapper;

    public Page<RequeteEntity> lister(FiltreRequete filtre, String tri, String ordre, int page, int size) {
        verifierFourchettePrime(filtre);
        Sort.Direction sens = "desc".equals(ordre) ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Tri secondaire sur l'id : l'ordre reste stable d'une page à l'autre
        Sort sort = Sort.by(sens, champDeTri(tri)).and(Sort.by("id"));
        return repository.findAll(RequeteSpecifications.filtrer(filtre), PageRequest.of(page, size, sort));
    }

    public RequeteEntity trouver(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Requête " + id + " introuvable"));
    }

    @Transactional
    public RequeteEntity creer(RequeteInput in) {
        verifierDatePeremption(in);
        return repository.save(mapper.versEntite(in));
    }

    @Transactional
    public RequeteEntity modifier(Long id, RequeteInput in) {
        RequeteEntity requete = trouver(id);
        if (!STATUTS_MODIFIABLES.contains(requete.getStatut())) {
            throw new ConflitMetierException(
                    "Une requête au statut " + requete.getStatut() + " ne peut plus être modifiée");
        }
        verifierDatePeremption(in);
        mapper.appliquer(requete, in);
        return requete;
    }

    private static String champDeTri(String tri) {
        if ("prime".equals(tri)) return "primeBronze";
        if ("niveau".equals(tri)) return "expRecommandee";
        return "datePeremption"; // "echeance", valeur par défaut du contrat
    }

    // Règle absente de l'OpenAPI (OpenAPI ne sait pas exprimer « aujourd'hui ou plus tard »)
    private void verifierDatePeremption(RequeteInput in) {
        if (in.getDatePeremption().isBefore(LocalDate.now())) {
            throw new ValidationMetierException("datePeremption",
                    "La date de péremption doit être aujourd'hui ou ultérieure");
        }
    }

    // Règle du contrat : primeMin doit être inférieur ou égal à primeMax
    private void verifierFourchettePrime(FiltreRequete filtre) {
        if (filtre.primeMin() != null && filtre.primeMax() != null && filtre.primeMin() > filtre.primeMax()) {
            throw new ValidationMetierException("primeMin", "primeMin doit être inférieur ou égal à primeMax");
        }
    }
}
