package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.UserActivitySubmission;

public interface UserActivitySubmissionRepository extends JpaRepository<UserActivitySubmission, Integer> {
	List<UserActivitySubmission> findByUserActivityId(Integer userActivityId);

	@Query("""
			    SELECT uas FROM UserActivitySubmission uas
			    WHERE uas.userActivity.id = :userActivityId
			    ORDER BY uas.createdAt DESC
			""")
	List<UserActivitySubmission> findByUserActivityIdOrderByCreatedAtDesc(
			@Param("userActivityId") Integer userActivityId);
}
