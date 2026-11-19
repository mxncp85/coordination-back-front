package fr.capuches.dopale.compte;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository
		extends JpaRepository<TransactionEntity, Long>, JpaSpecificationExecutor<TransactionEntity> {

	@Query("select coalesce(sum(t.montantBronze), 0) from TransactionEntity t")
	long calculerSolde();
}
