package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.ActivityReward;

public interface ActivityRewardRepository extends JpaRepository<ActivityReward, Long> {
	List<ActivityReward> findByActivityId(Integer activityId);

	void deleteByActivityId(Integer activityId);
}
