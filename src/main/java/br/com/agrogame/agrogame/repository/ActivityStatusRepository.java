package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.ActivityStatus;

public interface ActivityStatusRepository extends JpaRepository<ActivityStatus, Integer> {
	Optional<ActivityStatus> findByCode(String code);
}
