package br.com.agrogame.agrogame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.CropType;

public interface CropTypeRepository extends JpaRepository<CropType, Integer> {
}
