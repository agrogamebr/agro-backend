package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.UserActivity;

public interface UserActivityRepository extends JpaRepository<UserActivity, Integer> {

	@Query("SELECT ua FROM UserActivity ua " + "WHERE ua.user.id = :userId " + "AND ua.activity.id = :activityId "
			+ "AND ua.farm.id = :farmId")
	Optional<UserActivity> findByUserAndActivityAndFarm(@Param("userId") Integer userId,
			@Param("activityId") Integer activityId, @Param("farmId") Integer farmId);

	@Query("""
		    SELECT ua
		      FROM UserActivity ua
		      JOIN FETCH ua.activity a
		      JOIN FETCH ua.user u
		      JOIN FETCH ua.farm f
		      LEFT JOIN FETCH ua.productionUnit
		     WHERE a.company.id = :companyId 
		       AND ua.status.code = 'submitted'
		     ORDER BY ua.createdAt DESC
		""")
		Page<UserActivity> findSubmittedByCompanyId(@Param("companyId") Integer companyId, Pageable pageable);


	// Alternativa com Specification (mais flexível para filtros futuros)
	List<UserActivity> findAll(Specification<UserActivity> spec);

	@Modifying
	@Query("UPDATE UserActivity ua SET ua.status.id = 5 " + // 5 = Cancelada
			"WHERE ua.farm.id = :farmId " + "AND ua.status.id IN (1, 2)") // Só cancela Pendente(1) ou Enviada(2).
	void cancelActivitiesByFarm(@Param("farmId") Integer farmId);

	@Modifying
	@Query("UPDATE UserActivity ua SET ua.status.id = 5 WHERE ua.productionUnit.id = :unitId AND ua.status.id NOT IN (3, 4, 5)")
	void cancelActivitiesByProductionUnit(@Param("unitId") Integer unitId);
	
	List<UserActivity> findByActivityId(Integer activityId);

}