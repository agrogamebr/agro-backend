package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.dto.TransactionWithUserActivity;
import br.com.agrogame.agrogame.model.UserPointsTransaction;

@Repository
public interface UserPointsTransactionRepository extends JpaRepository<UserPointsTransaction, Integer> {

	@Query("SELECT COALESCE(MAX(t.balanceAfter), 0) " + "FROM UserPointsTransaction t " + "WHERE t.user.id = :userId")
	Integer findLatestBalance(@Param("userId") Integer userId);

	List<UserPointsTransaction> findByUserIdOrderByCreatedAtDesc(Integer userId);

	@Query("SELECT t FROM UserPointsTransaction t " + "WHERE t.user.id = :userId " + "ORDER BY t.createdAt DESC")
	List<UserPointsTransaction> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);

	// Novos: com filtro de farm via user_activities
	@Query(value = """
			    SELECT COALESCE(MAX(upt.balance_after), 0)
			    FROM user_points_transactions upt
			    JOIN activities a ON a.id = upt.activity_id
			    JOIN user_activities ua ON ua.activity_id = a.id
			    WHERE upt.user_id = :userId
			      AND ua.farm_id = :farmId
			""", nativeQuery = true)
	Integer findLatestBalanceByUserAndFarm(@Param("userId") Integer userId, @Param("farmId") Integer farmId);

	@Query(value = """
			    SELECT upt.*
			    FROM user_points_transactions upt
			    JOIN activities a ON a.id = upt.activity_id
			    JOIN user_activities ua ON ua.activity_id = a.id
			    WHERE upt.user_id = :userId
			      AND ua.farm_id = :farmId
			    ORDER BY upt.created_at DESC
			""", nativeQuery = true)
	List<UserPointsTransaction> findByUserIdAndFarmIdOrderByCreatedAtDesc(@Param("userId") Integer userId,
			@Param("farmId") Integer farmId);

	// 1. Caso SEM filtro (Traz todas, fazenda vem null se não tiver atividade)
	@Query("""
			    SELECT t AS transaction,
			           ua.id AS userActivityId,
			           ua.farm.name AS farmName,
			           ua.farm.id AS farmId
			    FROM UserPointsTransaction t
			    LEFT JOIN UserActivity ua ON ua.activity.id = t.activity.id AND ua.user.id = t.user.id
			    WHERE t.user.id = :userId
			      AND (ua.id IS NULL OR ua.status.id = 3)
			    ORDER BY t.createdAt DESC
			""")
	List<TransactionWithUserActivity> findByUserIdWithUserActivity(@Param("userId") Integer userId);

	// Cenário 2: Busca Filtrada por Fazenda (JOIN normal)
	@Query("""
			    SELECT t AS transaction,
			           ua.id AS userActivityId,
			           ua.farm.name AS farmName,
			           ua.farm.id AS farmId
			    FROM UserPointsTransaction t
			    JOIN UserActivity ua ON ua.activity.id = t.activity.id AND ua.user.id = t.user.id
			    WHERE t.user.id = :userId
			      AND ua.farm.id = :farmId
			      AND ua.status.id = 3
			    ORDER BY t.createdAt DESC
			""")
	List<TransactionWithUserActivity> findByUserIdAndFarmIdWithUserActivity(@Param("userId") Integer userId,
			@Param("farmId") Integer farmId);
}
