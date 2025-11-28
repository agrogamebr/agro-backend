package br.com.agrogame.agrogame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.Reward;

public interface RewardRepository extends JpaRepository<Reward, Integer> {
}
