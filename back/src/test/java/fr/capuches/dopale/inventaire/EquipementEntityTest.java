package fr.capuches.dopale.inventaire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import fr.capuches.dopale.api.model.StatutEquipement;
import fr.capuches.dopale.commun.exception.ConflitMetierException;

class EquipementEntityTest {

	private EquipementEntity epee() {
		return new EquipementEntity("Épée longue", "arme", 30, 5000);
	}

	@Test
	void nouvelEquipement_estDisponibleAvecDurabiliteMax() {
		EquipementEntity equipement = epee();

		assertThat(equipement.getStatut()).isEqualTo(StatutEquipement.DISPONIBLE);
		assertThat(equipement.getDurabiliteActuelle()).isEqualTo(30);
		assertThat(equipement.getPrixAchatBronze()).isEqualTo(5000);
	}

	@Test
	void emprunter_unEquipementDisponible_lePasseEnEmprunte() {
		EquipementEntity equipement = epee();

		equipement.emprunter();

		assertThat(equipement.getStatut()).isEqualTo(StatutEquipement.EMPRUNTE);
	}

	@Test
	void emprunter_unEquipementDejaEmprunte_estRefuse() {
		EquipementEntity equipement = epee();
		equipement.emprunter();

		assertThatThrownBy(equipement::emprunter).isInstanceOf(ConflitMetierException.class);
		assertThat(equipement.getStatut()).isEqualTo(StatutEquipement.EMPRUNTE);
	}

	@Test
	void emprunter_unEquipementEnReparation_estRefuse() {
		EquipementEntity equipement = epee();
		ReflectionTestUtils.setField(equipement, "statut", StatutEquipement.EN_REPARATION);

		assertThatThrownBy(equipement::emprunter).isInstanceOf(ConflitMetierException.class);
		assertThat(equipement.getStatut()).isEqualTo(StatutEquipement.EN_REPARATION);
	}

	@Test
	void reparer_remetLaDurabiliteAuMaxEtLeRendDisponible() {
		EquipementEntity equipement = epee();
		ReflectionTestUtils.setField(equipement, "statut", StatutEquipement.EN_REPARATION);
		ReflectionTestUtils.setField(equipement, "durabiliteActuelle", 0);

		equipement.reparer();

		assertThat(equipement.getDurabiliteActuelle()).isEqualTo(30);
		assertThat(equipement.getStatut()).isEqualTo(StatutEquipement.DISPONIBLE);
	}

	@Test
	void reparer_unEquipementEnMission_estRefuse() {
		EquipementEntity equipement = epee();
		equipement.emprunter();

		assertThatThrownBy(equipement::reparer).isInstanceOf(ConflitMetierException.class);
	}
}
