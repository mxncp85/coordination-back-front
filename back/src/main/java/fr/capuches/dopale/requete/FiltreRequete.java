package fr.capuches.dopale.requete;

import fr.capuches.dopale.api.model.StatutRequete;
import java.time.LocalDate;

/** Critères de recherche de la liste des requêtes. Un critère à {@code null} n'est pas appliqué. */
public record FiltreRequete(
        StatutRequete statut,
        Long primeMin,
        Long primeMax,
        Integer niveau,
        LocalDate echeanceAvant,
        String commanditaire) {}