package fr.capuches.dopale.requete;

import static org.assertj.core.api.Assertions.assertThat;

import fr.capuches.dopale.api.model.Requete;
import fr.capuches.dopale.api.model.RequeteInput;
import fr.capuches.dopale.api.model.StatutRequete;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RequeteMapperTest {

    private final RequeteMapper mapper = new RequeteMapper();

    private static RequeteInput saisie() {
        return new RequeteInput()
                .nom("Escorte de caravane")
                .description("Escorter une caravane jusqu'à Brumevert")
                .dureeJours(5)
                .datePeremption(LocalDate.of(2026, 12, 31))
                .primeBronze(150_000L)
                .commanditaire("Village de Brumevert")
                .expRecommandee(120)
                .xpGagnee(40);
    }

    @Test
    void versEntite_recopie_tous_les_champs_saisis_et_commence_au_statut_nouvelle() {
        RequeteEntity requete = mapper.versEntite(saisie());

        assertThat(requete.getNom()).isEqualTo("Escorte de caravane");
        assertThat(requete.getDescription()).isEqualTo("Escorter une caravane jusqu'à Brumevert");
        assertThat(requete.getDureeJours()).isEqualTo(5);
        assertThat(requete.getDatePeremption()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(requete.getPrimeBronze()).isEqualTo(150_000L);
        assertThat(requete.getCommanditaire()).isEqualTo("Village de Brumevert");
        assertThat(requete.getExpRecommandee()).isEqualTo(120);
        assertThat(requete.getXpGagnee()).isEqualTo(40);
        assertThat(requete.getStatut()).isEqualTo(StatutRequete.NOUVELLE);
    }

    @Test
    void versEntite_met_a_zero_l_experience_et_l_xp_non_renseignees() {
        RequeteEntity requete = mapper.versEntite(saisie().expRecommandee(null).xpGagnee(null));

        assertThat(requete.getExpRecommandee()).isZero();
        assertThat(requete.getXpGagnee()).isZero();
    }

    @Test
    void appliquer_ne_modifie_jamais_le_statut_ni_les_dates_de_mission() {
        RequeteEntity requete = new RequeteEntity();
        requete.setStatut(StatutRequete.EN_COURS);
        requete.setDateDepart(LocalDate.of(2026, 11, 1));

        mapper.appliquer(requete, saisie());

        assertThat(requete.getStatut()).isEqualTo(StatutRequete.EN_COURS);
        assertThat(requete.getDateDepart()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(requete.getNom()).isEqualTo("Escorte de caravane");
    }

    @Test
    void versDto_renvoie_tous_les_champs_de_l_entite() {
        RequeteEntity requete = mapper.versEntite(saisie());
        ReflectionTestUtils.setField(requete, "id", 7L);
        requete.setStatut(StatutRequete.EQUIPE_VALIDEE);

        Requete dto = mapper.versDto(requete);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getNom()).isEqualTo("Escorte de caravane");
        assertThat(dto.getDureeJours()).isEqualTo(5);
        assertThat(dto.getPrimeBronze()).isEqualTo(150_000L);
        assertThat(dto.getStatut()).isEqualTo(StatutRequete.EQUIPE_VALIDEE);
        assertThat(dto.getDateDepart()).isNull();
        assertThat(dto.getDateCloture()).isNull();
    }
}
