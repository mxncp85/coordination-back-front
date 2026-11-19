package fr.capuches.dopale.requete;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

final class RequeteSpecifications {

    private RequeteSpecifications() {}

    /** Construit la clause WHERE : seuls les critères renseignés sont appliqués, combinés par un ET. */
    static Specification<RequeteEntity> filtrer(FiltreRequete filtre) {
        return (requete, query, cb) -> {
            List<Predicate> predicats = new ArrayList<>();

            if (filtre.statut() != null) {
                predicats.add(cb.equal(requete.get("statut"), filtre.statut()));
            }
            if (filtre.primeMin() != null) {
                predicats.add(cb.greaterThanOrEqualTo(requete.<Long>get("primeBronze"), filtre.primeMin()));
            }
            if (filtre.primeMax() != null) {
                predicats.add(cb.lessThanOrEqualTo(requete.<Long>get("primeBronze"), filtre.primeMax()));
            }
            if (filtre.niveau() != null) {
                predicats.add(cb.equal(requete.get("expRecommandee"), filtre.niveau()));
            }
            if (filtre.echeanceAvant() != null) {
                predicats.add(cb.lessThanOrEqualTo(requete.<LocalDate>get("datePeremption"), filtre.echeanceAvant()));
            }
            if (filtre.commanditaire() != null && !filtre.commanditaire().isBlank()) {
                String recherche = "%" + filtre.commanditaire().trim().toLowerCase() + "%";
                predicats.add(cb.like(cb.lower(requete.<String>get("commanditaire")), recherche));
            }

            return cb.and(predicats.toArray(new Predicate[0]));
        };
    }
}
