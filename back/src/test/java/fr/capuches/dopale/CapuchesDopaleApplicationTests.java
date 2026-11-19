package fr.capuches.dopale;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Smoke test sans PostgreSQL. Les tests d'intégration JPA/Flyway nécessitent la base
 * (Docker Compose service {@code db}).
 */
class CapuchesDopaleApplicationTests {

	@Test
	void applicationClassIsPresent() {
		assertNotNull(CapuchesDopaleApplication.class);
	}
}
