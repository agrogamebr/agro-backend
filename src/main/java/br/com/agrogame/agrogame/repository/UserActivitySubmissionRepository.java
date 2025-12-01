package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.UserActivitySubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserActivitySubmissionRepository extends JpaRepository<UserActivitySubmission, Integer> {
    List<UserActivitySubmission> findByUserActivityId(Integer userActivityId);
}
