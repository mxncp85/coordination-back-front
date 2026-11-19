package fr.capuches.dopale.commun.exception;

import java.util.List;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import fr.capuches.dopale.api.model.Erreur;
import fr.capuches.dopale.api.model.Erreur.CodeEnum;
import fr.capuches.dopale.api.model.ErreurDetailsInner;
import jakarta.validation.ConstraintViolationException;

/**
 * Transforme toutes les erreurs en réponse au format {@code Erreur} du contrat : {@code {code, message, details[]}}.
 * Les services lèvent seulement les exceptions de ce package, sans construire de ResponseEntity eux-mêmes.
 */
@RestControllerAdvice
public class GestionnaireErreurs {

	private static final String MESSAGE_VALIDATION = "Requête invalide";

	@ExceptionHandler(RessourceIntrouvableException.class)
	ResponseEntity<Erreur> introuvable(RessourceIntrouvableException ex) {
		return reponse(HttpStatus.NOT_FOUND, CodeEnum.NOT_FOUND, ex.getMessage(), List.of());
	}

	@ExceptionHandler(ConflitMetierException.class)
	ResponseEntity<Erreur> conflit(ConflitMetierException ex) {
		return reponse(HttpStatus.CONFLICT, CodeEnum.CONFLICT, ex.getMessage(), List.of());
	}

	@ExceptionHandler(AucuneSolutionException.class)
	ResponseEntity<Erreur> aucuneSolution(AucuneSolutionException ex) {
		return reponse(HttpStatus.UNPROCESSABLE_CONTENT, CodeEnum.NO_SOLUTION, ex.getMessage(), List.of());
	}

	@ExceptionHandler(ValidationMetierException.class)
	ResponseEntity<Erreur> validationMetier(ValidationMetierException ex) {
		return validation(List.of(detail(ex.getChamp(), ex.getMessage())));
	}

	/** Corps JSON qui ne respecte pas les contraintes du contrat (@NotNull, @Min…). */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Erreur> corpsInvalide(MethodArgumentNotValidException ex) {
		List<ErreurDetailsInner> details = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> detail(e.getField(), e.getDefaultMessage()))
				.toList();
		return validation(details);
	}

	/** Paramètre d'URL ou de query qui ne respecte pas les contraintes du contrat (ex. size > 100). */
	@ExceptionHandler(HandlerMethodValidationException.class)
	ResponseEntity<Erreur> parametresInvalides(HandlerMethodValidationException ex) {
		List<ErreurDetailsInner> details = ex.getParameterValidationResults().stream()
				.flatMap(resultat -> resultat.getResolvableErrors().stream()
						.map(erreur -> detail(nomChamp(erreur, resultat.getMethodParameter().getParameterName()),
								erreur.getDefaultMessage())))
				.toList();
		return validation(details);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<Erreur> contraintesInvalides(ConstraintViolationException ex) {
		List<ErreurDetailsInner> details = ex.getConstraintViolations().stream()
				.map(v -> detail(dernierSegment(v.getPropertyPath().toString()), v.getMessage()))
				.toList();
		return validation(details);
	}

	/** Valeur du mauvais type, par ex. {@code ?statut=INCONNU} ou {@code /requetes/abc}. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<Erreur> mauvaisType(MethodArgumentTypeMismatchException ex) {
		return validation(List.of(detail(ex.getName(), "Valeur invalide : " + ex.getValue())));
	}

	/** JSON mal formé ou valeur d'enum inconnue dans le corps. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<Erreur> corpsIllisible(HttpMessageNotReadableException ex) {
		return reponse(HttpStatus.BAD_REQUEST, CodeEnum.VALIDATION, "Corps de requête illisible ou mal formé", List.of());
	}

	private static ResponseEntity<Erreur> validation(List<ErreurDetailsInner> details) {
		return reponse(HttpStatus.BAD_REQUEST, CodeEnum.VALIDATION, MESSAGE_VALIDATION, details);
	}

	private static ResponseEntity<Erreur> reponse(HttpStatus statut, CodeEnum code, String message,
			List<ErreurDetailsInner> details) {
		Erreur erreur = new Erreur(code, message).details(details);
		return ResponseEntity.status(statut).body(erreur);
	}

	private static ErreurDetailsInner detail(String champ, String message) {
		return new ErreurDetailsInner(message).champ(champ);
	}

	private static String nomChamp(MessageSourceResolvable erreur, String parDefaut) {
		return erreur instanceof FieldError fieldError ? fieldError.getField() : parDefaut;
	}

	private static String dernierSegment(String chemin) {
		int point = chemin.lastIndexOf('.');
		return point >= 0 ? chemin.substring(point + 1) : chemin;
	}
}
