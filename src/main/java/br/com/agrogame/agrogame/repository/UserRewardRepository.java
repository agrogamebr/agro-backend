package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRewardRepository extends JpaRepository<UserReward, Integer> {
}
