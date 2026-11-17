package fr.capuches.dopale.commun.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.capuches.dopale.api.model.RequeteInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;

class GestionnaireErreursTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new ControleurDeTest())
				.setControllerAdvice(new GestionnaireErreurs())
				.build();
	}

	@Test
	void ressourceIntrouvable_renvoie404NotFound() throws Exception {
		mockMvc.perform(get("/test/introuvable"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Requête 42 introuvable"));
	}

	@Test
	void conflitMetier_renvoie409Conflict() throws Exception {
		mockMvc.perform(get("/test/conflit"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CONFLICT"));
	}

	@Test
	void aucuneSolution_renvoie422NoSolution() throws Exception {
		mockMvc.perform(get("/test/aucune-solution"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.code").value("NO_SOLUTION"));
	}

	@Test
	void validationMetier_renvoie400AvecLeChamp() throws Exception {
		mockMvc.perform(get("/test/validation-metier"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"))
				.andExpect(jsonPath("$.details[0].champ").value("primeMin"));
	}

	@Test
	void corpsQuiNeRespectePasLeContrat_renvoie400AvecLesChampsFautifs() throws Exception {
		mockMvc.perform(post("/test/corps").contentType(MediaType.APPLICATION_JSON).content("{\"nom\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"))
				.andExpect(jsonPath("$.details[?(@.champ == 'nom')]").exists())
				.andExpect(jsonPath("$.details[?(@.champ == 'primeBronze')]").exists());
	}

	@Test
	void jsonMalForme_renvoie400() throws Exception {
		mockMvc.perform(post("/test/corps").contentType(MediaType.APPLICATION_JSON).content("{pas du json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"));
	}

	@Test
	void parametreHorsLimites_renvoie400() throws Exception {
		mockMvc.perform(get("/test/parametre").param("size", "500"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"))
				.andExpect(jsonPath("$.details[0].champ").value("size"));
	}

	@Test
	void identifiantDuMauvaisType_renvoie400() throws Exception {
		mockMvc.perform(get("/test/type/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"))
				.andExpect(jsonPath("$.details[0].champ").value("id"));
	}

	@RestController
	static class ControleurDeTest {

		@GetMapping("/test/introuvable")
		void introuvable() {
			throw new RessourceIntrouvableException("Requête 42 introuvable");
		}

		@GetMapping("/test/conflit")
		void conflit() {
			throw new ConflitMetierException("La requête n'est plus modifiable");
		}

		@GetMapping("/test/aucune-solution")
		void aucuneSolution() {
			throw new AucuneSolutionException("Aucune équipe complète possible");
		}

		@GetMapping("/test/validation-metier")
		void validationMetier() {
			throw new ValidationMetierException("primeMin", "primeMin doit être inférieur ou égal à primeMax");
		}

		@PostMapping("/test/corps")
		void corps(@Valid @RequestBody RequeteInput input) {
			// la validation échoue avant d'arriver ici
		}

		@GetMapping("/test/parametre")
		void parametre(@Max(100) @RequestParam("size") Integer size) {
			// la validation échoue avant d'arriver ici
		}

		@GetMapping("/test/type/{id}")
		void type(@PathVariable("id") Long id) {
			// la conversion échoue avant d'arriver ici
		}
	}
}
