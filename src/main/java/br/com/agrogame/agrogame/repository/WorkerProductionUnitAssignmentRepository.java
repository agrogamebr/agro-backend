package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.WorkerProductionUnitAssignment;

@Repository
public interface WorkerProductionUnitAssignmentRepository
		extends JpaRepository<WorkerProductionUnitAssignment, Integer> {

	List<WorkerProductionUnitAssignment> findByWorkerIdAndIsActiveTrue(Integer workerId);

	@Modifying
	@Query(value = "DELETE FROM worker_production_unit_assignments WHERE worker_id = :workerId", nativeQuery = true)
	void deleteByWorkerId(@Param("workerId") Integer workerId);
	
	Optional<WorkerProductionUnitAssignment> findFirstActiveByWorkerId(Integer workerId);
	Optional<WorkerProductionUnitAssignment> findFirstByWorkerIdAndIsActiveTrue(Integer workerId);
}
