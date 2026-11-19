package fr.capuches.dopale.inventaire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import fr.capuches.dopale.api.model.StatutEquipement;
import fr.capuches.dopale.commun.exception.ConflitMetierException;
import fr.capuches.dopale.commun.exception.RessourceIntrouvableException;

@ExtendWith(MockitoExtension.class)
class EquipementServiceTest {

	@Mock
	private EquipementRepository equipementRepository;

	@InjectMocks
	private EquipementService equipementService;

	@Test
	void acheter_enregistreUnEquipementNeuf() {
		when(equipementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		EquipementEntity equipement = equipementService.acheter("Épée longue", "arme", 30, 5000);

		assertThat(equipement.getNom()).isEqualTo("Épée longue");
		assertThat(equipement.getPrixAchatBronze()).isEqualTo(5000);
		assertThat(equipement.getDurabiliteActuelle()).isEqualTo(30);
		assertThat(equipement.getStatut()).isEqualTo(StatutEquipement.DISPONIBLE);
		verify(equipementRepository).save(equipement);
	}

	@Test
	void trouver_unIdInconnu_leveIntrouvable() {
		when(equipementRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> equipementService.trouver(99L))
				.isInstanceOf(RessourceIntrouvableException.class);
	}

	@Test
	void lister_sansStatut_renvoieTout() {
		Pageable pageable = PageRequest.of(0, 20);
		when(equipementRepository.findAll(pageable)).thenReturn(Page.empty());

		equipementService.lister(null, pageable);

		verify(equipementRepository).findAll(pageable);
		verify(equipementRepository, never()).findByStatut(any(), any());
	}

	@Test
	void lister_avecStatut_filtre() {
		Pageable pageable = PageRequest.of(0, 20);
		when(equipementRepository.findByStatut(StatutEquipement.DISPONIBLE, pageable)).thenReturn(Page.empty());

		equipementService.lister(StatutEquipement.DISPONIBLE, pageable);

		verify(equipementRepository).findByStatut(StatutEquipement.DISPONIBLE, pageable);
	}

	@Test
	void emprunter_unEquipementDisponible() {
		EquipementEntity epee = new EquipementEntity("Épée longue", "arme", 30, 5000);
		when(equipementRepository.findById(1L)).thenReturn(Optional.of(epee));

		EquipementEntity resultat = equipementService.emprunter(1L);

		assertThat(resultat.getStatut()).isEqualTo(StatutEquipement.EMPRUNTE);
	}

	@Test
	void emprunter_deuxFoisLeMemeEquipement_estRefuse() {
		EquipementEntity epee = new EquipementEntity("Épée longue", "arme", 30, 5000);
		when(equipementRepository.findById(1L)).thenReturn(Optional.of(epee));
		equipementService.emprunter(1L);

		assertThatThrownBy(() -> equipementService.emprunter(1L)).isInstanceOf(ConflitMetierException.class);
	}
}
