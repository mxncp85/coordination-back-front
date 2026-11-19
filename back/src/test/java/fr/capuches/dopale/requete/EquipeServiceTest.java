package fr.capuches.dopale.requete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.capuches.dopale.api.model.Equipe;
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

@ExtendWith(MockitoExtension.class)
class EquipeServiceTest {

    @Mock
    private RequeteRepository requeteRepository;
    @Mock
    private EquipeMembreRepository equipeMembreRepository;
    @Mock
    private AventurierRepository aventurierRepository;
    @Mock
    private DisponibiliteService disponibiliteService;
    @Mock
    private EquipementRepository equipementRepository;
    @Captor
    private ArgumentCaptor<List<EquipeMembreEntity>> membresCaptor;

    @InjectMocks
    private EquipeService service;

    private RequeteEntity requete(StatutRequete statut, LocalDate datePeremption) {
        RequeteEntity requete = new RequeteEntity();
        ReflectionTestUtils.setField(requete, "id", 1L);
        requete.setNom("Chasse au dragon");
        requete.setDureeJours(10);
        requete.setDatePeremption(datePeremption);
        requete.setPrimeBronze(1000);
        requete.setExpRecommandee(100);
        requete.setStatut(statut);
        return requete;
    }

    private RequeteEntity requeteNouvelle() {
        return requete(StatutRequete.NOUVELLE, LocalDate.now().plusDays(5));
    }

    private AventurierEntity aventurier(long id, int experience, long taux) {
        AventurierEntity aventurier = new AventurierEntity();
        ReflectionTestUtils.setField(aventurier, "id", id);
        aventurier.setNom("Aventurier " + id);
        aventurier.setExperience(experience);
        ReflectionTestUtils.setField(aventurier, "tauxJournalierBronze", taux);
        return aventurier;
    }

    private EquipementEntity equipement(long id, StatutEquipement statut) {
        EquipementEntity equipement = new EquipementEntity("Épée", "arme", 10, 500);
        ReflectionTestUtils.setField(equipement, "id", id);
        ReflectionTestUtils.setField(equipement, "statut", statut);
        return equipement;
    }

    private EquipeMembreEntity membre(long aventurierId) {
        EquipeMembreEntity membre = new EquipeMembreEntity();
        membre.setRequeteId(1L);
        membre.setAventurierId(aventurierId);
        return membre;
    }

    // ---------- composer ----------

