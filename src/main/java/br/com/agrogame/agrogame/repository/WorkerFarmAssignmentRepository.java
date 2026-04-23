package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.WorkerFarmAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkerFarmAssignmentRepository extends JpaRepository<WorkerFarmAssignment, Long> {

	Optional<WorkerFarmAssignment> findByWorkerIdAndIsActiveTrue(Long workerId);

	List<WorkerFarmAssignment> findAllByWorkerIdOrderByIdDesc(Long workerId);

	List<WorkerFarmAssignment> findAllByFarmIdAndIsActiveTrue(Long farmId);

	boolean existsByWorkerIdAndFarmIdAndIsActiveTrue(Long workerId, Long farmId);

	boolean existsByWorkerIdAndIsActiveTrue(Long workerId);
}