package fr.capuches.dopale.aventurier;

import java.time.LocalDate;

/**
 * Règle US-21 : après une mission, l'aventurier est indisponible pendant son repos.
 * Durées en jours entiers.
 *
 * <p>Jours de repos = {@code max(1, ceil(dureeMission × Exp_rec / Exp_aventurier))} ;
 * si {@code Exp_rec = 0}, repos minimal d'1 jour.
 */
public final class CalculRepos {

	private CalculRepos() {
	}

	public static int jours(int dureeMissionJours, int expRecommandee, int experienceAventurier) {
		if (dureeMissionJours < 1) {
			throw new IllegalArgumentException("La durée de mission doit être >= 1");
		}
		if (experienceAventurier < 1) {
			throw new IllegalArgumentException("L'expérience doit être >= 1");
		}
		if (expRecommandee <= 0) {
			return 1;
		}
		return Math.max(1, (int) Math.ceil(dureeMissionJours * (double) expRecommandee / experienceAventurier));
	}

	/**
	 * Le repos commence le lendemain de la clôture et dure {@code jours} jours inclusifs.
	 * {@code reposJusquAu} = dernier jour de repos ; {@code disponibleLe} = lendemain.
	 */
	public static LocalDate reposJusquAu(LocalDate dateCloture, int joursRepos) {
		return dateCloture.plusDays(joursRepos);
	}

	public static LocalDate disponibleLe(LocalDate reposJusquAu) {
		return reposJusquAu == null ? null : reposJusquAu.plusDays(1);
	}
}
