package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.ProductionUnit;

@Repository
public interface ProductionUnitRepository extends JpaRepository<ProductionUnit, Integer> {

	// Buscar todas units ativas de uma farm
	List<ProductionUnit> findByFarmIdAndIsActiveTrue(Integer farmId);

	// Buscar todas units de uma farm (ativas ou não)
	List<ProductionUnit> findByFarmId(Integer farmId);

	// Buscar uma unit específica ativa
	Optional<ProductionUnit> findByIdAndIsActiveTrue(Integer id);

	// Buscar units de um owner (producer) específico
	@Query("SELECT pu FROM ProductionUnit pu " + "JOIN pu.farm f " + "WHERE f.owner.id = :ownerId "
			+ "AND pu.isActive = true " + "ORDER BY f.name, pu.name")
	List<ProductionUnit> findByOwnerId(@Param("ownerId") Integer ownerId);

	// Buscar units de uma farm específica de um owner
	@Query("SELECT pu FROM ProductionUnit pu " + "JOIN pu.farm f " + "WHERE f.id = :farmId "
			+ "AND f.owner.id = :ownerId " + "AND pu.isActive = true")
	List<ProductionUnit> findByFarmIdAndOwnerId(@Param("farmId") Integer farmId, @Param("ownerId") Integer ownerId);
}