    @Test
    void composer_enregistre_les_membres_et_calcule_l_equipe() {
        RequeteEntity requete = requeteNouvelle();
        EquipeMembreEntity retire = membre(12L);
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requete));
        when(aventurierRepository.findByIdAndSupprimeLeIsNull(10L)).thenReturn(Optional.of(aventurier(10, 50, 200)));
        when(aventurierRepository.findByIdAndSupprimeLeIsNull(11L)).thenReturn(Optional.of(aventurier(11, 200, 300)));
        when(disponibiliteService.estDisponibleSur(any(), any(Periode.class))).thenReturn(true);
        when(equipeMembreRepository.findByRequeteId(1L)).thenReturn(List.of(retire));

        Equipe equipe = service.composer(1L, List.of(10L, 11L), List.of());

        verify(equipeMembreRepository).deleteAll(List.of(retire));
        verify(equipeMembreRepository).saveAll(membresCaptor.capture());
        assertThat(membresCaptor.getValue())
                .extracting(EquipeMembreEntity::getAventurierId, EquipeMembreEntity::getTauxJournalierFigeBronze)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(10L, 200L), org.assertj.core.groups.Tuple.tuple(11L, 300L));

        // coût = (200 + 300) x 10 jours ; ti = 1,0 et 0,5 -> Tq = 0,75
        assertThat(equipe.getMembres()).hasSize(2);
        assertThat(equipe.getCoutBronze()).isEqualTo(5000L);
        assertThat(equipe.getChanceReussite()).isEqualByComparingTo(new BigDecimal("0.75"));
        assertThat(equipe.getRatioCoutPrime()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(equipe.getComplete()).isTrue();
        assertThat(equipe.getValidee()).isFalse();
    }

    @Test
    void composer_refuse_un_aventurier_indisponible_et_n_enregistre_rien() {
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requeteNouvelle()));
        when(aventurierRepository.findByIdAndSupprimeLeIsNull(10L)).thenReturn(Optional.of(aventurier(10, 50, 200)));
        when(aventurierRepository.findByIdAndSupprimeLeIsNull(11L)).thenReturn(Optional.of(aventurier(11, 200, 300)));
        when(disponibiliteService.estDisponibleSur(eq(10L), any(Periode.class))).thenReturn(true);
        when(disponibiliteService.estDisponibleSur(eq(11L), any(Periode.class))).thenReturn(false);

        assertThatThrownBy(() -> service.composer(1L, List.of(10L, 11L), List.of()))
                .isInstanceOf(ConflitMetierException.class);

        verify(equipeMembreRepository, never()).saveAll(any());
        verify(equipeMembreRepository, never()).deleteAll(any());
    }

    @Test
    void composer_refuse_un_aventurier_inconnu() {
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requeteNouvelle()));
        when(aventurierRepository.findByIdAndSupprimeLeIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.composer(1L, List.of(99L), List.of()))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    void composer_refuse_une_requete_qui_n_est_plus_nouvelle() {
        when(requeteRepository.findById(1L))
                .thenReturn(Optional.of(requete(StatutRequete.EQUIPE_VALIDEE, LocalDate.now().plusDays(5))));

        assertThatThrownBy(() -> service.composer(1L, List.of(10L), List.of()))
                .isInstanceOf(ConflitMetierException.class);
    }

    @Test
    void composer_refuse_une_requete_inconnue() {
        when(requeteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.composer(99L, List.of(10L), List.of()))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ---------- consulter ----------

    @Test
    void consulter_une_equipe_vide_n_a_pas_de_chance_de_reussite() {
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requeteNouvelle()));
        when(equipeMembreRepository.findByRequeteId(1L)).thenReturn(List.of());

        Equipe equipe = service.consulter(1L);

        assertThat(equipe.getMembres()).isEmpty();
        assertThat(equipe.getChanceReussite()).isNull();
        assertThat(equipe.getRatioCoutPrime()).isNull();
        assertThat(equipe.getCoutBronze()).isZero();
        assertThat(equipe.getComplete()).isFalse();
    }

    @Test
    void consulter_une_requete_inconnue() {
        when(requeteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consulter(99L))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ---------- valider ----------

    @Test
    void valider_passe_la_requete_en_equipe_validee() {
        RequeteEntity requete = requeteNouvelle();
        EquipeMembreEntity membre = membre(10L);
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requete));
        when(equipeMembreRepository.findByRequeteId(1L)).thenReturn(List.of(membre));
        when(aventurierRepository.findAllById(anyIterable())).thenReturn(List.of(aventurier(10, 50, 200)));
        when(disponibiliteService.estDisponibleSur(eq(10L), any(Periode.class))).thenReturn(true);

        Equipe equipe = service.valider(1L);

        assertThat(requete.getStatut()).isEqualTo(StatutRequete.EQUIPE_VALIDEE);
        assertThat(membre.getTauxJournalierFigeBronze()).isEqualTo(200L);
        assertThat(equipe.getValidee()).isTrue();
    }

    @Test
    void valider_refuse_une_equipe_incomplete() {
        RequeteEntity requete = requeteNouvelle();
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requete));
        when(equipeMembreRepository.findByRequeteId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.valider(1L)).isInstanceOf(ConflitMetierException.class);
        assertThat(requete.getStatut()).isEqualTo(StatutRequete.NOUVELLE);
    }

    @Test
    void valider_refuse_une_requete_expiree() {
        when(requeteRepository.findById(1L))
                .thenReturn(Optional.of(requete(StatutRequete.NOUVELLE, LocalDate.now().minusDays(1))));

        assertThatThrownBy(() -> service.valider(1L)).isInstanceOf(ConflitMetierException.class);
    }

    @Test
    void valider_refuse_une_requete_qui_n_est_plus_nouvelle() {
        when(requeteRepository.findById(1L))
                .thenReturn(Optional.of(requete(StatutRequete.EN_COURS, LocalDate.now().plusDays(5))));

        assertThatThrownBy(() -> service.valider(1L)).isInstanceOf(ConflitMetierException.class);
    }

    @Test
    void valider_refuse_si_un_membre_n_est_plus_disponible() {
        RequeteEntity requete = requeteNouvelle();
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requete));
        when(equipeMembreRepository.findByRequeteId(1L)).thenReturn(List.of(membre(10L)));
        when(aventurierRepository.findAllById(anyIterable())).thenReturn(List.of(aventurier(10, 50, 200)));
        when(disponibiliteService.estDisponibleSur(eq(10L), any(Periode.class))).thenReturn(false);

        assertThatThrownBy(() -> service.valider(1L)).isInstanceOf(ConflitMetierException.class);
        assertThat(requete.getStatut()).isEqualTo(StatutRequete.NOUVELLE);
    }

    // ---------- équipements ----------

    @Test
    void composer_enregistre_les_equipements_disponibles() {
        RequeteEntity requete = requeteNouvelle();
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requete));
        when(equipementRepository.findById(5L)).thenReturn(Optional.of(equipement(5, StatutEquipement.DISPONIBLE)));
        when(equipeMembreRepository.findByRequeteId(1L)).thenReturn(List.of());

        Equipe equipe = service.composer(1L, List.of(), List.of(5L));

        assertThat(requete.getEquipementIds()).containsExactly(5L);
        assertThat(equipe.getEquipementIds()).containsExactly(5L);
    }

    @Test
    void composer_sans_equipementIds_vide_les_equipements() {
        RequeteEntity requete = requeteNouvelle();
        requete.getEquipementIds().add(5L);
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requete));
        when(equipeMembreRepository.findByRequeteId(1L)).thenReturn(List.of());

        service.composer(1L, List.of(), null);

        assertThat(requete.getEquipementIds()).isEmpty();
    }

    @Test
    void composer_refuse_un_equipement_indisponible_et_n_enregistre_rien() {
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requeteNouvelle()));
        when(equipementRepository.findById(5L)).thenReturn(Optional.of(equipement(5, StatutEquipement.EMPRUNTE)));

        assertThatThrownBy(() -> service.composer(1L, List.of(), List.of(5L)))
                .isInstanceOf(ConflitMetierException.class);

        verify(equipeMembreRepository, never()).saveAll(any());
    }

    @Test
    void composer_refuse_un_equipement_inconnu() {
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requeteNouvelle()));
        when(equipementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.composer(1L, List.of(), List.of(99L)))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    void valider_reserve_les_equipements() {
        RequeteEntity requete = requeteNouvelle();
        requete.getEquipementIds().add(5L);
        EquipementEntity equipement = equipement(5, StatutEquipement.DISPONIBLE);
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requete));
        when(equipeMembreRepository.findByRequeteId(1L)).thenReturn(List.of(membre(10L)));
        when(aventurierRepository.findAllById(anyIterable())).thenReturn(List.of(aventurier(10, 50, 200)));
        when(disponibiliteService.estDisponibleSur(eq(10L), any(Periode.class))).thenReturn(true);
        when(equipementRepository.findById(5L)).thenReturn(Optional.of(equipement));

        service.valider(1L);

        assertThat(equipement.getStatut()).isEqualTo(StatutEquipement.EMPRUNTE);
        assertThat(requete.getStatut()).isEqualTo(StatutRequete.EQUIPE_VALIDEE);
    }

    @Test
    void valider_refuse_si_un_equipement_n_est_plus_disponible() {
        RequeteEntity requete = requeteNouvelle();
        requete.getEquipementIds().add(5L);
        when(requeteRepository.findById(1L)).thenReturn(Optional.of(requete));
        when(equipeMembreRepository.findByRequeteId(1L)).thenReturn(List.of(membre(10L)));
        when(aventurierRepository.findAllById(anyIterable())).thenReturn(List.of(aventurier(10, 50, 200)));
        when(disponibiliteService.estDisponibleSur(eq(10L), any(Periode.class))).thenReturn(true);
        when(equipementRepository.findById(5L)).thenReturn(Optional.of(equipement(5, StatutEquipement.EN_REPARATION)));

        assertThatThrownBy(() -> service.valider(1L)).isInstanceOf(ConflitMetierException.class);
        assertThat(requete.getStatut()).isEqualTo(StatutRequete.NOUVELLE);
    }
}
