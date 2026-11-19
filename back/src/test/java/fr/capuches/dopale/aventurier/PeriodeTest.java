package fr.capuches.dopale.aventurier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PeriodeTest {

	@Test
	void contientBornesInclusives() {
		Periode p = new Periode(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
		assertTrue(p.contient(LocalDate.of(2026, 11, 10)));
		assertTrue(p.contient(LocalDate.of(2026, 11, 12)));
		assertFalse(p.contient(LocalDate.of(2026, 11, 13)));
	}

	@Test
	void chevaucheQuandPeriodesSeTouchent() {
		Periode a = new Periode(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10));
		Periode b = new Periode(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15));
		Periode c = new Periode(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 20));
		assertTrue(a.chevauche(b));
		assertFalse(a.chevauche(c));
	}

	@Test
	void rejettePeriodeInversee() {
		assertThrows(IllegalArgumentException.class,
				() -> new Periode(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1)));
	}
}
