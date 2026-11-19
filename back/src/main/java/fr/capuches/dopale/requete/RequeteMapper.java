package fr.capuches.dopale.requete;

import fr.capuches.dopale.api.model.Requete;
import fr.capuches.dopale.api.model.RequeteInput;
import org.springframework.stereotype.Component;

@Component
public class RequeteMapper {

    public RequeteEntity versEntite(RequeteInput in) {
        RequeteEntity requete = new RequeteEntity();
        appliquer(requete, in);
        return requete;
    }

    /** Recopie les champs saisis. Le statut et les dates de mission ne viennent jamais du client. */
    public void appliquer(RequeteEntity requete, RequeteInput in) {
        requete.setNom(in.getNom());
        requete.setDescription(in.getDescription());
        requete.setDureeJours(in.getDureeJours());
        requete.setDatePeremption(in.getDatePeremption());
        requete.setPrimeBronze(in.getPrimeBronze());
        requete.setCommanditaire(in.getCommanditaire());
        requete.setExpRecommandee(in.getExpRecommandee() == null ? 0 : in.getExpRecommandee());
        requete.setXpGagnee(in.getXpGagnee() == null ? 0 : in.getXpGagnee());
    }

    public Requete versDto(RequeteEntity r) {
        return new Requete()
                .id(r.getId())
                .nom(r.getNom())
                .description(r.getDescription())
                .dureeJours(r.getDureeJours())
                .datePeremption(r.getDatePeremption())
                .primeBronze(r.getPrimeBronze())
                .commanditaire(r.getCommanditaire())
                .expRecommandee(r.getExpRecommandee())
                .xpGagnee(r.getXpGagnee())
                .statut(r.getStatut())
                .dateDepart(r.getDateDepart())
                .dateCloture(r.getDateCloture());
    }
}