package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.dto.WorkerDetailDTO;
import br.com.agrogame.agrogame.model.User;

@Repository
public interface WorkerRepository extends JpaRepository<User, Integer> {

	@Query(value = """
		    SELECT
		        u.id AS id,
		        u.fullname,
		        u.email1 AS email,
		        u.phone,
		        u.thumbnail_gs_url AS profilePictureUrl,
		        COALESCE(string_agg(DISTINCT pu.name, ', ' ORDER BY pu.name), '') AS unitNames,
		        COALESCE(string_agg(DISTINCT f.name, ', ' ORDER BY f.name), '') AS farmNames,
		        u.zipcode,
		        u.number
		    FROM users u
		    JOIN worker_farm_assignments wfa
		        ON wfa.worker_id = u.id
		       AND wfa.is_active = TRUE
		    JOIN farms f
		        ON f.id = wfa.farm_id
		       AND f.is_active = TRUE
		    LEFT JOIN production_units pu
		        ON pu.farm_id = f.id
		       AND pu.is_active = TRUE
		    WHERE f.owner_id = :producerId
		      AND u.user_type_id = 9
		      AND u.user_status_id <> 5
		      AND (:farmId IS NULL OR f.id = :farmId)
		      AND (:workerId IS NULL OR u.id = :workerId)
		    GROUP BY
		        u.id,
		        u.fullname,
		        u.email1,
		        u.phone,
		        u.thumbnail_gs_url,
		        u.zipcode,
		        u.number
		    ORDER BY u.fullname
		    """, nativeQuery = true)
		List<WorkerDetailDTO> findWorkersByProducer(@Param("producerId") Integer producerId,
		        @Param("farmId") Integer farmId,
		        @Param("workerId") Integer workerId);

	@Query(value = """
	        SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
	        FROM worker_farm_assignments wfa
	        JOIN farms f
	            ON f.id = wfa.farm_id
	        JOIN users prod
	            ON prod.id = f.owner_id
	        WHERE wfa.worker_id = :workerId
	          AND prod.email1 = :producerEmail
	          AND wfa.is_active = TRUE
	          AND f.is_active = TRUE
	        """, nativeQuery = true)
	boolean existsWorkerBelongsToProducer(@Param("producerEmail") String producerEmail,
	        @Param("workerId") Integer workerId);

	// 3) Contar unidades produtivas que pertencem ao produtor
	@Query(value = """
			SELECT COUNT(*)
			FROM production_units pu
			JOIN farms f ON f.id = pu.farm_id
			WHERE pu.id IN (:unitIds)
			  AND f.owner_id = :producerId
			  AND pu.is_active = TRUE
			  AND f.is_active = TRUE
			""", nativeQuery = true)
	Long countUnitsByProducer(@Param("unitIds") List<Integer> unitIds, @Param("producerId") Integer producerId);

	// 4) Contar unidades por farm específico
	@Query(value = """
			SELECT COUNT(*)
			FROM production_units pu
			WHERE pu.id IN (:unitIds)
			  AND pu.farm_id = :farmId
			  AND pu.is_active = TRUE
			""", nativeQuery = true)
	Long countUnitsByFarm(@Param("unitIds") List<Integer> unitIds, @Param("farmId") Integer farmId);

	User findByEmail1(String email);

	boolean existsByEmail1(String email);

	@Modifying
	@Query(value = "UPDATE users SET user_status_id = :statusId WHERE id = :id", nativeQuery = true)
	void updateUserStatusById(@Param("id") Integer id, @Param("statusId") Integer statusId);

	@Query("""
			SELECT DISTINCT w
			FROM User w
			JOIN WorkerFarmAssignment wfa
			  ON wfa.workerId = w.id
			JOIN Farm f
			  ON f.id = wfa.farmId
			WHERE w.userType.code = 'worker'
			  AND wfa.isActive = true
			  AND f.isActive = true
			  AND (:companyId IS NULL OR f.company.id = :companyId)
			  AND (:ownerId IS NULL OR f.owner.id = :ownerId)
			""")
	Page<User> findWorkersByOwnerAndCompany(@Param("ownerId") Integer ownerId, @Param("companyId") Integer companyId,
			Pageable pageable);

}
