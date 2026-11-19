package fr.capuches.dopale.aventurier;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AventurierRepository extends JpaRepository<AventurierEntity, Long>, JpaSpecificationExecutor<AventurierEntity> {

	Optional<AventurierEntity> findByIdAndSupprimeLeIsNull(Long id);

	List<AventurierEntity> findBySupprimeLeIsNullOrderByNomAsc();
}
