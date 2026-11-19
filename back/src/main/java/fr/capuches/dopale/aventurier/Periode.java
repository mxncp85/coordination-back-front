package fr.capuches.dopale.aventurier;

import java.time.LocalDate;
import java.util.Objects;

/** Période inclusive en jours entiers [debut, fin]. */
public record Periode(LocalDate debut, LocalDate fin) {

	public Periode {
		Objects.requireNonNull(debut, "debut");
		Objects.requireNonNull(fin, "fin");
		if (fin.isBefore(debut)) {
			throw new IllegalArgumentException("La fin doit être après ou égale au début");
		}
	}

	public boolean contient(LocalDate jour) {
		return !jour.isBefore(debut) && !jour.isAfter(fin);
	}

	public boolean chevauche(Periode autre) {
		return !debut.isAfter(autre.fin) && !autre.debut.isAfter(fin);
	}
}
