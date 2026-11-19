package fr.capuches.dopale.commun.exception;

/**
 * Aucune solution possible (ex. suggestion d'équipe impossible) : renvoie 422 avec le code {@code NO_SOLUTION}.
 */
public class AucuneSolutionException extends RuntimeException {

	public AucuneSolutionException(String message) {
		super(message);
	}
}
