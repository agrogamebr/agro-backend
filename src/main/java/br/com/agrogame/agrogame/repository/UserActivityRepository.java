package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.UserActivity;

public interface UserActivityRepository extends JpaRepository<UserActivity, Integer> {

	@Query("SELECT ua FROM UserActivity ua " + "WHERE ua.user.id = :userId " + "AND ua.activity.id = :activityId "
			+ "AND ua.farm.id = :farmId")
	Optional<UserActivity> findByUserAndActivityAndFarm(@Param("userId") Integer userId,
			@Param("activityId") Integer activityId, @Param("farmId") Integer farmId);

	// Listar user_activities com status "submitted" de uma empresa
	@Query("""
			    SELECT ua FROM UserActivity ua
			    WHERE ua.activity.company.id = :companyId
			    AND ua.status.code = 'submitted'
			    ORDER BY ua.createdAt DESC
			""")
	List<UserActivity> findSubmittedByCompanyId(@Param("companyId") Integer companyId);

	// Alternativa com Specification (mais flexível para filtros futuros)
	List<UserActivity> findAll(Specification<UserActivity> spec);
}