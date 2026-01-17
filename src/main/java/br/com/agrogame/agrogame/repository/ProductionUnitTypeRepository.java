package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.ProductionUnitType;

@Repository
public interface ProductionUnitTypeRepository extends JpaRepository<ProductionUnitType, Integer> {

	// Listar apenas tipos ativos
	List<ProductionUnitType> findByIsActiveTrueOrderByName();

	// Buscar por código
	Optional<ProductionUnitType> findByCodeAndIsActiveTrue(String code);

	@Query(value = """
			SELECT DISTINCT t.*
			FROM production_unit_types t
			INNER JOIN crop_type_unit_compatibility c ON c.production_unit_type_id = t.id
			INNER JOIN farm_crops fc ON fc.crop_type_id = c.crop_type_id
			WHERE fc.farm_id = :farmId
			AND t.is_active = true
			ORDER BY t.name
			""", nativeQuery = true)
	List<ProductionUnitType> findCompatibleTypesByFarmId(@Param("farmId") Integer farmId);
}
