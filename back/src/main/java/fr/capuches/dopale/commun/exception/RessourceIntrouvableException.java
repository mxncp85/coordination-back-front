package fr.capuches.dopale.commun.exception;

/**
 * Ressource absente : renvoie 404 avec le code {@code NOT_FOUND}.
 * Exemple : {@code throw new RessourceIntrouvableException("Requête " + id + " introuvable");}
 */
public class RessourceIntrouvableException extends RuntimeException {

	public RessourceIntrouvableException(String message) {
		super(message);
	}
}
