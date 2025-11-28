package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.RewardStatus;

public interface RewardStatusRepository extends JpaRepository<RewardStatus, Integer> {
    Optional<RewardStatus> findByCode(String code);
}

