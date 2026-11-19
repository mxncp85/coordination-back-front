package fr.capuches.dopale.requete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.capuches.dopale.api.model.RequeteInput;
import fr.capuches.dopale.api.model.StatutRequete;
import fr.capuches.dopale.commun.exception.ConflitMetierException;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;
import fr.capuches.dopale.commun.exception.ValidationMetierException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class RequeteServiceTest {

    @Mock
    private RequeteRepository repository;

    private RequeteService service;

    @BeforeEach
    void init() {
        service = new RequeteService(repository, new RequeteMapper());
    }

    // ---------- jeux de données ----------

    private static RequeteInput saisie(LocalDate datePeremption) {
        return new RequeteInput()
                .nom("Escorte de caravane")
                .description("Escorter une caravane jusqu'à Brumevert")
                .dureeJours(5)
                .datePeremption(datePeremption)
                .primeBronze(150_000L)
                .commanditaire("Village de Brumevert");
    }

    private static RequeteEntity requeteAuStatut(StatutRequete statut) {
        RequeteEntity requete = new RequeteEntity();
        requete.setNom("Ancien nom");
        requete.setDescription("Ancienne description");
        requete.setDureeJours(3);
        requete.setDatePeremption(LocalDate.now().plusDays(5));
        requete.setPrimeBronze(1_000);
        requete.setCommanditaire("Ancien client");
        requete.setStatut(statut);
        return requete;
    }

    private static FiltreRequete sansFiltre() {
        return new FiltreRequete(null, null, null, null, null, null);
    }

    // ---------- création ----------

    @Test
    void creer_enregistre_la_requete_au_statut_nouvelle() {
        when(repository.save(any(RequeteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RequeteEntity creee = service.creer(saisie(LocalDate.now().plusDays(10)));

        ArgumentCaptor<RequeteEntity> enregistree = ArgumentCaptor.forClass(RequeteEntity.class);
        verify(repository).save(enregistree.capture());
        assertThat(enregistree.getValue().getStatut()).isEqualTo(StatutRequete.NOUVELLE);
        assertThat(creee.getNom()).isEqualTo("Escorte de caravane");
        assertThat(creee.getPrimeBronze()).isEqualTo(150_000L);
    }

    @Test
    void creer_accepte_une_date_de_peremption_aujourdhui() {
        when(repository.save(any(RequeteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RequeteEntity creee = service.creer(saisie(LocalDate.now()));

        assertThat(creee.getDatePeremption()).isEqualTo(LocalDate.now());
    }

    @Test
    void creer_refuse_une_date_de_peremption_passee() {
        RequeteInput saisie = saisie(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> service.creer(saisie))
                .isInstanceOfSatisfying(ValidationMetierException.class,
                        e -> assertThat(e.getChamp()).isEqualTo("datePeremption"));
        verify(repository, never()).save(any());
    }

    // ---------- modification ----------

    @Test
    void modifier_met_a_jour_une_requete_nouvelle() {
        when(repository.findById(1L)).thenReturn(Optional.of(requeteAuStatut(StatutRequete.NOUVELLE)));

        RequeteEntity modifiee = service.modifier(1L, saisie(LocalDate.now().plusDays(10)));

        assertThat(modifiee.getNom()).isEqualTo("Escorte de caravane");
        assertThat(modifiee.getDureeJours()).isEqualTo(5);
        assertThat(modifiee.getStatut()).isEqualTo(StatutRequete.NOUVELLE);
    }

    @Test
    void modifier_est_autorise_quand_l_equipe_est_validee() {
        when(repository.findById(1L)).thenReturn(Optional.of(requeteAuStatut(StatutRequete.EQUIPE_VALIDEE)));

        RequeteEntity modifiee = service.modifier(1L, saisie(LocalDate.now().plusDays(10)));

        assertThat(modifiee.getStatut()).isEqualTo(StatutRequete.EQUIPE_VALIDEE);
    }

    @ParameterizedTest
    @EnumSource(value = StatutRequete.class, names = { "EN_COURS", "REUSSIE", "ECHOUEE", "REJETEE" })
    void modifier_est_refuse_une_fois_la_mission_partie_ou_terminee(StatutRequete statut) {
        RequeteEntity existante = requeteAuStatut(statut);
        when(repository.findById(1L)).thenReturn(Optional.of(existante));
        RequeteInput saisie = saisie(LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> service.modifier(1L, saisie)).isInstanceOf(ConflitMetierException.class);
        assertThat(existante.getNom()).isEqualTo("Ancien nom");
    }

    @Test
    void modifier_applique_les_memes_regles_de_date_qu_a_la_creation() {
        when(repository.findById(1L)).thenReturn(Optional.of(requeteAuStatut(StatutRequete.NOUVELLE)));
        RequeteInput saisie = saisie(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> service.modifier(1L, saisie)).isInstanceOf(ValidationMetierException.class);
    }

    @Test
    void modifier_renvoie_404_meme_avec_une_date_passee_si_la_requete_n_existe_pas() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        RequeteInput saisie = saisie(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> service.modifier(99L, saisie))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessageContaining("99");
    }

    // ---------- consultation ----------

    @Test
    void trouver_renvoie_la_requete() {
        RequeteEntity existante = requeteAuStatut(StatutRequete.NOUVELLE);
        when(repository.findById(1L)).thenReturn(Optional.of(existante));

        assertThat(service.trouver(1L)).isSameAs(existante);
    }

    @Test
    void trouver_renvoie_404_si_la_requete_n_existe_pas() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouver(99L)).isInstanceOf(RessourceIntrouvableException.class);
    }

    // ---------- liste : tri, pagination, filtres ----------

    private Pageable listerEtCapturerLePageable(FiltreRequete filtre, String tri, String ordre, int page, int size) {
        when(repository.findAll(ArgumentMatchers.<Specification<RequeteEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<RequeteEntity> resultat = service.lister(filtre, tri, ordre, page, size);

        assertThat(resultat).isEmpty();
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<RequeteEntity>>any(), pageable.capture());
        return pageable.getValue();
    }

    @Test
    void lister_transmet_la_page_et_la_taille_demandees() {
        Pageable pageable = listerEtCapturerLePageable(sansFiltre(), "echeance", "asc", 2, 5);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
    }

    @Test
    void lister_trie_par_echeance_croissante_avec_un_tri_secondaire_sur_l_id() {
        Pageable pageable = listerEtCapturerLePageable(sansFiltre(), "echeance", "asc", 0, 20);

        assertThat(pageable.getSort().getOrderFor("datePeremption").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(pageable.getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    void lister_trie_par_prime_decroissante() {
        Pageable pageable = listerEtCapturerLePageable(sansFiltre(), "prime", "desc", 0, 20);

        assertThat(pageable.getSort().getOrderFor("primeBronze").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void lister_trie_par_niveau() {
        Pageable pageable = listerEtCapturerLePageable(sansFiltre(), "niveau", "asc", 0, 20);

        assertThat(pageable.getSort().getOrderFor("expRecommandee").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void lister_refuse_une_fourchette_de_prime_inversee() {
        FiltreRequete filtre = new FiltreRequete(null, 500L, 100L, null, null, null);

        assertThatThrownBy(() -> service.lister(filtre, "echeance", "asc", 0, 20))
                .isInstanceOfSatisfying(ValidationMetierException.class,
                        e -> assertThat(e.getChamp()).isEqualTo("primeMin"));
        verifyNoInteractions(repository);
    }

    @Test
    void lister_accepte_une_fourchette_de_prime_avec_des_bornes_egales() {
        FiltreRequete filtre = new FiltreRequete(null, 100L, 100L, null, null, null);

        Pageable pageable = listerEtCapturerLePageable(filtre, "echeance", "asc", 0, 20);

        assertThat(pageable.getPageSize()).isEqualTo(20);
    }
}
