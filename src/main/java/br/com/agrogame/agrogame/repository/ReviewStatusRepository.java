package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.ReviewStatus;

@Repository
public interface ReviewStatusRepository extends JpaRepository<ReviewStatus, Integer> {
	Optional<ReviewStatus> findByCode(String code);
}
