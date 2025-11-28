package br.com.agrogame.agrogame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.Farm;

public interface FarmRepository extends JpaRepository<Farm, Integer> {
}
