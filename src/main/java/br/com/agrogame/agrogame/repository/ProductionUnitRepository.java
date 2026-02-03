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

	@Query("SELECT pu FROM ProductionUnit pu " + "JOIN pu.farm f " + "WHERE f.owner.id = :ownerId "
			+ "AND (:farmId IS NULL OR f.id = :farmId) " + "AND (:name IS NULL OR LOWER(pu.name) LIKE :name) "
			+ "AND (:isActive IS NULL OR pu.isActive = :isActive) " + "ORDER BY pu.name")
	List<ProductionUnit> findByFilters(@Param("ownerId") Integer ownerId, @Param("farmId") Integer farmId,
			@Param("name") String name, // Aqui virá o valor já com % e em minúsculo
			@Param("isActive") Boolean isActive);

	@Query(value = """
			SELECT pu.*
			FROM production_units pu
			JOIN farms f ON f.id = pu.farm_id
			JOIN crop_type_unit_compatibility c ON c.production_unit_type_id = pu.production_unit_type_id
			WHERE f.id IN (:farmIds)
			  AND pu.is_active = true
			  AND c.crop_type_id IN (:cropTypeIds)
			ORDER BY f.name, pu.name
			""", nativeQuery = true)
	List<ProductionUnit> findByFarmsAndCropTypes(@Param("farmIds") List<Integer> farmIds,
			@Param("cropTypeIds") List<Integer> cropTypeIds);

}