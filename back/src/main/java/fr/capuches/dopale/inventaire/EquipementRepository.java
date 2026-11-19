package fr.capuches.dopale.inventaire;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.capuches.dopale.api.model.StatutEquipement;

public interface EquipementRepository extends JpaRepository<EquipementEntity, Long> {

	Page<EquipementEntity> findByStatut(StatutEquipement statut, Pageable pageable);
}
