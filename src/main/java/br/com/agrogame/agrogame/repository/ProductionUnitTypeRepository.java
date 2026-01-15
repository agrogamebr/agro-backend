package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.ProductionUnitType;

@Repository
public interface ProductionUnitTypeRepository extends JpaRepository<ProductionUnitType, Integer> {

	// Listar apenas tipos ativos
	List<ProductionUnitType> findByIsActiveTrueOrderByName();

	// Buscar por código
	Optional<ProductionUnitType> findByCodeAndIsActiveTrue(String code);
}
