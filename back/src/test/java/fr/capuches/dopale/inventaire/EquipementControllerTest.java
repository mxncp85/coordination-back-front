package fr.capuches.dopale.inventaire;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import fr.capuches.dopale.api.model.StatutEquipement;
import fr.capuches.dopale.commun.exception.ConflitMetierException;
import fr.capuches.dopale.commun.exception.GestionnaireErreurs;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;

class EquipementControllerTest {

	private EquipementService equipementService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		equipementService = mock(EquipementService.class);
		mockMvc = MockMvcBuilders
				.standaloneSetup(new EquipementController(equipementService, new EquipementMapper()))
				.setControllerAdvice(new GestionnaireErreurs())
				.build();
	}

	private EquipementEntity epee(long id) {
		EquipementEntity epee = new EquipementEntity("Épée longue", "arme", 30, 5000);
		ReflectionTestUtils.setField(epee, "id", id);
		return epee;
	}

	@Test
	void acheter_renvoie201AvecLEquipement() throws Exception {
		when(equipementService.acheter("Épée longue", "arme", 30, 5000L)).thenReturn(epee(1));

		mockMvc.perform(post("/api/equipements")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nom":"Épée longue","type":"arme","durabiliteMax":30,"prixAchatBronze":5000}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.prixAchatBronze").value(5000))
				.andExpect(jsonPath("$.durabiliteActuelle").value(30))
				.andExpect(jsonPath("$.statut").value("DISPONIBLE"));
	}

	@Test
	void acheter_avecDesChampsInvalides_renvoie400() throws Exception {
		mockMvc.perform(post("/api/equipements")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nom":"","type":"arme","durabiliteMax":0,"prixAchatBronze":-1}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"))
				.andExpect(jsonPath("$.details[?(@.champ == 'nom')]").exists())
				.andExpect(jsonPath("$.details[?(@.champ == 'durabiliteMax')]").exists())
				.andExpect(jsonPath("$.details[?(@.champ == 'prixAchatBronze')]").exists());

		verify(equipementService, never()).acheter(anyString(), anyString(), anyInt(), anyLong());
	}

	@Test
	void lister_filtreParStatut() throws Exception {
		EquipementEntity epee = epee(1);
		epee.emprunter();
		when(equipementService.lister(eq(StatutEquipement.EMPRUNTE), any()))
				.thenReturn(new PageImpl<>(List.of(epee), PageRequest.of(0, 20), 1));

		mockMvc.perform(get("/api/equipements").param("statut", "EMPRUNTE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].statut").value("EMPRUNTE"));

		verify(equipementService).lister(eq(StatutEquipement.EMPRUNTE), any());
	}

	@Test
	void lister_avecUnStatutInconnu_renvoie400() throws Exception {
		mockMvc.perform(get("/api/equipements").param("statut", "CASSE"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"));
	}

	@Test
	void detail_dUnEquipementInconnu_renvoie404() throws Exception {
		when(equipementService.trouver(99L)).thenThrow(new RessourceIntrouvableException("Equipement 99 introuvable"));

		mockMvc.perform(get("/api/equipements/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

	@Test
	void reparer_unEquipementEnMission_renvoie409() throws Exception {
		when(equipementService.reparer(1L)).thenThrow(new ConflitMetierException("Equipement 1 en mission"));

		mockMvc.perform(post("/api/equipements/1/reparation"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CONFLICT"));
	}
}
