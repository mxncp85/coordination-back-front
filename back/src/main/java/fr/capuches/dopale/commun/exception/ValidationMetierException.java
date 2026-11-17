package fr.capuches.dopale.commun.exception;

/**
 * Entrée invalide que les annotations du contrat ne peuvent pas détecter
 * (ex. {@code primeMin > primeMax}, date de péremption passée) : renvoie 400 avec le code {@code VALIDATION}.
 */
public class ValidationMetierException extends RuntimeException {

	private final String champ;

	public ValidationMetierException(String champ, String message) {
		super(message);
		this.champ = champ;
	}

	public String getChamp() {
		return champ;
	}
}
