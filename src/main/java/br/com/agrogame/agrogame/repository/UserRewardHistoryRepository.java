package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.UserRewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRewardHistoryRepository extends JpaRepository<UserRewardHistory, Integer> {
}
