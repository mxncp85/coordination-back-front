package fr.capuches.dopale.web;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdventurerControllerTest {

	private static final String LYRA = "11111111-1111-1111-1111-111111111111";
	private static final String SELENE = "33333333-3333-3333-3333-333333333333";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void scheduleReturnsStatusMissionsRestsAndCalendar() throws Exception {
		mockMvc.perform(get("/api/adventurers/{id}/schedule", LYRA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.adventurerId").value(LYRA))
				.andExpect(jsonPath("$.name").value("Lyra Venteclair"))
				.andExpect(jsonPath("$.status.state").exists())
				.andExpect(jsonPath("$.missions").isArray())
				.andExpect(jsonPath("$.restPeriods").isArray())
				.andExpect(jsonPath("$.calendar").isArray());
	}

	@Test
	void scheduleNotFound() throws Exception {
		mockMvc.perform(get("/api/adventurers/{id}/schedule", "99999999-9999-9999-9999-999999999999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void availabilityRequiresFromAndTo() throws Exception {
		mockMvc.perform(get("/api/adventurers/availability"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void availabilityListsAdventurersForPeriod() throws Exception {
		mockMvc.perform(get("/api/adventurers/availability")
						.param("from", "2099-01-01")
						.param("to", "2099-01-05"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.from").value("2099-01-01"))
				.andExpect(jsonPath("$.to").value("2099-01-05"))
				.andExpect(jsonPath("$.adventurers").isArray())
				.andExpect(jsonPath("$.adventurers[?(@.id == '" + SELENE + "')].available", hasItem(true)));
	}
}
