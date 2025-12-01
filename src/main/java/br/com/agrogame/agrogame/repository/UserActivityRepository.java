package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.UserActivity;

public interface UserActivityRepository extends JpaRepository<UserActivity, Integer> {

	@Query("SELECT ua FROM UserActivity ua " + "WHERE ua.user.id = :userId " + "AND ua.activity.id = :activityId "
			+ "AND ua.farm.id = :farmId")
	Optional<UserActivity> findByUserAndActivityAndFarm(@Param("userId") Integer userId,
			@Param("activityId") Integer activityId, @Param("farmId") Integer farmId);
}