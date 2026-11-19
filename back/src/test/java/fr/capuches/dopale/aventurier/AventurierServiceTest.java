package fr.capuches.dopale.aventurier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import fr.capuches.dopale.api.model.Aventurier;
import fr.capuches.dopale.api.model.AventurierInput;
import fr.capuches.dopale.api.model.ListAventuriers200Response;
import fr.capuches.dopale.api.model.Specialite;
import fr.capuches.dopale.api.model.StatutAventurier;
import fr.capuches.dopale.aventurier.DisponibiliteService.StatutDisponibilite;
import fr.capuches.dopale.commun.exception.ConflitMetierException;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;
import fr.capuches.dopale.requete.EquipeMembreRepository;

@ExtendWith(MockitoExtension.class)
class AventurierServiceTest {

	@Mock
	private AventurierRepository repository;

	@Mock
	private DisponibiliteService disponibiliteService;

	@Mock
	private EquipeMembreRepository equipeMembreRepository;

	private AventurierService service;

	@BeforeEach
	void setUp() {
		service = new AventurierService(repository, new AventurierMapper(), disponibiliteService,
				equipeMembreRepository);
	}

	@Test
	void create_enregistreNomTauxExperienceEtSpecialites_etStatutDisponible() {
		AventurierInput input = new AventurierInput("Lyra", List.of(Specialite.ARCHER, Specialite.MESSAGER), 12, 100L);
		when(repository.saveAndFlush(any(AventurierEntity.class))).thenAnswer(invocation -> {
			AventurierEntity entity = invocation.getArgument(0);
			ReflectionTestUtils.setField(entity, "id", 1L);
			ReflectionTestUtils.setField(entity, "tauxJournalierBronze", 150L);
			return entity;
		});
		when(disponibiliteService.calculerStatut(any(AventurierEntity.class), any()))
				.thenReturn(new StatutDisponibilite(StatutAventurier.DISPONIBLE, null));

		Aventurier cree = service.create(input);

		ArgumentCaptor<AventurierEntity> captor = ArgumentCaptor.forClass(AventurierEntity.class);
		verify(repository).saveAndFlush(captor.capture());
		AventurierEntity sauve = captor.getValue();
		assertEquals("Lyra", sauve.getNom());
		assertEquals(12, sauve.getExperience());
		assertEquals(100L, sauve.getTauxBaseBronze());
		assertEquals(Set.of("ARCHER", "MESSAGER"), sauve.getSpecialites());
		assertNull(sauve.getSupprimeLe());

		assertEquals(1L, cree.getId());
		assertEquals(StatutAventurier.DISPONIBLE, cree.getStatut());
		assertNull(cree.getDisponibleLe());
	}

	@Test
	void update_modifieLaFicheActive() {
		AventurierEntity existant = entity(2L, "Borin", 20, 80L, Set.of("BARBARE"));
		when(repository.findByIdAndSupprimeLeIsNull(2L)).thenReturn(Optional.of(existant));
		when(repository.saveAndFlush(existant)).thenReturn(existant);
		when(disponibiliteService.calculerStatut(existant, java.time.LocalDate.now()))
				.thenReturn(new StatutDisponibilite(StatutAventurier.DISPONIBLE, null));

		AventurierInput input = new AventurierInput("Borin Marteaufer", List.of(Specialite.BARBARE, Specialite.FORGERON),
				25, 90L);
		Aventurier maj = service.update(2L, input);

		assertEquals("Borin Marteaufer", existant.getNom());
		assertEquals(25, existant.getExperience());
		assertEquals(90L, existant.getTauxBaseBronze());
		assertTrue(existant.getSpecialites().contains("FORGERON"));
		assertEquals("Borin Marteaufer", maj.getNom());
	}

	@Test
	void archive_marqueSupprimeLe_etDisparaitDesListes() {
		AventurierEntity existant = entity(3L, "Sélène", 30, 120L, Set.of("MAGE_ARCANIQUE"));
		when(repository.findByIdAndSupprimeLeIsNull(3L)).thenReturn(Optional.of(existant));
		when(equipeMembreRepository.estDansEquipeActive(3L)).thenReturn(false);

		service.archive(3L);

		assertNotNull(existant.getSupprimeLe());
	}

	@Test
	void archive_refuseSiEnMission() {
		AventurierEntity existant = entity(4L, "Grom", 40, 110L, Set.of("PALADIN"));
		when(repository.findByIdAndSupprimeLeIsNull(4L)).thenReturn(Optional.of(existant));
		when(equipeMembreRepository.estDansEquipeActive(4L)).thenReturn(true);

		ConflitMetierException ex = assertThrows(ConflitMetierException.class, () -> service.archive(4L));
		assertTrue(ex.getMessage().contains("archiver"));
		assertNull(existant.getSupprimeLe());
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void list_exclutLesArchivesParDefaut() {
		AventurierEntity actif = entity(5L, "Actif", 10, 50L, Set.of("ARCHER"));
		when(repository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(actif)));
		when(disponibiliteService.calculerStatut(any(AventurierEntity.class), any()))
				.thenReturn(new StatutDisponibilite(StatutAventurier.DISPONIBLE, null));

		ListAventuriers200Response page = service.list(null, null, null, null, null, "nom", "asc", 0, 20);

		assertEquals(1, page.getContent().size());
		assertEquals("Actif", page.getContent().getFirst().getNom());
		verify(repository).findAll(any(Specification.class), any(Pageable.class));
	}

	@Test
	void get_refuseUnArchive() {
		when(repository.findByIdAndSupprimeLeIsNull(99L)).thenReturn(Optional.empty());
		assertThrows(RessourceIntrouvableException.class, () -> service.get(99L));
	}

	private static AventurierEntity entity(Long id, String nom, int exp, long taux, Set<String> specialites) {
		AventurierEntity entity = new AventurierEntity();
		ReflectionTestUtils.setField(entity, "id", id);
		entity.setNom(nom);
		entity.setExperience(exp);
		entity.setTauxBaseBronze(taux);
		entity.setSpecialites(new LinkedHashSet<>(specialites));
		ReflectionTestUtils.setField(entity, "tauxJournalierBronze", taux);
		return entity;
	}
}
