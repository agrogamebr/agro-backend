package br.com.agrogame.agrogame.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.dto.BackofficePointsStatementProjection;
import br.com.agrogame.agrogame.dto.ProducerPointsTransactionDTO;
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

	@Query(value = """
			SELECT
			    upt.id AS id,
			    upt.created_at AS date,
			    CASE
			        WHEN act.name IS NOT NULL THEN act.name
			        WHEN rew.name IS NOT NULL THEN rew.name
			        ELSE 'Ajuste Manual'
			    END AS description,
			    f.name AS farmName,
			    pu.name AS productionUnitName,
			    tt.code AS operationType,
			    upt.points AS points,
			    upt.balance_after AS balanceAfter
			FROM user_points_transactions upt
			JOIN user_point_transaction_types tt ON tt.id = upt.transaction_type_id

			LEFT JOIN activities act ON act.id = upt.activity_id
			LEFT JOIN rewards rew ON rew.id = upt.reward_id
			LEFT JOIN user_activities ua ON ua.activity_id = act.id AND ua.user_id = upt.user_id
			LEFT JOIN farms f ON f.id = ua.farm_id
			LEFT JOIN production_units pu ON pu.id = ua.production_unit_id

			WHERE 1=1
			  AND (cast(:producerId as integer) IS NULL OR upt.user_id = :producerId)
			  AND (cast(:startDate as timestamp) IS NULL OR upt.created_at >= :startDate)
			  AND (cast(:endDate as timestamp)   IS NULL OR upt.created_at <= :endDate)
			  AND (cast(:operationType as text) IS NULL OR tt.code = :operationType)
			  AND (cast(:farmId as integer) IS NULL OR f.id = :farmId)
			  AND (cast(:productionUnitId as integer) IS NULL OR pu.id = :productionUnitId)
			ORDER BY upt.created_at DESC, upt.id DESC
			""", countQuery = """
			SELECT COUNT(upt.id)
			FROM user_points_transactions upt
			JOIN user_point_transaction_types tt ON tt.id = upt.transaction_type_id
			LEFT JOIN activities act ON act.id = upt.activity_id
			LEFT JOIN user_activities ua ON ua.activity_id = act.id AND ua.user_id = upt.user_id
			LEFT JOIN farms f ON f.id = ua.farm_id
			LEFT JOIN production_units pu ON pu.id = ua.production_unit_id
			WHERE 1=1
			  AND (cast(:producerId as integer) IS NULL OR upt.user_id = :producerId)
			  AND (cast(:startDate as timestamp) IS NULL OR upt.created_at >= :startDate)
			  AND (cast(:endDate as timestamp)   IS NULL OR upt.created_at <= :endDate)
			  AND (cast(:operationType as text) IS NULL OR tt.code = :operationType)
			  AND (cast(:farmId as integer) IS NULL OR f.id = :farmId)
			  AND (cast(:productionUnitId as integer) IS NULL OR pu.id = :productionUnitId)
			""", nativeQuery = true)
	Page<BackofficePointsStatementProjection> findStatementForBackoffice(@Param("producerId") Integer producerId,
			@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate,
			@Param("operationType") String operationType, @Param("farmId") Integer farmId,
			@Param("productionUnitId") Integer productionUnitId, Pageable pageable);

	@Query(value = """
			    SELECT new br.com.agrogame.agrogame.dto.ProducerPointsTransactionDTO(
			        t.id,
			        tt.code,
			        st.code,
			        act.name,
			        rew.name,
			        t.points,
			        t.balanceAfter,
			        t.createdAt,
			        act.id,
			        ua.id,
			        f.name,
			        f.id,
			        pu.name,
			        pu.id
			    )
			    FROM UserPointsTransaction t
			    LEFT JOIN t.transactionType tt
			    LEFT JOIN t.sourceType st
			    LEFT JOIN t.activity act
			    LEFT JOIN t.reward rew

			    LEFT JOIN UserActivity ua
			       ON ua.id = (
			           SELECT MAX(ua2.id)
			           FROM UserActivity ua2
			           WHERE ua2.activity.id = t.activity.id
			             AND ua2.user.id = t.user.id
			       )
			    LEFT JOIN ua.farm f
			    LEFT JOIN ua.productionUnit pu

			    WHERE t.user.id = :userId
			      AND (
			          :farmId IS NULL
			          OR (f.id = :farmId AND ua.status.id = 3)
			      )
			    ORDER BY t.createdAt DESC, t.id DESC
			""", countQuery = """
			    SELECT count(t)
			    FROM UserPointsTransaction t
			    LEFT JOIN UserActivity ua
			       ON ua.id = (
			           SELECT MAX(ua2.id)
			           FROM UserActivity ua2
			           WHERE ua2.activity.id = t.activity.id
			             AND ua2.user.id = t.user.id
			       )
			    LEFT JOIN ua.farm f
			    WHERE t.user.id = :userId
			      AND (
			          :farmId IS NULL
			          OR (f.id = :farmId AND ua.status.id = 3)
			      )
			""")
	Page<ProducerPointsTransactionDTO> findByUserAndFarmFilters(@Param("userId") Integer userId,
			@Param("farmId") Integer farmId, Pageable pageable);

}
