package br.com.agrogame.agrogame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.FarmActivity;

public interface FarmActivityRepository extends JpaRepository<FarmActivity, Long> {
}
