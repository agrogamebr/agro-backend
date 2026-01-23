package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

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

}
