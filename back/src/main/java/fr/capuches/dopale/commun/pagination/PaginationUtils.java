package fr.capuches.dopale.commun.pagination;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Passage entre les paramètres de pagination du contrat ({@code page}, {@code size}, {@code tri}, {@code ordre})
 * et Spring Data, puis entre une {@link Page} et les réponses paginées générées
 * ({@code ListRequetes200Response}, {@code ListAventuriers200Response}…).
 *
 * <pre>{@code
 * Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(PaginationUtils.direction(ordre), "datePeremption"));
 * Page<RequeteEntity> resultat = repository.findAll(spec, pageable);
 * return PaginationUtils.versReponse(resultat, mapper::versDto, ListRequetes200Response::new);
 * }</pre>
 */
public final class PaginationUtils {

	private PaginationUtils() {
	}

	public static Pageable pageable(Integer page, Integer size, Sort tri) {
		return PageRequest.of(page, size, tri);
	}

	/** {@code "desc"} → décroissant ; toute autre valeur (dont {@code null}) → croissant, valeur par défaut du contrat. */
	public static Sort.Direction direction(String ordre) {
		return "desc".equalsIgnoreCase(ordre) ? Sort.Direction.DESC : Sort.Direction.ASC;
	}

	/** Constructeur commun à toutes les réponses paginées générées depuis le contrat. */
	@FunctionalInterface
	public interface ConstructeurPage<D, R> {
		R creer(Integer page, Integer size, Long totalElements, Integer totalPages, List<D> content);
	}

	public static <E, D, R> R versReponse(Page<E> page, Function<E, D> versDto, ConstructeurPage<D, R> constructeur) {
		List<D> contenu = page.getContent().stream().map(versDto).toList();
		return constructeur.creer(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(),
				contenu);
	}
}
