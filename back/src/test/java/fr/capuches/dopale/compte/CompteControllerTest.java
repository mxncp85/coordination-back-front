package fr.capuches.dopale.compte;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import fr.capuches.dopale.api.model.TypeTransaction;
import fr.capuches.dopale.commun.exception.GestionnaireErreurs;
import fr.capuches.dopale.commun.exception.ValidationMetierException;

class CompteControllerTest {

	private CompteService compteService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		compteService = mock(CompteService.class);
		mockMvc = MockMvcBuilders
				.standaloneSetup(new CompteController(compteService, new TransactionMapper()))
				.setControllerAdvice(new GestionnaireErreurs())
				.build();
	}

	private TransactionEntity achatEpee() {
		TransactionEntity achat = TransactionEntity.debit(5000, "Achat : Épée longue", TypeTransaction.ACHAT)
				.pourEquipement(7L);
		ReflectionTestUtils.setField(achat, "id", 1L);
		return achat;
	}

	@Test
	void solde_renvoieLaSomme() throws Exception {
		when(compteService.solde()).thenReturn(-5000L);

		mockMvc.perform(get("/api/comptes/solde"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.soldeBronze").value(-5000));
	}

	@Test
	void historique_avecFiltres() throws Exception {
		when(compteService.lister(eq(TypeTransaction.ACHAT), eq(LocalDate.of(2026, 11, 1)),
				eq(LocalDate.of(2026, 11, 30)), eq(3L), any()))
				.thenReturn(new PageImpl<>(List.of(achatEpee()), PageRequest.of(0, 20), 1));

		mockMvc.perform(get("/api/comptes/transactions")
				.param("type", "ACHAT")
				.param("from", "2026-11-01")
				.param("to", "2026-11-30")
				.param("requeteId", "3"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].montantBronze").value(-5000))
				.andExpect(jsonPath("$.content[0].type").value("ACHAT"))
				.andExpect(jsonPath("$.content[0].equipementId").value(7));
	}

	@Test
	void historique_avecFromApresTo_renvoie400() throws Exception {
		when(compteService.lister(any(), any(), any(), any(), any()))
				.thenThrow(new ValidationMetierException("from", "from doit être antérieur ou égal à to"));

		mockMvc.perform(get("/api/comptes/transactions").param("from", "2026-12-01").param("to", "2026-11-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"))
				.andExpect(jsonPath("$.details[0].champ").value("from"));
	}

	@Test
	void historique_avecUneDateInvalide_renvoie400() throws Exception {
		mockMvc.perform(get("/api/comptes/transactions").param("from", "pas-une-date"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"));
	}

	@Test
	void saisieManuelle_debit_renvoie201() throws Exception {
		TransactionEntity loyer = TransactionEntity.debit(1000, "Loyer", TypeTransaction.MANUEL);
		when(compteService.saisirManuellement(1000L, false, "Loyer")).thenReturn(loyer);

		mockMvc.perform(post("/api/comptes/transactions")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"montantBronze":1000,"sens":"DEBIT","libelle":"Loyer"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.montantBronze").value(-1000))
				.andExpect(jsonPath("$.type").value("MANUEL"));
	}

	@Test
	void saisieManuelle_invalide_renvoie400() throws Exception {
		mockMvc.perform(post("/api/comptes/transactions")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"montantBronze":0,"sens":"CREDIT","libelle":""}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.details[?(@.champ == 'montantBronze')]").exists())
				.andExpect(jsonPath("$.details[?(@.champ == 'libelle')]").exists());

		verify(compteService, never()).saisirManuellement(anyLong(), anyBoolean(), anyString());
	}
}
