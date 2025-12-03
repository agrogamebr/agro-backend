package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.UserActivitySubmissionFile;

public interface UserActivitySubmissionFileRepository extends JpaRepository<UserActivitySubmissionFile, Integer> {

	List<UserActivitySubmissionFile> findByUserActivityId(Integer userActivityId);
}
