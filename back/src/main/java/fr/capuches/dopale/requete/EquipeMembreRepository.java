package fr.capuches.dopale.requete;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipeMembreRepository extends JpaRepository<EquipeMembreEntity, EquipeMembreEntity.Pk> {

	@Query("""
			select em from EquipeMembreEntity em
			join fetch em.requete r
			where em.aventurierId = :aventurierId
			order by coalesce(r.dateDepart, r.datePeremption)
			""")
	List<EquipeMembreEntity> findOccupationsByAventurierId(@Param("aventurierId") Long aventurierId);

	@Query("""
			select count(em) > 0 from EquipeMembreEntity em
			join em.requete r
			where em.aventurierId = :aventurierId
			  and r.statut in (fr.capuches.dopale.api.model.StatutRequete.EQUIPE_VALIDEE,
                               fr.capuches.dopale.api.model.StatutRequete.EN_COURS)
			""")
	boolean estDansEquipeActive(@Param("aventurierId") Long aventurierId);

	@Query("""
			select em from EquipeMembreEntity em
			join fetch em.requete r
			where em.aventurierId = :aventurierId
			  and r.statut in (fr.capuches.dopale.api.model.StatutRequete.EQUIPE_VALIDEE,
                               fr.capuches.dopale.api.model.StatutRequete.EN_COURS)
			""")
	List<EquipeMembreEntity> findEquipesActives(@Param("aventurierId") Long aventurierId);

	List<EquipeMembreEntity> findByRequeteId(Long requeteId);

}

