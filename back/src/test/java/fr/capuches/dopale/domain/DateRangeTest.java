package fr.capuches.dopale.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DateRangeTest {

	@Test
	void containsInclusiveBounds() {
		DateRange range = new DateRange(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
		assertTrue(range.contains(LocalDate.of(2026, 11, 10)));
		assertTrue(range.contains(LocalDate.of(2026, 11, 12)));
		assertFalse(range.contains(LocalDate.of(2026, 11, 9)));
		assertFalse(range.contains(LocalDate.of(2026, 11, 13)));
	}

	@Test
	void overlapsWhenPeriodsTouchOrCross() {
		DateRange a = new DateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10));
		DateRange b = new DateRange(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15));
		DateRange c = new DateRange(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 20));
		assertTrue(a.overlaps(b));
		assertFalse(a.overlaps(c));
	}

	@Test
	void rejectsInvertedRange() {
		assertThrows(IllegalArgumentException.class,
				() -> new DateRange(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1)));
	}
}
