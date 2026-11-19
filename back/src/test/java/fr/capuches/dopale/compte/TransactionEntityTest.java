package fr.capuches.dopale.compte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import fr.capuches.dopale.api.model.TypeTransaction;

class TransactionEntityTest {

	@Test
	void credit_stockeUnMontantPositif() {
		TransactionEntity transaction = TransactionEntity.credit(2000, "Don", TypeTransaction.MANUEL);

		assertThat(transaction.getMontantBronze()).isEqualTo(2000);
		assertThat(transaction.getDateTransaction()).isNotNull();
	}

	@Test
	void debit_stockeUnMontantNegatif() {
		TransactionEntity transaction = TransactionEntity.debit(5000, "Achat : Épée", TypeTransaction.ACHAT);

		assertThat(transaction.getMontantBronze()).isEqualTo(-5000);
	}

	@Test
	void unMontantNulOuNegatif_estRefuse() {
		assertThatThrownBy(() -> TransactionEntity.credit(0, "Rien", TypeTransaction.MANUEL))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> TransactionEntity.debit(-10, "Négatif", TypeTransaction.MANUEL))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void lesLiensSontRenseignes() {
		TransactionEntity transaction = TransactionEntity.debit(300, "Paiement", TypeTransaction.PAIEMENT_AVENTURIER)
				.pourRequete(4L)
				.pourAventurier(9L);

		assertThat(transaction.getRequeteId()).isEqualTo(4L);
		assertThat(transaction.getAventurierId()).isEqualTo(9L);
		assertThat(transaction.getEquipementId()).isNull();
	}
}
