package br.com.agrogame.agrogame.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.dto.ActivityWithUserActivityProjection;
import br.com.agrogame.agrogame.dto.BackofficeActivityProjection;
import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.ActivityReward;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {
	Page<Activity> findByCompanyId(Integer companyId, Pageable pageable);

	List<Activity> findByCompanyIdAndActivityStatus_Code(Integer companyId, String statusCode);

	// Query para buscar atividades que têm intersecção de crop_types com a farm
	@Query("SELECT DISTINCT a FROM Activity a " + "JOIN ActivityCropType act ON act.activity.id = a.id "
			+ "JOIN FarmCrop fc ON fc.cropType.id = act.cropType.id " + "WHERE fc.farm.id = :farmId "
			+ "AND a.company.id = :companyId " + "AND a.activityStatus.id = 3")
	List<Activity> findActivitiesForFarm(@Param("farmId") Integer farmId, @Param("companyId") Integer companyId);

	@Query("SELECT ar FROM ActivityReward ar WHERE ar.activity.id = :activityId")
	List<ActivityReward> findByActivityId(@Param("activityId") Integer activityId);

	@Query("""
			SELECT DISTINCT a
			FROM Activity a
			LEFT JOIN a.activityCropTypes actCrop
			LEFT JOIN FarmCrop fc ON fc.cropType.id = actCrop.cropType.id
			LEFT JOIN ActivityDraft ad ON ad.activity.id = a.id
			LEFT JOIN UserActivity ua ON ua.activity.id = a.id
			WHERE a.company.id = :companyId
			  AND (:statusCode IS NULL OR LOWER(a.activityStatus.code) = :statusCode)
			  AND (COALESCE(:cropTypeIds, NULL) IS NULL OR actCrop.cropType.id IN (:cropTypeIds))
			  AND (COALESCE(:farmIds, NULL) IS NULL OR (
			       fc.farm.id IN (:farmIds) OR
			       ad.farm.id IN (:farmIds) OR
			       ua.farm.id IN (:farmIds)
			  ))
			  AND (COALESCE(:productionUnitIds, NULL) IS NULL OR (
			       ad.productionUnit.id IN (:productionUnitIds) OR
			       ua.productionUnit.id IN (:productionUnitIds)
			  ))
			  AND (cast(:startDate as date) IS NULL OR a.validTo   >= :startDate)
			  AND (cast(:endDate   as date) IS NULL OR a.validFrom <= :endDate)
			""")
	Page<Activity> findWithFilters(@Param("companyId") Integer companyId, @Param("statusCode") String statusCode,
			@Param("cropTypeIds") List<Integer> cropTypeIds, @Param("farmIds") List<Integer> farmIds,
			@Param("productionUnitIds") List<Integer> productionUnitIds, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate, Pageable pageable);

	@Query(value = """
			    SELECT
			        a.id as id,
			        a.name as name,
			        a.company_id as companyId,
			        a.description as description,
			        a.points as points,
			        s.code as status,
			        a.valid_from as validFrom,
			        a.valid_to as validTo,
			        ua.id as userActivityId,
			        uas.code as userActivityStatus,
			        ua.farm_id as userActivityFarmId
			    FROM activities a
			    JOIN activity_statuses s ON s.id = a.activity_status_id
			    LEFT JOIN user_activities ua ON ua.activity_id = a.id
			        AND ua.user_id = :producerId
			        AND ua.farm_id = :farmId
			    LEFT JOIN user_activity_statuses uas ON uas.id = ua.status_id
			    WHERE a.company_id = :companyId
			      AND (:status IS NULL OR s.code = :status)
			      AND (:startDate IS NULL OR a.valid_from >= :startDate)
			      AND (:endDate IS NULL OR a.valid_to <= :endDate)
			""", nativeQuery = true)
	List<ActivityWithUserActivityProjection> listActivitiesWithUserActivity(@Param("companyId") Integer companyId,
			@Param("producerId") Integer producerId, @Param("farmId") Integer farmId, @Param("status") String status,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query(value = """
			    SELECT
			        a.id                  AS id,
			        a.name                AS name,
			        a.company_id          AS companyId,
			        a.description         AS description,
			        a.points              AS points,
			        s.code                AS status,
			        a.valid_from          AS validFrom,
			        a.valid_to            AS validTo,
			        ua.id                 AS userActivityId,
			        uas.code              AS userActivityStatus,
			        ua.farm_id            AS userActivityFarmId,
			        a.thumbnail_url        AS thumbnailUrl,
			        a.thumbnail_gsutil_uri  AS thumbnailGsutilUri
			    FROM activities a
			    JOIN activity_statuses s
			          ON s.id = a.activity_status_id
			    LEFT JOIN user_activities ua
			           ON ua.activity_id = a.id
			          AND ua.user_id     = :producerId
			          AND ua.farm_id     IN (:farmIds)
			    LEFT JOIN user_activity_statuses uas
			           ON uas.id = ua.status_id
			    WHERE a.company_id = :companyId
			      AND a.activity_status_id = 3
			      AND (:status IS NULL OR uas.code = :status)
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
	List<ActivityWithUserActivityProjection> listActivitiesWithUserActivityForFarms(
			@Param("companyId") Integer companyId, @Param("producerId") Integer producerId,
			@Param("farmIds") List<Integer> farmIds, @Param("status") String status,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
			@Param("cropTypeId") Integer cropTypeId);

	@Query(value = """
			SELECT
			    a.id                   AS id,
			    a.name                 AS name,
			    a.company_id           AS companyId,
			    a.description          AS description,
			    a.points               AS points,
			    s.code                 AS status,
			    a.valid_from           AS validFrom,
			    a.valid_to             AS validTo,
			    MAX(ua.id)             AS userActivityId,
			    MAX(uas.code)          AS userActivityStatus,
			    MAX(ua.user_id)        AS userId,
			    MAX(ua.farm_id)        AS farmId,
			    MAX(ua.production_unit_id) AS productionUnitId,
			    a.thumbnail_url        AS thumbnailUrl,
			    a.thumbnail_gsutil_uri AS thumbnailGsutilUri
			FROM activities a
			JOIN activity_statuses s
			      ON s.id = a.activity_status_id
			LEFT JOIN user_activities ua
			       ON ua.activity_id = a.id
			LEFT JOIN user_activity_statuses uas
			       ON uas.id = ua.status_id
			WHERE a.company_id = :companyId
			  AND (:activityStatus IS NULL OR s.code = :activityStatus)
			  AND (:userActivityStatus IS NULL OR uas.code = :userActivityStatus)
			  AND (:producerId IS NULL OR ua.user_id = :producerId)
			  AND (:farmId IS NULL OR ua.farm_id = :farmId)
			  AND (:productionUnitId IS NULL OR ua.production_unit_id = :productionUnitId)
			  AND (:startDate IS NULL OR a.valid_from >= :startDate)
			  AND (:endDate   IS NULL OR a.valid_to   <= :endDate)
			  AND (:cropTypeId IS NULL OR EXISTS (
			        SELECT 1 FROM activity_crop_types act
			        WHERE act.activity_id = a.id AND act.crop_type_id = :cropTypeId
			  ))
			GROUP BY
			    a.id, a.name, a.company_id, a.description, a.points,
			    s.code, a.valid_from, a.valid_to,
			    a.thumbnail_url, a.thumbnail_gsutil_uri
			""", countQuery = """
			SELECT COUNT(*)
			FROM (
			    SELECT a.id
			    FROM activities a
			    JOIN activity_statuses s
			          ON s.id = a.activity_status_id
			    LEFT JOIN user_activities ua
			           ON ua.activity_id = a.id
			    LEFT JOIN user_activity_statuses uas
			           ON uas.id = ua.status_id
			    WHERE a.company_id = :companyId
			      AND (:activityStatus IS NULL OR s.code = :activityStatus)
			      AND (:userActivityStatus IS NULL OR uas.code = :userActivityStatus)
			      AND (:producerId IS NULL OR ua.user_id = :producerId)
			      AND (:farmId IS NULL OR ua.farm_id = :farmId)
			      AND (:productionUnitId IS NULL OR ua.production_unit_id = :productionUnitId)
			      AND (:startDate IS NULL OR a.valid_from >= :startDate)
			      AND (:endDate   IS NULL OR a.valid_to   <= :endDate)
			      AND (:cropTypeId IS NULL OR EXISTS (
			            SELECT 1 FROM activity_crop_types act
			            WHERE act.activity_id = a.id AND act.crop_type_id = :cropTypeId
			      ))
			    GROUP BY a.id
			) x
			""", nativeQuery = true)
	Page<BackofficeActivityProjection> findForBackoffice(@Param("companyId") Integer companyId,
			@Param("activityStatus") String activityStatus, @Param("userActivityStatus") String userActivityStatus,
			@Param("producerId") Integer producerId, @Param("farmId") Integer farmId,
			@Param("productionUnitId") Integer productionUnitId, @Param("cropTypeId") Integer cropTypeId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

}
