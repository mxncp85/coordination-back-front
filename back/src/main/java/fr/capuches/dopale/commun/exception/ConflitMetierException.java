package fr.capuches.dopale.commun.exception;

/**
 * Action impossible dans l'état actuel (mauvais statut, ressource déjà réservée, solde insuffisant…) :
 * renvoie 409 avec le code {@code CONFLICT}.
 */
public class ConflitMetierException extends RuntimeException {

	public ConflitMetierException(String message) {
		super(message);
	}
}
