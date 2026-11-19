package fr.capuches.dopale.compte;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.capuches.dopale.api.model.TypeTransaction;
import fr.capuches.dopale.commun.exception.ValidationMetierException;
import jakarta.persistence.criteria.Predicate;

@Service
public class CompteService {

	private final TransactionRepository transactionRepository;

	public CompteService(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@Transactional
	public TransactionEntity enregistrer(TransactionEntity transaction) {
		return transactionRepository.save(transaction);
	}

	// TODO règle à confirmer avec le client : un retrait peut-il rendre le solde négatif ? (409 prévu dans le contrat)
	@Transactional
	public TransactionEntity saisirManuellement(long montantBronze, boolean credit, String libelle) {
		TransactionEntity transaction = credit
				? TransactionEntity.credit(montantBronze, libelle, TypeTransaction.MANUEL)
				: TransactionEntity.debit(montantBronze, libelle, TypeTransaction.MANUEL);
		return transactionRepository.save(transaction);
	}

	@Transactional(readOnly = true)
	public long solde() {
		return transactionRepository.calculerSolde();
	}

	@Transactional(readOnly = true)
	public Page<TransactionEntity> lister(TypeTransaction type, LocalDate from, LocalDate to, Long requeteId,
			Pageable pageable) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new ValidationMetierException("from", "from doit être antérieur ou égal à to");
		}
		return transactionRepository.findAll(filtres(type, from, to, requeteId), pageable);
	}

	private Specification<TransactionEntity> filtres(TypeTransaction type, LocalDate from, LocalDate to,
			Long requeteId) {
		ZoneId zone = ZoneId.systemDefault();
		return (root, query, cb) -> {
			List<Predicate> conditions = new ArrayList<>();
			if (type != null) {
				conditions.add(cb.equal(root.get("type"), type));
			}
			if (from != null) {
				OffsetDateTime debut = from.atStartOfDay(zone).toOffsetDateTime();
				conditions.add(cb.greaterThanOrEqualTo(root.<OffsetDateTime>get("dateTransaction"), debut));
			}
			if (to != null) {
				// to est inclusif : on prend tout ce qui est avant le lendemain à minuit
				OffsetDateTime fin = to.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
				conditions.add(cb.lessThan(root.<OffsetDateTime>get("dateTransaction"), fin));
			}
			if (requeteId != null) {
				conditions.add(cb.equal(root.get("requeteId"), requeteId));
			}
			return cb.and(conditions.toArray(Predicate[]::new));
		};
	}
}
