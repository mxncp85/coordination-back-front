package fr.capuches.dopale.requete;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RequeteRepository
        extends JpaRepository<RequeteEntity, Long>, JpaSpecificationExecutor<RequeteEntity> {}
