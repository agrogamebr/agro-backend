package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.UserActivityReview;

@Repository
public interface UserActivityReviewRepository extends JpaRepository<UserActivityReview, Integer> {

	List<UserActivityReview> findByUserActivityIdOrderByReviewedAtDesc(Integer userActivityId);
}