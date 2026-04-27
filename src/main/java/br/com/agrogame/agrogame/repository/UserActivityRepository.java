package br.com.agrogame.agrogame.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.dto.ProducerUserActivityProjection;
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

	@Query(value = """
			SELECT
			    ua.id                  AS userActivityId,
			    uas.code               AS userActivityStatus,
			    ua.farm_id             AS userActivityFarmId,
			    ua.production_unit_id  AS productionUnitId,

			    a.id                   AS activityId,
			    a.name                 AS activityName,
			    a.description          AS description,
			    a.points               AS points,
			    s.code                 AS activityStatus,
			    a.valid_from           AS validFrom,
			    a.valid_to             AS validTo,
			    a.thumbnail_url        AS thumbnailUrl,
			    a.thumbnail_gsutil_uri AS thumbnailGsutilUri
			FROM user_activities ua
			JOIN activities a
			     ON a.id = ua.activity_id
			JOIN activity_statuses s
			     ON s.id = a.activity_status_id
			JOIN user_activity_statuses uas
			     ON uas.id = ua.status_id
			WHERE ua.user_id = :producerId
			  AND ua.farm_id IN (:farmIds)
			  AND a.company_id = :companyId
			  AND s.code = 'send'
			  AND (:status IS NULL OR uas.code = :status)
			  AND (:unidadeProdutivaId IS NULL OR ua.production_unit_id = :unidadeProdutivaId)
			  AND (:startDate IS NULL OR a.valid_from >= :startDate)
			  AND (:endDate   IS NULL OR a.valid_to   <= :endDate)
			  AND (
			        :cropTypeId IS NULL
			     OR EXISTS (
			            SELECT 1
			            FROM activity_crop_types act
			            WHERE act.activity_id = a.id
			              AND act.crop_type_id = :cropTypeId
			        )
			  )
			  ORDER BY ua.created_at DESC, ua.id DESC
			""", countQuery = """
			SELECT COUNT(*)
			FROM user_activities ua
			JOIN activities a
			     ON a.id = ua.activity_id
			JOIN activity_statuses s
			     ON s.id = a.activity_status_id
			JOIN user_activity_statuses uas
			     ON uas.id = ua.status_id
			WHERE ua.user_id = :producerId
			  AND ua.farm_id IN (:farmIds)
			  AND a.company_id = :companyId
			  AND s.code = 'send'
			  AND (:status IS NULL OR uas.code = :status)
			  AND (:unidadeProdutivaId IS NULL OR ua.production_unit_id = :unidadeProdutivaId)
			  AND (:startDate IS NULL OR a.valid_from >= :startDate)
			  AND (:endDate   IS NULL OR a.valid_to   <= :endDate)
			  AND (
			        :cropTypeId IS NULL
			     OR EXISTS (
			            SELECT 1
			            FROM activity_crop_types act
			            WHERE act.activity_id = a.id
			              AND act.crop_type_id = :cropTypeId
			        )
			  )
			""", nativeQuery = true)
	Page<ProducerUserActivityProjection> listUserActivitiesForProducer(@Param("companyId") Integer companyId,
			@Param("producerId") Integer producerId, @Param("farmIds") List<Integer> farmIds,
			@Param("status") String status, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate, @Param("cropTypeId") Integer cropTypeId,
			@Param("unidadeProdutivaId") Integer unidadeProdutivaId, Pageable pageable);

	@Query(value = """
			SELECT
			    ua.id                  AS userActivityId,
			    uas.code               AS userActivityStatus,
			    ua.farm_id             AS userActivityFarmId,
			    ua.production_unit_id  AS productionUnitId,
			    a.id                   AS activityId,
			    a.name                 AS activityName,
			    a.description          AS description,
			    a.points               AS points,
			    s.code                 AS activityStatus,
			    a.valid_from           AS validFrom,
			    a.valid_to             AS validTo,
			    a.thumbnail_url        AS thumbnailUrl,
			    a.thumbnail_gsutil_uri AS thumbnailGsutilUri
			FROM user_activities ua
			JOIN activities a
			    ON a.id = ua.activity_id
			JOIN activity_statuses s
			    ON s.id = a.activity_status_id
			JOIN user_activity_statuses uas
			    ON uas.id = ua.status_id
			WHERE ua.farm_id = :farmId
			  AND (
			        :productionUnitId IS NULL
			     OR ua.production_unit_id = :productionUnitId
			     OR ua.production_unit_id IS NULL
			  )
			  AND a.company_id = :companyId
			  AND s.code = 'send'
			  AND (:status IS NULL OR uas.code = :status)
			  AND (:startDate IS NULL OR a.valid_from >= :startDate)
			  AND (:endDate IS NULL OR a.valid_to <= :endDate)
			  AND (
			        :cropTypeId IS NULL
			     OR NOT EXISTS (
			            SELECT 1
			            FROM activity_crop_types actAll
			            WHERE actAll.activity_id = a.id
			        )
			     OR EXISTS (
			            SELECT 1
			            FROM activity_crop_types act
			            WHERE act.activity_id = a.id
			              AND act.crop_type_id = :cropTypeId
			        )
			  )
			ORDER BY ua.created_at DESC, ua.id DESC
			""", countQuery = """
			SELECT COUNT(*)
			FROM user_activities ua
			JOIN activities a
			    ON a.id = ua.activity_id
			JOIN activity_statuses s
			    ON s.id = a.activity_status_id
			JOIN user_activity_statuses uas
			    ON uas.id = ua.status_id
			WHERE ua.farm_id = :farmId
			  AND (
			        :productionUnitId IS NULL
			     OR ua.production_unit_id = :productionUnitId
			     OR ua.production_unit_id IS NULL
			  )
			  AND a.company_id = :companyId
			  AND s.code = 'send'
			  AND (:status IS NULL OR uas.code = :status)
			  AND (:startDate IS NULL OR a.valid_from >= :startDate)
			  AND (:endDate IS NULL OR a.valid_to <= :endDate)
			  AND (
			        :cropTypeId IS NULL
			     OR NOT EXISTS (
			            SELECT 1
			            FROM activity_crop_types actAll
			            WHERE actAll.activity_id = a.id
			        )
			     OR EXISTS (
			            SELECT 1
			            FROM activity_crop_types act
			            WHERE act.activity_id = a.id
			              AND act.crop_type_id = :cropTypeId
			        )
			  )
			""", nativeQuery = true)
	Page<ProducerUserActivityProjection> listUserActivitiesForWorker(@Param("companyId") Integer companyId,
			@Param("farmId") Integer farmId, @Param("status") String status, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate, @Param("cropTypeId") Integer cropTypeId,
			@Param("productionUnitId") Integer productionUnitId, Pageable pageable);

}