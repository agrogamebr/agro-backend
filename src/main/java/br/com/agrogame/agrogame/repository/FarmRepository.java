package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.Farm;

public interface FarmRepository extends JpaRepository<Farm, Integer> {

	List<Farm> findByOwnerId(Integer ownerId);

	List<Farm> findByOwnerIdAndCompanyId(Integer ownerId, Integer companyId);

	List<Farm> findByOwnerIdAndIsActiveTrue(Integer ownerId);

	Optional<Farm> findByIdAndOwnerIdAndIsActiveTrue(Integer id, Integer ownerId);

	Optional<Farm> findByIdAndOwnerIdAndIsActiveFalse(Integer id, Integer ownerId);

	List<Farm> findByCompanyId(Integer companyId);

	List<Farm> findByCompanyIdAndIsActiveTrue(Integer companyId);

	@Query("""
			    SELECT DISTINCT f
			    FROM Farm f
			    JOIN ProductionUnit pu ON pu.farm.id = f.id
			    JOIN WorkerProductionUnitAssignment wpu ON wpu.productionUnitId = pu.id
			    WHERE wpu.workerId = :workerId
			      AND wpu.isActive = true
			      AND pu.isActive = true
			      AND f.isActive = true
			""")
	List<Farm> findByWorkerAssignments(@Param("workerId") Integer workerId);

	@Query("""
			    SELECT DISTINCT f
			    FROM Farm f
			    JOIN ProductionUnit pu ON pu.farm.id = f.id
			    JOIN WorkerProductionUnitAssignment wpu ON wpu.productionUnitId = pu.id
			    WHERE wpu.workerId = :workerId
			      AND f.id = :farmId
			      AND wpu.isActive = true
			      AND pu.isActive = true
			      AND f.isActive = true
			""")
	Optional<Farm> findByIdAndWorkerAssignments(@Param("farmId") Integer farmId, @Param("workerId") Integer workerId);

	@Query("""
			SELECT DISTINCT f
			FROM Farm f
			JOIN FarmCrop fc ON fc.farm = f
			WHERE f.company.id = :companyId
			  AND f.isActive = true
			  AND fc.cropType.id IN :cropTypeIds
			ORDER BY f.name
			""")
	List<Farm> findByCompanyIdAndCropTypes(@Param("companyId") Integer companyId,
			@Param("cropTypeIds") List<Integer> cropTypeIds);

	List<Farm> findByIdInAndCompanyIdAndIsActiveTrue(List<Integer> ids, Integer companyId);

	@Query(value = """
			    SELECT f
			    FROM Farm f
			    LEFT JOIN FETCH f.owner
			    WHERE f.company.id = :companyId
			    AND f.isActive = true
			""", countQuery = """
			    SELECT count(f)
			    FROM Farm f
			    WHERE f.company.id = :companyId
			    AND f.isActive = true
			""")
	Page<Farm> buscarPorCompanyIdAndIsActiveTrue(@Param("companyId") Integer companyId, Pageable pageable);

	@Query(value = """
			    SELECT f
			    FROM Farm f
			    LEFT JOIN FETCH f.owner
			    WHERE f.company.id = :companyId
			    AND f.isActive = true
			    AND (:farmId IS NULL OR f.id = :farmId)
			    AND (:nameLike IS NULL OR lower(f.name) LIKE lower(cast(:nameLike as text)))
			    AND (:ownerId IS NULL OR f.owner.id = :ownerId)
			""", countQuery = """
			    SELECT count(f)
			    FROM Farm f
			    WHERE f.company.id = :companyId
			    AND f.isActive = true
			    AND (:farmId IS NULL OR f.id = :farmId)
			    AND (:nameLike IS NULL OR lower(f.name) LIKE lower(cast(:nameLike as text)))
			    AND (:ownerId IS NULL OR f.owner.id = :ownerId)
			""")
	Page<Farm> findByFilters(@Param("companyId") Integer companyId, @Param("farmId") Integer farmId,
			@Param("nameLike") String nameLike, @Param("ownerId") Integer ownerId, Pageable pageable);
}
