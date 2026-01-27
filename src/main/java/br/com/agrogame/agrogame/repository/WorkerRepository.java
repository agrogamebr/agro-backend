package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.dto.WorkerDetailDTO;
import br.com.agrogame.agrogame.model.User;

@Repository
public interface WorkerRepository extends JpaRepository<User, Integer> {

	// 1) Listar workers do produtor (com farmId opcional)
	@Query(value = """
			SELECT DISTINCT
			    u.id,
			    u.fullname,
			    u.email1 AS email,
			    u.phone,
			    u.thumbnail_gs_url AS profilePictureUrl,
			    string_agg(DISTINCT pu.name, ', ' ORDER BY pu.name) AS unitNames,
			    string_agg(DISTINCT f.name, ', ' ORDER BY f.name) AS farmNames
			FROM users u
			JOIN worker_production_unit_assignments wpu ON wpu.worker_id = u.id
			JOIN production_units pu ON pu.id = wpu.production_unit_id
			JOIN farms f ON f.id = pu.farm_id
			WHERE f.owner_id = :producerId
			  AND u.user_type_id = 9
			  AND u.user_status_id <> 5
			  AND wpu.is_active = TRUE
			  AND pu.is_active = TRUE
			  AND f.is_active = TRUE
			  AND (:farmId IS NULL OR f.id = :farmId)
			  AND (:workerId IS NULL OR u.id = :workerId)
			GROUP BY u.id, u.fullname, u.email1, u.phone, u.thumbnail_gs_url
			""", nativeQuery = true)
	List<WorkerDetailDTO> findWorkersByProducer(@Param("producerId") Integer producerId,
			@Param("farmId") Integer farmId, @Param("workerId") Integer workerId);

	// 2) Verificar se worker pertence ao produtor (via email do produtor)
	@Query(value = """
			SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
			FROM worker_production_unit_assignments wpu
			JOIN production_units pu ON pu.id = wpu.production_unit_id
			JOIN farms f ON f.id = pu.farm_id
			JOIN users prod ON prod.id = f.owner_id
			WHERE wpu.worker_id = :workerId
			  AND prod.email1 = :producerEmail
			  AND wpu.is_active = TRUE
			  AND pu.is_active = TRUE
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
}
