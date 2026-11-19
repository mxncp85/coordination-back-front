package fr.capuches.dopale.compte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import fr.capuches.dopale.api.model.TypeTransaction;
import fr.capuches.dopale.commun.exception.ValidationMetierException;

@ExtendWith(MockitoExtension.class)
class CompteServiceTest {

	@Mock
	private TransactionRepository transactionRepository;

	@InjectMocks
	private CompteService compteService;

	@Test
	void solde_estLaSommeDesTransactions() {
		when(transactionRepository.calculerSolde()).thenReturn(12500L);

		assertThat(compteService.solde()).isEqualTo(12500L);
	}

	@Test
	void saisieManuelle_credit() {
		when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		TransactionEntity transaction = compteService.saisirManuellement(1000, true, "Don du roi");

		assertThat(transaction.getMontantBronze()).isEqualTo(1000);
		assertThat(transaction.getType()).isEqualTo(TypeTransaction.MANUEL);
		assertThat(transaction.getLibelle()).isEqualTo("Don du roi");
	}

	@Test
	void saisieManuelle_debit() {
		when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		TransactionEntity transaction = compteService.saisirManuellement(1000, false, "Loyer");

		assertThat(transaction.getMontantBronze()).isEqualTo(-1000);
	}

	@Test
	@SuppressWarnings("unchecked")
	void lister_appliqueLesFiltres() {
		Pageable pageable = PageRequest.of(0, 20);
		when(transactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

		compteService.lister(TypeTransaction.ACHAT, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 3L,
				pageable);

		verify(transactionRepository).findAll(any(Specification.class), any(Pageable.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	void lister_avecFromApresTo_estRefuse() {
		Pageable pageable = PageRequest.of(0, 20);
		LocalDate from = LocalDate.of(2026, 12, 1);
		LocalDate to = LocalDate.of(2026, 11, 1);

		assertThatThrownBy(() -> compteService.lister(null, from, to, null, pageable))
				.isInstanceOf(ValidationMetierException.class);
		verify(transactionRepository, never()).findAll(any(Specification.class), any(Pageable.class));
	}
}
