package fr.capuches.dopale.aventurier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CalculReposTest {

	@Test
	void joursUtiliseExpRecEtExperience() {
		// ceil(10 * 100 / 50) = ceil(20) = 20
		assertEquals(20, CalculRepos.jours(10, 100, 50));
	}

	@Test
	void joursMinimalQuandExpRecNulle() {
		assertEquals(1, CalculRepos.jours(7, 0, 40));
	}

	@Test
	void joursAuMoinsUn() {
		assertEquals(1, CalculRepos.jours(1, 1, 100));
	}

	@Test
	void reposJusquAuEtDisponibleLe() {
		LocalDate cloture = LocalDate.of(2026, 11, 17);
		LocalDate jusquAu = CalculRepos.reposJusquAu(cloture, 2);
		assertEquals(LocalDate.of(2026, 11, 19), jusquAu);
		assertEquals(LocalDate.of(2026, 11, 20), CalculRepos.disponibleLe(jusquAu));
	}

	@Test
	void rejetteDureeInvalide() {
		assertThrows(IllegalArgumentException.class, () -> CalculRepos.jours(0, 10, 10));
	}
}
