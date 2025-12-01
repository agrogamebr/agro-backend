package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.FarmCrop;

public interface FarmCropRepository extends JpaRepository<FarmCrop, Integer> {
	List<FarmCrop> findByFarmId(Integer farmId);
}
